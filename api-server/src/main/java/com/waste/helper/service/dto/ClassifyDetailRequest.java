package com.waste.helper.service.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ClassifyDetailRequest(
    @NotBlank String detectedClass,
    @NotNull Float confidence,
    String regionCode
) {}
