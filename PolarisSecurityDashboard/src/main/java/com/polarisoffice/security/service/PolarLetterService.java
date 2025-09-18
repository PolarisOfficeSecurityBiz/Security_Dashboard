package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarLetterDao;
import com.polarisoffice.security.model.PolarLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolarLetterService {

    private final PolarLetterDao dao;

    public List<PolarLetter> getAll(Integer size, String q) {
        int fetch = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetch, q);
    }

    public Optional<PolarLetter> getById(String id) {
        return dao.findById(id);
    }

    // create/update/delete 필요 시 dao 위임 메서드 추가
}
