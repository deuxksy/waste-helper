package com.waste.helper.web.rest;

import com.waste.helper.service.ClassificationService;
import com.waste.helper.service.dto.ClassifyDetailResponse;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1")
public class ClassificationResource {

    private final ClassificationService classificationService;

    public ClassificationResource(ClassificationService classificationService) {
        this.classificationService = classificationService;
    }

    @PostMapping(value = "/classify/detail", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ClassifyDetailResponse> classifyDetail(
        @RequestParam("image") MultipartFile image,
        @RequestParam("detectedClass") String detectedClass,
        @RequestParam("confidence") Float confidence,
        @RequestParam(value = "regionCode", required = false) String regionCode
    ) {
        try {
            ClassifyDetailResponse response = classificationService.classifyDetail(image, detectedClass, confidence, regionCode);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}
