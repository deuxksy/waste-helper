package com.waste.helper.web.rest;

import com.waste.helper.domain.Region;
import com.waste.helper.repository.RegionRepository;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/regions")
public class RegionV1Resource {

    private final RegionRepository regionRepository;

    public RegionV1Resource(RegionRepository regionRepository) {
        this.regionRepository = regionRepository;
    }

    @GetMapping
    public ResponseEntity<?> getRegions(
        @RequestParam(required = false) String sido,
        @RequestParam(required = false) String keyword
    ) {
        if (keyword != null) {
            return ResponseEntity.ok(
                regionRepository.findBySidoContainingOrSigunguContainingOrEmdNameContaining(keyword, keyword, keyword)
            );
        }
        if (sido != null) {
            return ResponseEntity.ok(regionRepository.findBySido(sido));
        }
        return ResponseEntity.ok(regionRepository.findAll());
    }

    @GetMapping("/{emdCode}")
    public ResponseEntity<?> getRegion(@PathVariable String emdCode) {
        return regionRepository.findByEmdCode(emdCode)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
}
