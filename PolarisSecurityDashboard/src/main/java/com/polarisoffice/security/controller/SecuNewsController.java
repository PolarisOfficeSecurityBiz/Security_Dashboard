package com.polarisoffice.security.controller;

import com.polarisoffice.security.dto.SecuNewsCreateRequest;
import com.polarisoffice.security.dto.SecuNewsUpdateRequest;
import com.polarisoffice.security.model.SecuNews;
import com.polarisoffice.security.service.SecuNewsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/v1/secu-news")
@RequiredArgsConstructor
public class SecuNewsController {

    private final SecuNewsService service;

    /** 목록 */
    @GetMapping
    public List<SecuNews> list(
            @RequestParam(required = false, defaultValue = "200") Integer size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String category) {
        return service.getAll(size, q, category);
    }

    /** 단건 */
    @GetMapping("/{id}")
    public SecuNews findById(@PathVariable String id) {
        return service.getById(id)
                .orElseThrow(() ->
                        new ResponseStatusException(HttpStatus.NOT_FOUND, "SecuNews not found: " + id));
    }

    /** 생성 */
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SecuNews create(@Valid @RequestBody SecuNewsCreateRequest req) {
        return service.create(req);
    }

    /** 부분수정 */
    @PatchMapping("/{id}")
    public SecuNews update(@PathVariable String id,
                           @Valid @RequestBody SecuNewsUpdateRequest req) {
        return service.update(id, req);
    }

    /** 삭제 */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable String id) {
        service.delete(id);
    }
}
