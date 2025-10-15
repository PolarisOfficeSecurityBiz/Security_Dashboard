package com.polarisoffice.security.service;

import com.polarisoffice.security.model.SdkEntity;
import com.polarisoffice.security.repository.SdkRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;

@Service
public class VGuardService {

    private final SdkRepository sdkRepository;

    private final Path uploadPath = Paths.get("uploads/sdk");

    public VGuardService(SdkRepository sdkRepository) {
        this.sdkRepository = sdkRepository;
    }

    public void saveSdkFile(String sdkType, MultipartFile file, String username) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = sdkType + "_" + timestamp + "_" + file.getOriginalFilename();
        Path target = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

        // DB 기록 저장
        SdkEntity sdk = new SdkEntity(
                sdkType,
                fileName,
                target.toAbsolutePath().toString(),
                LocalDateTime.now(),
                username
        );
        sdkRepository.save(sdk);
    }

    public java.util.List<SdkEntity> getAllSdkList() {
        return sdkRepository.findAll();
    }
}
