package com.lehangajanayake.speechapp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.lehangajanayake.speechapp.dto.GlobalStatsResponse;
import com.lehangajanayake.speechapp.service.StatsService;

@RestController
@RequestMapping("/api/v1/global")
public class GlobalController {

	private final StatsService statsService;

	public GlobalController(StatsService statsService) {
		this.statsService = statsService;
	}

	@GetMapping("/stats")
	public ResponseEntity<GlobalStatsResponse> stats() {
		return ResponseEntity.ok(new GlobalStatsResponse(
				statsService.getInputTokens(),
				statsService.getOutputTokens()));
	}
}
