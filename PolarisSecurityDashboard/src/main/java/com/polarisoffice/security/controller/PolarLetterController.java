package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.PolarLetter;
import com.polarisoffice.security.service.PolarLetterService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/polar-letters")
@RequiredArgsConstructor
public class PolarLetterController {

    private final PolarLetterService service;

    @GetMapping
    public List<PolarLetter> list(
            @RequestParam(required = false, defaultValue = "200") Integer size,
            @RequestParam(required = false) String q) {
        return service.getAll(size, q);
    }

    @GetMapping("/{id}")
    public PolarLetter get(@PathVariable String id) {
        return service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PolarLetter not found: " + id));
    }
}
