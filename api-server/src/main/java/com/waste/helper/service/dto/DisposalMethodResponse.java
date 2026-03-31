package com.waste.helper.service.dto;

import java.util.List;

public record DisposalMethodResponse(
    String method,
    List<String> notes,
    List<DisposalItemResponse> items
) {}
