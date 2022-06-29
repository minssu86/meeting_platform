package com.sparta.meeting_platform.service;


import com.sparta.meeting_platform.domain.User;
import com.sparta.meeting_platform.dto.FinalResponseDto;
import com.sparta.meeting_platform.dto.user.DuplicateRequestDto;
import com.sparta.meeting_platform.dto.user.LoginRequestDto;
import com.sparta.meeting_platform.dto.user.SignUpRequestDto;
import com.sparta.meeting_platform.repository.UserRepository;
import com.sparta.meeting_platform.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    public ResponseEntity<FinalResponseDto<?>> duplicateUsername(DuplicateRequestDto requestDto) {

        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("중복된 이메일이 존재합니다.");
        }
        return new ResponseEntity<>(new FinalResponseDto<>(true, "사용 가능한 이메일입니다."), HttpStatus.OK);
    }


    public ResponseEntity<FinalResponseDto<?>> signup(SignUpRequestDto requestDto) {

        if (!requestDto.getPassword().equals(requestDto.getPasswordCheck())) {
            throw new IllegalArgumentException("비밀번호가 일치하지 않습니다.");
        }
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new IllegalArgumentException("이메일 중복체크는 필수입니다.");
        }
        requestDto.setPassword(passwordEncoder.encode(requestDto.getPassword()));
        userRepository.save(new User(requestDto));

        return new ResponseEntity<>(new FinalResponseDto<>(true, "회원가입 성공"), HttpStatus.OK);
    }


    public ResponseEntity<FinalResponseDto<?>> login(LoginRequestDto requestDto) {
        User user = userRepository.findByUsername(requestDto.getUsername()).orElseThrow(
                () -> new IllegalArgumentException("해당 유저를 찾을 수 없습니다.")
        );
        if (!passwordEncoder.matches(requestDto.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("비밀번호를 확인해 주세요");
        }
        jwtTokenProvider.createToken(requestDto.getUsername());

        return new ResponseEntity<>(new FinalResponseDto<>
                        (true, "로그인 성공!!", user.getNickName(),user.getMannerTemp()), HttpStatus.OK);
    }
}