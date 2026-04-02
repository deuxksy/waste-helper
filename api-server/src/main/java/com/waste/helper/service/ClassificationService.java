package com.waste.helper.service;

import com.waste.helper.grpc.VlmGrpcClient;
import com.waste.helper.grpc.vlm.AnalyzeResponse;
import com.waste.helper.grpc.vlm.DisposalMethod;
import com.waste.helper.service.dto.*;
import io.github.resilience4j.circuitbreaker.CallNotPermittedException;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.retry.Retry;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClassificationService {

    private static final Logger log = LoggerFactory.getLogger(ClassificationService.class);

    private final VlmGrpcClient vlmGrpcClient;
    private final CacheService cacheService;
    private final CircuitBreaker circuitBreaker;
    private final Retry retry;

    public ClassificationService(
        VlmGrpcClient vlmGrpcClient,
        CacheService cacheService,
        CircuitBreaker vlmCircuitBreaker,
        Retry vlmRetry
    ) {
        this.vlmGrpcClient = vlmGrpcClient;
        this.cacheService = cacheService;
        this.circuitBreaker = vlmCircuitBreaker;
        this.retry = vlmRetry;
    }

    public ClassifyDetailResponse classifyDetail(
        MultipartFile image,
        String detectedClass,
        float confidence,
        String regionCode
    ) {
        String cacheKey = cacheService.generateCacheKey(detectedClass, regionCode);

        // 캐시 확인
        ClassifyDetailResponse cached = cacheService.getCachedClassification(cacheKey);
        if (cached != null) {
            return new ClassifyDetailResponse(
                cached.detectedClass(),
                cached.confirmedClass(),
                cached.confidence(),
                cached.disposalMethod(),
                cached.costInfo(),
                cached.warnings(),
                cached.regionSpecific(),
                cached.source(),
                true
            );
        }

        // VLM gRPC 호출 (CircuitBreaker + Retry)
        ClassifyDetailResponse result;
        try {
            byte[] imageData = image.getBytes();
            AnalyzeResponse response = CircuitBreaker.decorateSupplier(circuitBreaker,
                Retry.decorateSupplier(retry, () ->
                    vlmGrpcClient.analyzeWaste(imageData, detectedClass, confidence, regionCode)
                )
            ).get();

            result = mapGrpcResponse(response, detectedClass, confidence);
            log.info("VLM 분류 성공: {} → {}", detectedClass, response.getWasteType());
        } catch (CallNotPermittedException e) {
            log.warn("CircuitBreaker OPEN — VLM 서비스 호출 차단");
            result = createFallbackResponse(detectedClass, confidence);
        } catch (Exception e) {
            log.warn("VLM gRPC 호출 실패, fallback 응답 반환: {}", e.getMessage());
            result = createFallbackResponse(detectedClass, confidence);
        }

        cacheService.cacheClassification(cacheKey, result);
        return result;
    }

    private ClassifyDetailResponse mapGrpcResponse(
        AnalyzeResponse response,
        String detectedClass,
        float confidence
    ) {
        DisposalMethod dm = response.getDisposalMethod();
        DisposalMethodResponse disposalMethod = new DisposalMethodResponse(
            dm.getMethod(),
            dm.getNotesList(),
            dm.getItemsList().stream()
                .map(item -> new DisposalItemResponse(item.getLabel(), item.getAction()))
                .toList()
        );

        CostInfoResponse costInfo = new CostInfoResponse(
            response.getCostInfo(),
            0,
            "KRW",
            null,
            null
        );

        // gRPC warnings는 단일 String → 비어있지 않으면 리스트로 래핑
        List<String> warnings = response.getWarnings().isEmpty()
            ? List.of()
            : List.of(response.getWarnings());

        return new ClassifyDetailResponse(
            detectedClass,
            response.getWasteType(),
            response.getConfidence() > 0 ? response.getConfidence() : confidence,
            disposalMethod,
            costInfo,
            warnings,
            null,
            "VLM",
            false
        );
    }

    private ClassifyDetailResponse createFallbackResponse(String detectedClass, float confidence) {
        return new ClassifyDetailResponse(
            detectedClass,
            detectedClass,
            confidence,
            new DisposalMethodResponse("기본 배출 요령", List.of(), List.of()),
            new CostInfoResponse("알 수 없음", 0, "KRW", null, null),
            List.of("VLM 서비스 미연결 — 기본 정보만 제공"),
            null,
            "YOLO_ONLY",
            false
        );
    }
}
