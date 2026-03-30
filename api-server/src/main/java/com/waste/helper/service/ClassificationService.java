package com.waste.helper.service;

import com.waste.helper.service.dto.*;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class ClassificationService {

    private final CacheService cacheService;

    public ClassificationService(CacheService cacheService) {
        this.cacheService = cacheService;
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

        // TODO: VLM gRPC 호출 (Task 5의 VlmGrpcClient 사용)
        // 현재는 fallback 응답 반환
        ClassifyDetailResponse result = createFallbackResponse(detectedClass, confidence);
        cacheService.cacheClassification(cacheKey, result);
        return result;
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
