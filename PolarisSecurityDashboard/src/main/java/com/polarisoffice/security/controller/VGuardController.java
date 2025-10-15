package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.SdkEntity;
import com.polarisoffice.security.repository.SdkRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.core.io.Resource;
import java.io.IOException;
import java.nio.file.*;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/vguard")
@RequiredArgsConstructor
public class VGuardController {

    private final SdkRepository sdkRepository;

    // SDK 목록 표시
    @GetMapping
    public String showPage(Model model) {
        List<SdkEntity> sdkList = sdkRepository.findAll();
        model.addAttribute("sdkList", sdkList);
        return "admin/vguard/vguard";
    }

    // SDK 업로드 처리
    @PostMapping("/upload")
    public String uploadSdk(@RequestParam("sdkType") String sdkType,
                            @RequestParam("sdkFile") MultipartFile sdkFile,
                            Principal principal) throws IOException {

        String username = principal != null ? principal.getName() : "관리자";

        // 저장 디렉토리 지정
        Path uploadDir = Paths.get("uploads/sdk");
        if (!Files.exists(uploadDir)) {
            Files.createDirectories(uploadDir);
        }

        // 파일 저장
        String fileName = sdkFile.getOriginalFilename();
        Path filePath = uploadDir.resolve(fileName);
        sdkFile.transferTo(filePath.toFile());

        // DB 저장
        SdkEntity sdkEntity = new SdkEntity(
                sdkType,
                fileName,
                filePath.toString(),
                LocalDateTime.now(),
                username
        );
        sdkRepository.save(sdkEntity);

        return "redirect:/admin/vguard";
    }

    // SDK 파일 다운로드
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadSdk(@PathVariable Long id) throws IOException {
        SdkEntity sdk = sdkRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 SDK ID: " + id));

        Path path = Paths.get(sdk.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 읽을 수 없습니다: " + sdk.getFilePath());
        }

        String fileName = sdk.getFileName();

        // ✅ Content-Disposition 헤더로 강제 다운로드
        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

}
