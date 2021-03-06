package com.sparta.meeting_platform.service;

import com.sparta.meeting_platform.domain.Notice;
import com.sparta.meeting_platform.domain.Opinion;
import com.sparta.meeting_platform.domain.User;
import com.sparta.meeting_platform.dto.FinalResponseDto;
import com.sparta.meeting_platform.dto.SettingDto.NoticeRequestDto;
import com.sparta.meeting_platform.dto.SettingDto.NoticeResponseDto;
import com.sparta.meeting_platform.dto.SettingDto.OpinionRequestDto;
import com.sparta.meeting_platform.exception.SettingApiException;
import com.sparta.meeting_platform.exception.UserApiException;
import com.sparta.meeting_platform.repository.NoticeRepository;
import com.sparta.meeting_platform.repository.OpinionRepository;
import com.sparta.meeting_platform.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SettingService {

    private final NoticeRepository noticeRepository;
    private final UserRepository userRepository;
    private final OpinionRepository opinionRepository;

    //공지사항 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getNotice(Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            throw new UserApiException("공지사항 조회 실패");
        }
        List<Notice> foundNotice = noticeRepository.findAllByOrderByIdDesc();
        List<NoticeResponseDto> noticeResponseDtoList = new ArrayList<>();

        for (Notice notice : foundNotice) {
            NoticeResponseDto noticeResponseDto = new NoticeResponseDto(notice);
            noticeResponseDtoList.add(noticeResponseDto);
        }

        return new ResponseEntity<>(new FinalResponseDto<>(true, "공지사항 조회 성공", noticeResponseDtoList, user.get().getIsOwner()), HttpStatus.OK);
    }

    //의견 보내기
    @Transactional
    public ResponseEntity<FinalResponseDto<?>> creatOpinion(OpinionRequestDto requestDto, Long userId) {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            throw new UserApiException("의견 보내기 실패");
        }
        if (requestDto.getMessage() == null) {
            throw new SettingApiException("의견이 없습니다.");
        }
        Opinion opinion = new Opinion(user.get(), requestDto);
        opinionRepository.save(opinion);

        return new ResponseEntity<>(new FinalResponseDto<>(true, "의견 보내기 성공"), HttpStatus.OK);
    }

    //공지사항 작성하기
    @Transactional
    public ResponseEntity<FinalResponseDto<?>> createNotice(Long userId, NoticeRequestDto requestDto) {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            throw new UserApiException("공지 작성 실패");
        }
        LocalDateTime localDateTime = LocalDateTime.now();
        Notice notice = new Notice(requestDto, localDateTime);
        noticeRepository.save(notice);

        return new ResponseEntity<>(new FinalResponseDto<>(true, "공지 작성 성공"), HttpStatus.OK);
    }
}
