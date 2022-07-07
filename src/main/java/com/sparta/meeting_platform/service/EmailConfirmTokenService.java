package com.sparta.meeting_platform.service;

import com.sparta.meeting_platform.domain.EmailToken;
import com.sparta.meeting_platform.repository.EmailConfirmTokenRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.time.LocalDateTime;
import java.util.Optional;

@RequiredArgsConstructor
@Service
public class EmailConfirmTokenService {

    private final EmailConfirmTokenRepository emailConfirmTokenRepository;
    private final JavaMailSender javaMailSender;

    public void createEmailConfirmationToken(String receiverEmail) throws MessagingException {

//      인증 Token 정보 DB 저장
        EmailToken emailToken = EmailToken.createEmailConfirmToken(receiverEmail);
        emailConfirmTokenRepository.save(emailToken);

//      Mail Message 생성
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, "utf-8");
        helper.setTo(receiverEmail); //받는사람
        helper.setSubject("벙글! 회원가입 이메일 인증"); //메일제목
        helper.setText("인증 링크 : "+"<a href=" +"'http://localhost:8080/confirmEmail?token=" + emailToken.getId()+"'>"+"인증 하기"+"</a>", true); //ture넣을경우 html

        javaMailSender.send(mimeMessage);

    }


    public EmailToken findByIdAndExpirationDateAfterAndExpired(String confirmationTokenId) {
        Optional<EmailToken> confirmationToken = emailConfirmTokenRepository.findByIdAndExpirationDateAfterAndExpired(confirmationTokenId, LocalDateTime.now(), false);
        return confirmationToken.orElseThrow(() -> new BadCredentialsException("Token Not Found"));
    }

}