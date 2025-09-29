package com.polarisoffice.security.controller;


import org.springframework.http.*;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;

import java.io.InputStream;
import java.net.*;
import java.time.Duration;
import java.util.Set;

@RestController
public class ImageProxyController {

    // 필요 호스트를 추가하세요. 당장은 진단 위해 *느슨하게* 가도 됩니다.
    private static final Set<String> ALLOW_HOSTS = Set.of(
        "postfiles.pstatic.net", "ssl.pstatic.net", "i.imgur.com",
        "images.unsplash.com", "picsum.photos", "img.youtube.com",
        "naver.com", "m.blog.naver.com", "blog.naver.com"
    );

    @GetMapping("/img-proxy")
    public ResponseEntity<byte[]> proxy(@RequestParam("u") String u) {
        try {
            if (u == null || u.isBlank()) return ResponseEntity.badRequest().build();

            URI uri = new URI(u);
            String scheme = uri.getScheme();
            if (scheme == null || !(scheme.equals("http") || scheme.equals("https"))) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            String host = uri.getHost();
            if (host == null) return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();

            // ✔️ 404 방지: 일단 느슨히 통과(문제 잡힌 뒤 다시 좁히세요)
            boolean allowed = ALLOW_HOSTS.stream().anyMatch(h -> host.endsWith(h));
            if (!allowed) {
                // return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                // 진단 단계에서는 통과
            }

            // 최대 3회 리다이렉트 추적
            URL url = uri.toURL();
            for (int i = 0; i < 3; i++) {
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setInstanceFollowRedirects(false); // 수동 추적
                conn.setConnectTimeout(8000);
                conn.setReadTimeout(12000);
                conn.setRequestMethod("GET");

                // ✔️ CDN 403 회피: UA/Accept/Referer 세팅
                conn.setRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 "
                    + "(KHTML, like Gecko) Chrome/124.0 Safari/537.36");
                conn.setRequestProperty("Accept",
                    "image/avif,image/webp,image/apng,image/*,*/*;q=0.8");
                // 많은 CDN이 같은 출처의 레퍼러를 요구: scheme://host/ 로 설정
                String ref = uri.getScheme() + "://" + host + "/";
                conn.setRequestProperty("Referer", ref);

                int code = conn.getResponseCode();
                if (code == HttpURLConnection.HTTP_MOVED_PERM ||
                    code == HttpURLConnection.HTTP_MOVED_TEMP ||
                    code == HttpURLConnection.HTTP_SEE_OTHER ||
                    code == 307 || code == 308) {
                    String loc = conn.getHeaderField("Location");
                    if (loc == null) break;
                    url = new URL(url, loc); // 상대경로도 처리
                    continue;
                }

                if (code / 100 != 2) {
                    // 실패면 502로 돌려주고 <img onerror>가 플레이스홀더로 대체
                    return ResponseEntity.status(code).body(new byte[0]);
                }

                String ct = conn.getContentType();
                MediaType mediaType = (ct != null && !ct.isBlank())
                        ? MediaType.parseMediaType(ct)
                        : MediaType.IMAGE_JPEG;

                try (InputStream is = conn.getInputStream()) {
                    byte[] body = StreamUtils.copyToByteArray(is);
                    return ResponseEntity.ok()
                    	    .contentType(mediaType)
                    	    .cacheControl(CacheControl.maxAge(Duration.ofHours(6)))
                    	    .header("X-Content-Type-Options", "nosniff")
                    	    .body(body);
                }
            }
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).build();
        }
    }
}