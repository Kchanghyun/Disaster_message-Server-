package com.gwnu.fcm_server;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.gson.Gson;
import com.gwnu.fcm_server.Dto.ApiResponseDTO;
import com.gwnu.fcm_server.Dto.DisasterMessageDTO;
import com.gwnu.fcm_server.service.FcmService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
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

@Slf4j
@Component
@RequiredArgsConstructor
public class Server {
    private static final Gson gson = new Gson();
    private static final String BASE_URL = "https://www.safetydata.go.kr/V2/api/DSSP-IF-00247";
    private static final String SERVICE_KEY = System.getenv("DISASTER_API_KEY");
    private static final long POLLING_INTERVAL = 90000L;  // 90초마다 호출 (하루 960번 호출) - 일일 한도 1000회

    private long lastMessageSN = 0;
    private final FcmService fcmService; // Autowired

    // fixedRate = 이전 실행 시작 시점 기준 다음 실행
    // fixedDelay = 이전 실행 끝난 시점 기준 다음 실행
    @Scheduled(fixedDelay = POLLING_INTERVAL)
    public void startMonitoring() {
        try {
            List<DisasterMessageDTO> messages = fetchAlerts();
            if (!messages.isEmpty()) {
                log.info("\n=== 새로운 재난문자 {}건이 수신되었습니다 ===", messages.size());
                messages.forEach(this::processMessage);
            } else {
                log.debug("수신된 재난문자가 없습니다.");
            }
        } catch (Exception e) {
            log.error("모니터링 중 오류 발생", e);
        }
    }

    private List<DisasterMessageDTO> fetchAlerts() {
        // BASE_URL 및 service 키 등등 정보를 URL에 넣어서 HttpURLConnection으로 연결
        HttpURLConnection connection = buildRequestUrl();
        if (connection == null) return new ArrayList<>();
        try {
            int responseCode = connection.getResponseCode();
            // responseCode가 200에서 299 사이의 성공 코드라면
            if (responseCode >= 200 && responseCode < 300) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
                String responseText = reader.lines().collect(Collectors.joining("\n"));
                reader.close();
                log.info("API 호출...");
                // BufferedReader를 통해 받은 json 데이터를 자바 객체로 parse
                List<DisasterMessageDTO> newMessage = parseApiResponse(responseText);
                return newMessage;
            } else {
                log.error("API 호출 실패: {}", responseCode);
            }
        } catch (Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage(), e);
        }
        finally {
            connection.disconnect();
        }
        return new ArrayList<>();
    }

    private HttpURLConnection buildRequestUrl() {
        // 당일 재난문자만 받음
        String currentDate = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        try {
            urlBuilder.append("?serviceKey=").append(URLEncoder.encode(SERVICE_KEY, "UTF-8"))
                    .append("&returnType=json")
                    .append("&pageNo=1")
                    .append("&numOfRows=20")
                    .append("&crtDt=").append(currentDate);

            URL url = new URL(urlBuilder.toString());

            // url로 연결 및 get, json, UTF-8 등 설정
            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
            connection.setRequestMethod("GET");
            connection.setRequestProperty("Accept", "application/json");
            connection.setRequestProperty("Accept-Charset", "UTF-8");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            return connection;
        } catch(Exception e) {
            log.error("API 호출 중 오류 발생: {}", e.getMessage(), e);
        }
        return null;
    }

    private List<DisasterMessageDTO> parseApiResponse(String json) {
        ApiResponseDTO response = gson.fromJson(json, ApiResponseDTO.class);
        if ("00".equals(response.getHeader().getResultCode())) {
            // SN 일련번호 업데이트 및 newMessages 생성
            return filterNewMessages(response);
        } else {
            log.error("API 오류: {}", response.getHeader().getErrorMsg());
        }
        return new ArrayList<>();
    }

    private List<DisasterMessageDTO> filterNewMessages(ApiResponseDTO response) {
        List<DisasterMessageDTO> newMessages = new ArrayList<>();
        if(response.getBody() != null) {
            // 일련번호가 마지막으로 저장된 일련번호보다 높은 경우만 newMessages에 저장
            newMessages = response.getBody().stream().filter(message -> Long.parseLong(message.getSN()) > lastMessageSN).collect(Collectors.toList());
        }
        if (!newMessages.isEmpty()) {
            // newMessages에 값이 있다면 lastMessageSN 값을 message.SN의 가장 높은 값(max)로 저장
            lastMessageSN = newMessages.stream().mapToLong(message -> Long.parseLong(message.getSN())).max().getAsLong();
            log.info("마지막 메시지 SN 업데이트: {}", lastMessageSN);
        }
        return newMessages;
    }

    private void processMessage(DisasterMessageDTO message) {
        String title = message.getDST_SE_NM() + " - " + message.getCRT_DT();
        String body = message.getMSG_CN();
        String location = message.getRCPTN_RGN_NM();

        log.info("title = {}, body = {}, location = {}", title, body, location);
        try {
            sendMessageWithRetry(title, body, location);
        } catch (FirebaseMessagingException e) {
            log.error("FCM 메시지 전송 실패", e);
        }
    }

    private void sendMessageWithRetry(String title, String body, String location) throws FirebaseMessagingException {
        int retryCount = 0;
        while (retryCount < 3) {
            try {
                fcmService.sendMessageByTopic(title, body, location);
                log.info("sendMessageTopic 완료");
                return;
            } catch (Exception e) {
                retryCount++;
                log.error("Failed to send message (attempt {}/3)", retryCount, e);
                if (retryCount < 3) waitBeforeRetry();
                else throw e;
            }
        }
    }

    private void waitBeforeRetry() {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
    }
}
