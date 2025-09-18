package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.PolarLetter;
import com.polarisoffice.security.service.PolarLetterService;
import jakarta.validation.Valid;
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

    /** 목록 조회 */
    @GetMapping
    public List<PolarLetter> list(
            @RequestParam(required = false, defaultValue = "200") Integer size,
            @RequestParam(required = false) String q) {
        return service.getAll(size, q);
    }

    /** 단건 조회 */
    @GetMapping("/{id}")
    public PolarLetter get(@PathVariable String id) {
        return service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PolarLetter not found: " + id));
    }

    /** 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolarLetter create(@Valid @RequestBody PolarLetter req) {
        return service.create(req);
    }

    /** 수정 */
    @PatchMapping("/{id}")
    public PolarLetter update(@PathVariable String id, @Valid @RequestBody PolarLetter req) {
        return service.update(id, req);
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
