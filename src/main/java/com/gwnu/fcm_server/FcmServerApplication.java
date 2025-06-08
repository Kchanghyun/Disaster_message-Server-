package com.gwnu.fcm_server;

import com.google.firebase.messaging.FirebaseMessagingException;
import com.gwnu.fcm_server.Controller.FcmController;
import lombok.RequiredArgsConstructor;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling // 스케줄링 기능 활성화
@RequiredArgsConstructor
public class FcmServerApplication implements CommandLineRunner {

	private final Server server; // Server 클래스 주입

	public static void main(String[] args) {
		// 애플리케이션 실행을 멈추지 않기 위해 쓰레드 사용
		SpringApplication.run(FcmServerApplication.class, args);
	}

	@Override
	public void run(String... args) {

		new Thread(() -> {
			try {
				server.startMonitoring();
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass())
						.error("모니터링 중 오류 발생", e);
			}
		}).start();
	}
}
