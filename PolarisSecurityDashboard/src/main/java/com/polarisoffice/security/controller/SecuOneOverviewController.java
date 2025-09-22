package com.polarisoffice.security.controller;

import com.polarisoffice.security.service.PolarNoticeService;
import com.polarisoffice.security.service.PolarLetterService;
import com.polarisoffice.security.service.SecuNewsService;
import com.polarisoffice.security.service.PolarDirectAdService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1")
public class SecuOneOverviewController {

    private final PolarNoticeService polarNoticeService;
    private final PolarLetterService polarLetterService;
    private final SecuNewsService secuNewsService;
    private final PolarDirectAdService directAdService;

    public SecuOneOverviewController(PolarNoticeService polarNoticeService, 
                              PolarLetterService polarLetterService,
                              SecuNewsService secuNewsService,
                              PolarDirectAdService directAdService) {
        this.polarNoticeService = polarNoticeService;
        this.polarLetterService = polarLetterService;
        this.secuNewsService = secuNewsService;
        this.directAdService = directAdService;
    }

    @GetMapping("/overview")
    public OverviewResponse getOverviewData() {
        try {
            int noticeCount = polarNoticeService.getCount();
            int letterCount = polarLetterService.getCount();
            int newsCount = secuNewsService.getCount();
            int adCount = directAdService.getCount();

            int noticeDelta = polarNoticeService.getDelta();
            int letterDelta = polarLetterService.getDelta();
            int newsDelta = secuNewsService.getDelta();
            int adDelta = directAdService.getDelta();

            return new OverviewResponse(
                new OverviewResponse.KpiData(noticeCount, noticeDelta, null), // items는 null로 시작
                new OverviewResponse.KpiData(letterCount, letterDelta, null),
                new OverviewResponse.KpiData(newsCount, newsDelta, null),
                new OverviewResponse.KpiData(adCount, adDelta, null)
            );
        } catch (Exception e) {
            // 로그를 추가하여 오류를 추적
            e.printStackTrace();
            throw new RuntimeException("Failed to fetch overview data", e);
        }
    }
    public static class OverviewResponse {
        private final KpiData notice;
        private final KpiData letter;
        private final KpiData news;
        private final KpiData ad;

        public OverviewResponse(KpiData notice, KpiData letter, KpiData news, KpiData ad) {
            this.notice = notice;
            this.letter = letter;
            this.news = news;
            this.ad = ad;
        }

        public KpiData getNotice() {
            return notice;
        }

        public KpiData getLetter() {
            return letter;
        }

        public KpiData getNews() {
            return news;
        }

        public KpiData getAd() {
            return ad;
        }

        public static class KpiData {
            private final int count;
            private final int delta;
            private final Object items;  // 추가된 부분: 실제 데이터 항목들 (예: 목록)

            public KpiData(int count, int delta, Object items) {
                this.count = count;
                this.delta = delta;
                this.items = items;
            }

            public int getCount() {
                return count;
            }

            public int getDelta() {
                return delta;
            }

            public Object getItems() {
                return items;
            }
        }
    }
}
