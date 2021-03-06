package com.sparta.meeting_platform.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.sparta.meeting_platform.config.GoogleConfig;
import com.sparta.meeting_platform.domain.User;
import com.sparta.meeting_platform.domain.UserRoleEnum;
import com.sparta.meeting_platform.dto.FinalResponseDto;
import com.sparta.meeting_platform.dto.GoogleDto.GoogleLoginDto;
import com.sparta.meeting_platform.dto.GoogleDto.GoogleLoginRequestDto;
import com.sparta.meeting_platform.dto.GoogleDto.GoogleLoginResponseDto;
import com.sparta.meeting_platform.repository.UserRepository;
import com.sparta.meeting_platform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.UUID;


@RequiredArgsConstructor
@Service
public class SocialGoogleService {

    private final UserRepository userRepository;
    private final GoogleConfig googleConfig;
    private final PasswordEncoder passwordEncoder;
    private final UserRoleCheckService userRoleCheckService;
    private final UserService userService;

    @Transactional
    public ResponseEntity<FinalResponseDto<?>> googleLogin
            (String authCode) throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        // 1. "????????????" ??? "????????? ??????" ??????
        // 2. ???????????? ?????? API ??????
        // 3. ??????ID??? ???????????? ??????
        // 4. ?????? ????????? ??????
        // 5. response Header??? JWT ?????? ??????


        // Gooogle Oauth Access Token ????????? Dto
        GoogleLoginRequestDto googleLoginRequestDto = GoogleLoginRequestDto.builder()
                .clientId(googleConfig.getGoogleClientId())     // API Console Credentials page ?????? ????????? ??????????????? ID
                .clientSecret(googleConfig.getGoogleSecret())       // Credentials page ?????? ????????? API Console ??????????????? ?????? ????????????
                .code(authCode)     // ?????? ???????????? ????????? ?????? ??????
                .redirectUri(googleConfig.getGoogleRedirectUri())       // ????????? client_id??? API ConsoleCredentials page ?????? ??????????????? ????????? ???????????? URI??? ??????
                .grantType("authorization_code")        // OAuth 2.0 ????????? ????????? ?????? ??? ?????? ?????? authorization_code??? ??????
                .build();

        // Http Header ??????
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<GoogleLoginRequestDto> httpRequestEntity = new HttpEntity<>(googleLoginRequestDto, headers);
        ResponseEntity<String> apiResponseJson = restTemplate.postForEntity
                (googleConfig.getGoogleAuthUrl() + "/token", httpRequestEntity, String.class);

        // ObjectMapper??? ?????? String to Object??? ??????
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL); // NULL??? ?????? ?????? ????????????(NULL??? ????????? ??????)
        GoogleLoginResponseDto googleLoginResponseDto
                = objectMapper.readValue(apiResponseJson.getBody(), new TypeReference<GoogleLoginResponseDto>() {
        });

        // ???????????? ????????? JWT Token?????? ???????????? ??????, Id_Token??? ?????? ????????????.
        String jwtToken = googleLoginResponseDto.getIdToken();

        //======================= 1??????(????????? ?????? ??????) ??? =======================

        // JWT Token??? ????????? JWT ????????? ????????? ?????? ??????
        String requestUrl = UriComponentsBuilder.fromHttpUrl
                (googleConfig.getGoogleAuthUrl() + "/tokeninfo").queryParam("id_token", jwtToken).toUriString();

        String resultJson = restTemplate.getForObject(requestUrl, String.class);

        GoogleLoginDto userInfoDto = new GoogleLoginDto();
        if (resultJson != null) {
            userInfoDto = objectMapper.readValue(resultJson, new TypeReference<GoogleLoginDto>() {
            });
        }

        // 3??? ????????????
        // ????????? ??????
        int mannerTemp = userRoleCheckService.userResignCheck(userInfoDto.getEmail());
        User user = userRepository.findByUsername(userInfoDto.getEmail()).orElse(null);

        if (user == null) {
            String username = userInfoDto.getEmail(); // username: google ID(email)
            String nickName = userInfoDto.getName();
            String password = UUID.randomUUID().toString(); // password: random UUID
            String encodedPassword = passwordEncoder.encode(password);
            String profileImage = userInfoDto.getPicture(); // profileImage: google profile image
            LocalDateTime createdAt = LocalDateTime.now();
            String googleId = userInfoDto.getSub(); // ?????? ?????????
            user = User.builder()
                    .username(username)
                    .nickName(nickName)
                    .password(encodedPassword)
                    .profileUrl(profileImage)
                    .createdAt(createdAt)
                    .googleId(googleId)
                    .mannerTemp(mannerTemp)
                    .isOwner(false)
                    .role(UserRoleEnum.USER)
                    .build();
            userRepository.save(user);
        }

        // 4??? ?????? ????????? ??????
        UserDetailsImpl userDetails = new UserDetailsImpl(user);
        Authentication authentication =
                new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);

        // User ?????? ??????
        userRoleCheckService.userRoleCheck(user);

        // 5??? response Header??? JWT ?????? ??????
        userService.accessAndRefreshTokenProcess(user.getUsername());

        return new ResponseEntity<>(new FinalResponseDto<>
                (true, "????????? ??????!!", user), HttpStatus.OK);
    }

}
