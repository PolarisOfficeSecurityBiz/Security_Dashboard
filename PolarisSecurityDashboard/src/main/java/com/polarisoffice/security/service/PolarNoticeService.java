package com.polarisoffice.security.service;

import com.polarisoffice.security.dao.PolarNoticeDao;
import com.polarisoffice.security.model.PolarNotice;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PolarNoticeService {

    private final PolarNoticeDao dao;

    public List<PolarNotice> getAll(Integer size, String q) {
        int fetch = (size == null || size <= 0) ? 200 : Math.min(size, 1000);
        return dao.findAll(fetch, q);
    }

    public Optional<PolarNotice> getById(String id) {
        return dao.findById(id);
    }

    public PolarNotice create(PolarNotice req) {
        return dao.create(req);
    }

    public PolarNotice update(String id, PolarNotice patch) {
        return dao.update(id, patch);
    }

    public void delete(String id) {
        dao.delete(id);
    }
}
