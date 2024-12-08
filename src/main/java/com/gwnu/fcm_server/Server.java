package com.gwnu.fcm_server;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.gson.Gson;
import com.gwnu.fcm_server.Controller.FcmController;
import com.gwnu.fcm_server.Dto.MessageRequestDTO;
import com.gwnu.fcm_server.service.FcmService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final Gson gson = new Gson();
    private static final String BASE_URL = "https://www.safetydata.go.kr/V2/api/DSSP-IF-00247";
    private static final String SERVICE_KEY = System.getenv("DISASTER_API_KEY");
    private static final long POLLING_INTERVAL = 90000L;  // 144분 (하루 10회 호출 기준)

    private long lastMessageSN = 0;
    private final FcmController fcmController;

    // API 응답 구조를 위한 데이터 클래스들
    public static class ApiResponse {
        public Header header;
        public List<DisasterMessage> body;
        public int numOfRows;
        public int pageNo;
        public int totalCount;
    }

    public static class Header {
        public String resultMsg;
        public String resultCode;
        public String errorMsg;
    }

    public static class DisasterMessage {
        public String MSG_CN;          // 메시지 내용
        public String RCPTN_RGN_NM;    // 수신 지역
        public String CRT_DT;          // 생성 일시
        public String EMRG_STEP_NM;    // 긴급 단계
        public String DST_SE_NM;       // 재해 구분
        public String SN;              // 일련번호
        public String REG_YMD;         // 등록 일자
        public String MDFCN_YMD;       // 수정 일자
    }

    private String formatMessage(DisasterMessage message) {
        return String.format("""
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            [재난문자 #%s]
            %s - %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            • 발생시각: %s
            • 발생지역: %s
            • 내용: %s
            • 등록일시: %s
            • 수정일시: %s
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """,
                message.SN,
                message.DST_SE_NM,
                message.EMRG_STEP_NM,
                message.CRT_DT,
                message.RCPTN_RGN_NM,
                message.MSG_CN,
                message.REG_YMD != null ? message.REG_YMD : "없음",
                message.MDFCN_YMD != null ? message.MDFCN_YMD : "없음"
        );
    }

    private List<DisasterMessage> fetchAlerts() {
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        try {
            urlBuilder.append("?serviceKey=").append(URLEncoder.encode(SERVICE_KEY, "UTF-8"))
                    .append("&returnType=json")
                    .append("&pageNo=1")
                    .append("&numOfRows=20")
                    .append("&crtDt=").append(currentDate);

            URL url = new URL(urlBuilder.toString());
            logger.debug("요청 URL: {}", url);

            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);

            try {
                int responseCode = connection.getResponseCode();
                if (responseCode >= 200 && responseCode < 300) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream(), "UTF-8"));
                    String responseText = reader.lines().collect(Collectors.joining("\n"));
                    reader.close();
                    logger.info("API 호출...");

                    ApiResponse response = gson.fromJson(responseText, ApiResponse.class);
                    if ("00".equals(response.header.resultCode)) {
                        List<DisasterMessage> newMessages = new ArrayList<>();

                        if(response.body != null) {
                            newMessages = response.body.stream()
                                    .filter(message -> Long.parseLong(message.SN) > lastMessageSN)
                                    .collect(Collectors.toList());
                        }

                        if (!newMessages.isEmpty()) {
                            lastMessageSN = newMessages.stream()
                                    .mapToLong(message -> Long.parseLong(message.SN))
                                    .max()
                                    .getAsLong();
                            logger.info("마지막 메시지 SN 업데이트: {}", lastMessageSN);
                        }

                        return newMessages;
                    } else {
                        logger.error("API 오류: {}", response.header.errorMsg);
                    }
                } else {
                    logger.error("API 호출 실패: {}", responseCode);
                }
            } finally {
                connection.disconnect();
            }
        } catch (Exception e) {
            logger.error("API 호출 중 오류 발생: {}", e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    public void startMonitoring() {
        logger.info("""
            재난문자 모니터링을 시작합니다...
            • API 주소: {}
            • 호출 간격: {}초
            • 예상 일일 호출 횟수: {}회
            ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
            """,
                BASE_URL,
                POLLING_INTERVAL / 1000,
                86400000 / POLLING_INTERVAL);

        while (true) {
            try {
                List<DisasterMessage> messages = fetchAlerts();
                if (!messages.isEmpty()) {
                    logger.info("\n=== 새로운 재난문자 {}건이 수신되었습니다 ===", messages.size());
                    for (DisasterMessage message : messages) {
                        logger.info("\n{}", formatMessage(message));

                        try {
                            String title = message.DST_SE_NM + " - " + message.CRT_DT;
                            String body = message.MSG_CN;

                            logger.info("title = {}, body = {}", title, body);
                            MessageRequestDTO messageRequestDTO = new MessageRequestDTO(title, body, "FCMMessage");

                            int retryCount = 0;
                            while (retryCount < 3) {
                                try {
                                    fcmController.sendMessageTopic(messageRequestDTO);
                                    logger.info("sendMessageTopic 완료");
                                    break;
                                } catch (Exception e) {
                                    retryCount++;
                                    logger.error("Failed to send message (attempt {}/3)", retryCount, e);
                                    if (retryCount < 3) Thread.sleep(1000);
                                    else throw e;
                                }
                            }
                        } catch (FirebaseMessagingException e) {
                            logger.error("FCM 메시지 전송 실패", e);
                        }
                    }
                } else {
                    logger.debug("수신된 재난문자가 없습니다.");
                }
                Thread.sleep(POLLING_INTERVAL);
            } catch (Exception e) {
                logger.error("모니터링 중 오류 발생", e);
                try {
                    Thread.sleep(POLLING_INTERVAL);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
    }
}
