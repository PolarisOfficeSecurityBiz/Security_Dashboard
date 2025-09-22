package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.PolarDirectAd;
import com.polarisoffice.security.dto.PolarDirectAdCreateRequest;
import com.polarisoffice.security.dto.PolarDirectAdUpdateRequest;
import com.polarisoffice.security.service.PolarDirectAdService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/direct-ads")
@RequiredArgsConstructor
public class PolarDirectAdController {

    private final PolarDirectAdService service;

    @GetMapping
    public List<PolarDirectAd> findAll() {
        return service.getAll();
    }

    @GetMapping("/{id}")
    public PolarDirectAd findById(@PathVariable String id) {
        return service.getById(id)
                .orElseThrow(() -> new IllegalArgumentException("Ad not found: " + id));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public PolarDirectAd create(@Valid @RequestBody PolarDirectAdCreateRequest request) {
        return service.create(request);
    }

    @PatchMapping("/{id}")
    public PolarDirectAd update(@PathVariable String id,
                                @Valid @RequestBody PolarDirectAdUpdateRequest request) {
        return service.update(id, request);
    }

    @ResponseStatus(HttpStatus.NO_CONTENT)
    @DeleteMapping("/{id}")
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
