package com.sparta.meeting_platform.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sparta.meeting_platform.dto.FinalResponseDto;
import com.sparta.meeting_platform.service.SocialGoogleService;
import com.sparta.meeting_platform.service.SocialKakaoService;
import com.sparta.meeting_platform.service.SocialNaverService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user/signin")
@Slf4j
public class SocialController {

    private final SocialGoogleService socialGoogleService;
    private final SocialNaverService socialNaverService;
    public final SocialKakaoService socialKakaoService;

    //소셜 구글 로그인
    @GetMapping("/google")
    public ResponseEntity<FinalResponseDto<?>> googleLogin(
            @RequestParam(value = "code") String authCode) throws JsonProcessingException {
        return socialGoogleService.googleLogin(authCode);
    }

    //소셜 카카오 로그인
    @GetMapping("/kakao")
    public ResponseEntity<FinalResponseDto<?>> kakaoLogin(
            @RequestParam(value = "code") String code) throws JsonProcessingException {
        return socialKakaoService.kakaoLogin(code);
    }

    //소결 네이버 로그인
    @GetMapping("/naver")
    public ResponseEntity<FinalResponseDto<?>> naverLogin(@RequestParam String code, @RequestParam String state) {
        return socialNaverService.naverLogin(code, state);
    }
}
