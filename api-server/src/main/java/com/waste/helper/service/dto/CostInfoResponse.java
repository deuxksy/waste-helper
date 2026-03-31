package com.waste.helper.service.dto;

public record CostInfoResponse(
    String type,
    int amount,
    String currency,
    String collectionSchedule,
    String notes
) {}
