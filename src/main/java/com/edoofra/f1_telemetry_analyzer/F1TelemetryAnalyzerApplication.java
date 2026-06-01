package com.edoofra.f1_telemetry_analyzer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class F1TelemetryAnalyzerApplication {

	public static void main(String[] args) {
		SpringApplication.run(F1TelemetryAnalyzerApplication.class, args);
	}

}
