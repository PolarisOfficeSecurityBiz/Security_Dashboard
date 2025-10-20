package com.polarisoffice.security.service;

import com.polarisoffice.security.model.Sdk;
import com.polarisoffice.security.model.SdkHistory;
import com.polarisoffice.security.repository.SdkHistoryRepository;
import com.polarisoffice.security.repository.SdkRepository;

import jakarta.transaction.Transactional;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class VGuardService {

    private final SdkRepository sdkRepository;
    private final SdkHistoryRepository sdkHistoryRepository;
    private final Path uploadPath = Paths.get("uploads/sdk");

    public VGuardService(SdkRepository sdkRepository, SdkHistoryRepository sdkHistoryRepository) {
        this.sdkRepository = sdkRepository;
        this.sdkHistoryRepository = sdkHistoryRepository;
    }

    /**
     * ✅ SDK 업로드
     * - 같은 유형이 있으면 덮어쓰기
     * - 히스토리 기록 남김
     */
    @Transactional
    public void saveSdkFile(String sdkType, MultipartFile file, String username) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("파일이 선택되지 않았습니다.");
        }

        // 업로드 경로 없으면 생성
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 기존 SDK 여부 확인
        Optional<Sdk> existingOpt = sdkRepository.findBySdkType(sdkType);

        // 파일 이름 생성
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = sdkType + "_" + timestamp + "_" + file.getOriginalFilename();
        Path targetPath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        // 공통 저장 정보
        LocalDateTime now = LocalDateTime.now();

        if (existingOpt.isPresent()) {
            // 기존 SDK 덮어쓰기
            Sdk existingSdk = existingOpt.get();
            existingSdk.setFileName(fileName);
            existingSdk.setFilePath(targetPath.toAbsolutePath().toString());
            existingSdk.setUploadedAt(now);
            existingSdk.setUploadedBy(username);
            sdkRepository.save(existingSdk);

            // 히스토리 기록
            saveHistory(existingSdk, timestamp, username, sdkType, "업로드(덮어쓰기)",
                    sdkType + " SDK 파일이 업데이트되었습니다.");

        } else {
            // 신규 SDK 등록
            Sdk newSdk = Sdk.builder()
                    .sdkType(sdkType)
                    .fileName(fileName)
                    .filePath(targetPath.toAbsolutePath().toString())
                    .uploadedAt(now)
                    .uploadedBy(username)
                    .build();
            sdkRepository.save(newSdk);

            // 히스토리 기록
            saveHistory(newSdk, timestamp, username, sdkType, "업로드(신규)",
                    sdkType + " SDK 최초 업로드");
        }
    }

    /**
     * ✅ 히스토리 기록 저장
     */
    private void saveHistory(Sdk sdk, String timestamp, String username, String sdkType,
                             String actionType, String releaseNote) {
        SdkHistory history = SdkHistory.builder()
                .sdk(sdk)
                .version("v" + timestamp)
                .actionType(actionType)
                .uploadedBy(username)
                .releaseNote(releaseNote)
                .downloadUrl("/admin/vguard/download/" + sdk.getId())
                .createdAt(LocalDateTime.now())
                .build();

        sdkHistoryRepository.save(history);
    }

    /**
     * ✅ SDK 리스트 (유형별 최신 1개만)
     */
    public List<Sdk> getAllSdkList() {
        return sdkRepository.findAllByOrderByUploadedAtDesc()
                .stream()
                .collect(Collectors.toMap(
                        Sdk::getSdkType, // key: SDK 유형
                        sdk -> sdk,      // value: SDK 객체
                        (first, second) -> first, // 중복일 경우 첫 번째만
                        LinkedHashMap::new
                ))
                .values()
                .stream()
                .toList();
    }

    /**
     * ✅ 특정 SDK의 이력 조회
     */
    public List<SdkHistory> findBySdkId(Long sdkId) {
        return sdkHistoryRepository.findBySdkIdOrderByCreatedAtDesc(sdkId);
    }

    /**
     * ✅ SDK ID로 상세 조회
     */
    public Optional<Sdk> findById(Long sdkId) {
        return sdkRepository.findById(sdkId);
    }

    @Transactional
    public void updateReleaseNote(Long historyId, String newNote, String currentUser) {
        SdkHistory history = sdkHistoryRepository.findById(historyId)
                .orElseThrow(() -> new IllegalArgumentException("히스토리를 찾을 수 없습니다. id=" + historyId));

        // 서버에서도 권한 검증 (업로더이거나 ADMIN 권한)
        boolean isOwner = history.getUploadedBy() != null && history.getUploadedBy().equals(currentUser);
        boolean isAdmin = false;
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            isAdmin = auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()));
        }
        if (!(isOwner || isAdmin)) {
            throw new SecurityException("수정 권한이 없습니다.");
        }

        history.setReleaseNote(newNote);
        sdkHistoryRepository.save(history);
    }
}
