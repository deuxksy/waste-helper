package com.waste.helper.security;

import com.waste.helper.config.RateLimitConfig;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ConcurrentHashMap<String, Bucket> buckets;

    public RateLimitFilter(ConcurrentHashMap<String, Bucket> rateLimitBuckets) {
        this.buckets = rateLimitBuckets;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
        throws ServletException, IOException {

        String path = request.getRequestURI();
        // Only rate-limit API endpoints
        if (!path.startsWith("/api/")) {
            filterChain.doFilter(request, response);
            return;
        }

        // IP-based rate limit
        String clientIp = getClientIp(request);
        Bucket ipBucket = buckets.computeIfAbsent("ip:" + clientIp, k -> RateLimitConfig.createIpBucket());
        if (!ipBucket.tryConsume(1)) {
            sendRateLimitResponse(response);
            return;
        }

        // User/Guest rate limit
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            String userId = auth.getName();
            Bucket userBucket = buckets.computeIfAbsent("user:" + userId, k -> RateLimitConfig.createUserBucket());
            if (!userBucket.tryConsume(1)) {
                sendRateLimitResponse(response);
                return;
            }
        } else {
            // Guest: use deviceId from JWT subject or fallback to IP
            String guestKey = "guest:" + clientIp;
            Bucket guestBucket = buckets.computeIfAbsent(guestKey, k -> RateLimitConfig.createGuestBucket());
            if (!guestBucket.tryConsume(1)) {
                sendRateLimitResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty()) {
            ip = request.getRemoteAddr();
        } else {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

    private void sendRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(429);
        response.setHeader("X-RateLimit-Remaining", "0");
        response.setContentType("application/json");
        response.getWriter().write("{\"error\":\"Too many requests\",\"status\":429}");
    }
}
