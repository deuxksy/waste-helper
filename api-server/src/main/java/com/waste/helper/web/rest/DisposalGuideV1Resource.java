package com.waste.helper.web.rest;

import com.waste.helper.domain.DisposalGuide;
import com.waste.helper.domain.enumeration.Source;
import com.waste.helper.repository.DisposalGuideRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/disposal-guide")
public class DisposalGuideV1Resource {

    private final DisposalGuideRepository disposalGuideRepository;

    public DisposalGuideV1Resource(DisposalGuideRepository disposalGuideRepository) {
        this.disposalGuideRepository = disposalGuideRepository;
    }

    @GetMapping
    public ResponseEntity<?> getDisposalGuide(
        @RequestParam String wasteType,
        @RequestParam(required = false) String regionCode
    ) {
        if (regionCode != null) {
            // 1순위: 공공데이터
            Optional<DisposalGuide> guide = disposalGuideRepository
                .findByRegion_EmdCodeAndWasteTypeAndSource(regionCode, wasteType, Source.PUBLIC_API);
            if (guide.isPresent()) {
                return ResponseEntity.ok(guide.get());
            }
            // 2순위: LLM 보완
            guide = disposalGuideRepository
                .findByRegion_EmdCodeAndWasteTypeAndSource(regionCode, wasteType, Source.LLM_SUPPLEMENTED);
            if (guide.isPresent()) {
                return ResponseEntity.ok(guide.get());
            }
        }
        List<DisposalGuide> guides = disposalGuideRepository.findByWasteType(wasteType);
        return ResponseEntity.ok(guides);
    }
}
