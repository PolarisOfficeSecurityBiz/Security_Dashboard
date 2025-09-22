package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.PolarNotice;
import com.polarisoffice.security.service.PolarNoticeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/polar-notices")
@RequiredArgsConstructor
public class PolarNoticeController {

    private final PolarNoticeService service;

    /** 목록 */
    @GetMapping
    public List<PolarNotice> list(
            @RequestParam(required = false, defaultValue = "200") Integer size,
            @RequestParam(required = false) String q) {
        return service.getAll(size, q);
    }

    /** 단건 */
    @GetMapping("/{id}")
    public PolarNotice get(@PathVariable String id) {
        return service.getById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "PolarNotice not found: " + id));
    }

    /** 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public PolarNotice create(@Valid @RequestBody PolarNotice req) {
        return service.create(req);
    }

    /** 부분 수정 */
    @PatchMapping("/{id}")
    public PolarNotice update(@PathVariable String id, @Valid @RequestBody PolarNotice patch) {
        return service.update(id, patch);
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
