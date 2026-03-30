package com.waste.helper.service.dto;

import java.util.List;

public record ClassifyDetailResponse(
    String detectedClass,
    String confirmedClass,
    float confidence,
    DisposalMethodResponse disposalMethod,
    CostInfoResponse costInfo,
    List<String> warnings,
    String regionSpecific,
    String source,
    boolean cached
) {}
