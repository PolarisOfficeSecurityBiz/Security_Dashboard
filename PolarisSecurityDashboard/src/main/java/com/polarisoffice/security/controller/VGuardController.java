package com.polarisoffice.security.controller;

import com.polarisoffice.security.model.Sdk;
import com.polarisoffice.security.model.SdkHistory;
import com.polarisoffice.security.service.VGuardService;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.UrlResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.Principal;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin/vguard")
@RequiredArgsConstructor
public class VGuardController {

    private final VGuardService vguardService;

    /**
     * V-Guard 메인 페이지 - SDK 목록 표시
     */
    @GetMapping
    public String showPage(Model model) {
        List<Sdk> sdkList = vguardService.getAllSdkList();
        model.addAttribute("sdkList", sdkList);
        return "admin/vguard/vguard";
    }

    /**
     * SDK 업로드 처리
     */
    @PostMapping("/upload")
    public String uploadSdk(@RequestParam("sdkType") String sdkType,
                            @RequestParam("sdkFile") MultipartFile sdkFile,
                            Principal principal,
                            RedirectAttributes redirectAttributes) throws IOException {

        String username = principal != null ? principal.getName() : "관리자";

        vguardService.saveSdkFile(sdkType, sdkFile, username);

        // ✅ 업로드 성공 메시지 전달
        redirectAttributes.addFlashAttribute("toast", "SDK 업로드가 완료되었습니다!");
        return "redirect:/admin/vguard";
    }

    /**
     * SDK 파일 다운로드
     */
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadSdk(@PathVariable Long id) throws IOException {
        Sdk sdk = vguardService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 SDK ID: " + id));

        Path path = Paths.get(sdk.getFilePath());
        Resource resource = new UrlResource(path.toUri());

        if (!resource.exists() || !resource.isReadable()) {
            throw new IOException("파일을 읽을 수 없습니다: " + sdk.getFilePath());
        }

        String fileName = sdk.getFileName();

        return ResponseEntity.ok()
                .header("Content-Disposition", "attachment; filename=\"" + fileName + "\"")
                .body(resource);
    }

    /**
     * SDK 히스토리 페이지
     */
    @GetMapping("/history/{sdkId}")
    public String viewSdkHistory(@PathVariable Long sdkId, Model model, Principal principal) {
        Sdk sdk = vguardService.findById(sdkId)
                .orElseThrow(() -> new IllegalArgumentException("SDK를 찾을 수 없습니다."));

        List<SdkHistory> historyList = vguardService.findBySdkId(sdkId);

        model.addAttribute("sdk", sdk);
        model.addAttribute("historyList", historyList);

        // ✨ 수정 버튼 노출용 현재 사용자
        model.addAttribute("currentUser", principal != null ? principal.getName() : "");
        return "admin/vguard/sdk_history";
    }

    /** 릴리즈 노트 수정 (히스토리 소유자 또는 관리자만) */
    @PatchMapping("/history/{historyId}/note")
    @ResponseBody
    public ResponseEntity<?> updateHistoryNote(
            @PathVariable Long historyId,
            @RequestBody Map<String, String> body,
            Authentication authentication
    ) {
        String newNote = body.getOrDefault("releaseNote", "").trim();
        
        // 릴리즈 노트의 길이가 500자를 초과하면 에러 반환
        if (newNote.length() > 500) {
            return ResponseEntity.badRequest().body(Map.of("error", "릴리즈 노트는 500자를 넘을 수 없습니다."));
        }

        // authentication 객체가 null인 경우, 예를 들어 "anonymous"로 처리
        String username = (authentication != null && authentication.getName() != null) 
                          ? authentication.getName() 
                          : "anonymous";
        
        try {
            // 릴리즈 노트 업데이트
            vguardService.updateReleaseNote(historyId, newNote, username);
            return ResponseEntity.ok(Map.of("ok", true));
        } catch (SecurityException e) {
            // 예외 처리: 예를 들어 권한 부족으로 인한 오류 처리
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "수정 권한이 없습니다."));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of("error", "서버 오류 발생"));
        }
    }

}
