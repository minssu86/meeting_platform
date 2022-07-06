package com.sparta.meeting_platform.service;

import com.sparta.meeting_platform.domain.Like;
import com.sparta.meeting_platform.domain.Post;
import com.sparta.meeting_platform.domain.User;
import com.sparta.meeting_platform.dto.FinalResponseDto;
import com.sparta.meeting_platform.dto.PostDto.PostDetailsResponseDto;
import com.sparta.meeting_platform.dto.PostDto.PostRequestDto;
import com.sparta.meeting_platform.dto.PostDto.PostResponseDto;
import com.sparta.meeting_platform.dto.PostTestDto;
import com.sparta.meeting_platform.dto.SearchMapDto;
import com.sparta.meeting_platform.dto.user.MyPageDto;
import com.sparta.meeting_platform.repository.LikeRepository;
import com.sparta.meeting_platform.repository.PostRepository;
import com.sparta.meeting_platform.repository.UserRepository;
import com.sparta.meeting_platform.repository.mapping.PostMapping;
import com.sparta.meeting_platform.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final LikeRepository likeRepository;
    private final S3Service s3Service;

    private final MapService mapService;

    //게시글 전체 조회(4개만)
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getPosts(Long userId) throws ParseException {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 조회 실패"), HttpStatus.BAD_REQUEST);
        }
        List<PostResponseDto> postList = new ArrayList<>();
        List<Post> posts = postRepository.findTop4ByOrderByCreatedAtDesc();

        for (Post post : posts) {
            Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getId()).orElse(null);
            Boolean isLike;

            if(like == null){
                isLike = false;
            }else {
                isLike = like.getIsLike();
            }
            PostResponseDto postResponseDto =PostResponseDto.builder()
                    .id(post.getId())
                    .title(post.getTitle())
                    .personnel(post.getPersonnel())
                    .joinCount(1)                       //TODO 수정필요
                    .place(post.getPlace())
                    .postUrl(post.getPostUrls().get(0)) //TODO 수정필요
                    .time(timeCheck(post.getTime()))
                    .avgTemp(50)                      //TODO 수정필요
                    .isLetter(post.getIsLetter())
                    .isLike(isLike)
                    .build();
            postList.add(postResponseDto);
            }

        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 조회 성공", postList), HttpStatus.OK);
    }

    //카테고리별 게시글 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getPostsByCategories(Long userId, List<String> categories) throws ParseException {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 조회 실패"), HttpStatus.BAD_REQUEST);
        }
        List<PostResponseDto> postList = new ArrayList<>();

        for (String category : categories) {
            List<Post> posts = postRepository.findAllByCategories(category);
            if(posts.size() < 1){
                return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글이 없습니다, 다른단어로 조회해주세요"), HttpStatus.BAD_REQUEST);
            }
            for (Post post : posts) {
                Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getId()).orElse(null);
                Boolean isLike;

                if(like == null){
                    isLike = false;
                }else {
                    isLike = like.getIsLike();
                }
                PostResponseDto postResponseDto =PostResponseDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .personnel(post.getPersonnel())
                        .joinCount(1)                       //TODO 수정필요
                        .place(post.getPlace())
                        .postUrl(post.getPostUrls().get(0)) //TODO 수정필요
                        .time(timeCheck(post.getTime()))
                        .avgTemp(50)                      //TODO 수정필요
                        .isLetter(post.getIsLetter())
                        .isLike(isLike)
                        .build();
                postList.add(postResponseDto);
            }
        }

        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 조회 성공", postList), HttpStatus.OK);
    }
    //태그별 게시글 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getPostsByTags(Long userId, List<String> tags) throws ParseException {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 조회 실패"), HttpStatus.BAD_REQUEST);
        }

        List<PostResponseDto> postList = new ArrayList<>();

        for (String tag : tags) {
            List<Post> posts = postRepository.findAllByTags(tag);
            if(posts.size() < 1){
                return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글이 없습니다, 다른단어로 조회해주세요"), HttpStatus.BAD_REQUEST);
            }

            for (Post post : posts) {
                Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getId()).orElse(null);
                Boolean isLike;

                if(like == null){
                    isLike = false;
                }else {
                    isLike = like.getIsLike();
                }
                PostResponseDto postResponseDto =PostResponseDto.builder()
                        .id(post.getId())
                        .title(post.getTitle())
                        .personnel(post.getPersonnel())
                        .joinCount(1)                       //TODO 수정필요
                        .place(post.getPlace())
                        .postUrl(post.getPostUrls().get(0)) //TODO 수정필요
                        .time(timeCheck(post.getTime()))
                        .avgTemp(50)                      //TODO 수정필요
                        .isLetter(post.getIsLetter())
                        .isLike(isLike)
                        .build();
                postList.add(postResponseDto);
            }
        }


        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 조회 성공", postList), HttpStatus.OK);

    }

    //게시글 상세 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>>getPostsDetails(Long postId, Long userId) throws ParseException {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 조회 실패"), HttpStatus.BAD_REQUEST);
        }
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new NullPointerException("존재하지 않는 게시물 입니다.")
        );
        Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getId()).orElse(null);
        Boolean isLike;

        if(like == null){
            isLike = false;
        }else {
            isLike = like.getIsLike();
        }
        List<String> joinPeopleurls = new ArrayList<>(); //TODO 수정필요
        joinPeopleurls.add("test1");
        joinPeopleurls.add("test2");
        List<String> joinPeopleNicknames = new ArrayList<>(); //TODO 수정필요
        joinPeopleNicknames.add("test1");
        joinPeopleNicknames.add("test2");
        PostDetailsResponseDto postDetailsResponseDto =PostDetailsResponseDto.builder()
                .title(post.getTitle())
                .time(timeCheck(post.getTime()))
                .personnel(post.getPersonnel())
                .place(post.getPlace())
                .postUrls(post.getPostUrls())
                .tags(post.getTags())
                .categories(post.getCategories())
                .bungCount(post.getUser().getBungCount())
                .mannerTemp(post.getUser().getMannerTemp())
                .joinPeopleUrl(joinPeopleurls)                //TODO 수정필요
                .joinPeopleNickname(joinPeopleNicknames)           //TODO 수정필요
                .joinCount(1)                       //TODO 수정필요
                .isLetter(post.getIsLetter())
                .isLike(isLike)
                .build();

        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 조회 성공",postDetailsResponseDto), HttpStatus.OK);
    }
    //게시글 삭제
    @Transactional
    public ResponseEntity<FinalResponseDto<?>>deletePost(Long postid, Long userId) {
        Post post = postRepository.findById(postid).orElseThrow(
                () -> new NullPointerException("해당 게시글이 존재하지 않습니다.")
        );

        if (!post.getUser().getId().equals(userId)) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "본인 게시글이 아닙니다."),HttpStatus.BAD_REQUEST);
        } else {
            postRepository.deleteById(postid);
            return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 삭제 성공"), HttpStatus.OK);
        }
    }

    @Transactional
    // 게시글 등록
    public ResponseEntity<FinalResponseDto<?>> createPost(Long userId, PostTestDto requestDto, List<MultipartFile> files) throws Exception {
        User user = userRepository.findById(userId).orElse(null);

        if (user==null) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 개설 실패"), HttpStatus.BAD_REQUEST);
        }

        if(files.isEmpty()){
            requestDto.setPostUrls(null);  // 기본 이미지로 변경 필요
        } else {
            List<String> postUrls = new ArrayList<>();
            for(MultipartFile file : files){
                postUrls.add(s3Service.upload(file));
            }
            requestDto.setPostUrls(postUrls);
        }
        SearchMapDto searchMapDto = mapService.searchLatAndLong(requestDto.getPlace());
        Double longitude = searchMapDto.getLongitude();
        Double latitude = searchMapDto.getLatitude();

        String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
        // WKTReader를 통해 WKT를 실제 타입으로 변환합니다.
        Point point = (Point) new WKTReader().read(pointWKT);

        postRepository.save(new Post(user, requestDto,longitude,latitude,point));
        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 개설 성공"), HttpStatus.OK);
    }

//    public void saveUser() {
//        String name = "momentjin";
//        Double latitude = 32.123;
//        Double longitude = 127.123;
//        String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
//
//        // WKTReader를 통해 WKT를 실제 타입으로 변환합니다.
//        Point point = (Point) new WKTReader().read(pointWKT);
//        User user = new User(name, point);
//        userRepository.save(driverLocation);
//    }

    // 게시글 수정
    @Transactional
    public ResponseEntity<FinalResponseDto<?>> updatePost(Long postId, Long userId, PostRequestDto requestDto, List<MultipartFile> files) {
        Post post = postRepository.findByIdAndUserId(postId, userId).orElse(null);

        if(post == null){
            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 수정 실패"), HttpStatus.BAD_REQUEST);
        }

        if(files.isEmpty()){
            requestDto.setPostUrls(null);  // 기본 이미지로 변경 필요
        } else {
            List<String> postUrls = new ArrayList<>();
            for(MultipartFile file : files){
                postUrls.add(s3Service.upload(file));
            }
            requestDto.setPostUrls(postUrls);
        }
        // DB 업데이트
        post.update(requestDto);

        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 수정 성공"), HttpStatus.OK);

    }

    // 찜한 게시글 전체 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getLikedPosts(Long userId) throws ParseException {
        Optional<User> user = userRepository.findById(userId);

        if (!user.isPresent()) {
            return new ResponseEntity<>(new FinalResponseDto<>(false, "좋아요한 게시글 조회 실패"), HttpStatus.BAD_REQUEST);
        }
        List<PostMapping> posts = likeRepository.findAllByUserIdAndIsLikeTrue(userId);


        List<PostResponseDto> postList = new ArrayList<>();

        for (PostMapping post : posts) {
            Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getPost().getId()).orElseThrow(
                    ()-> new NullPointerException("찜한 게시글이 없습니다.")
            );

            PostResponseDto postResponseDto =PostResponseDto.builder()
                    .id(post.getPost().getId())
                    .title(post.getPost().getTitle())
                    .personnel(post.getPost().getPersonnel())
                    .joinCount(1)                                     //TODO 수정필요
                    .place(post.getPost().getPlace())
                    .postUrl(post.getPost().getPostUrls().get(0))    //TODO 수정필요
                    .time(timeCheck(post.getPost().getTime()))
                    .avgTemp(50)                                  //TODO 수정필요
                    .isLetter(post.getPost().getIsLetter())
                    .isLike(like.getIsLike())
                    .build();
            postList.add(postResponseDto);
        }
        return new ResponseEntity<>(new FinalResponseDto<>(true, "좋아요한 게시글 조회 성공", postList), HttpStatus.OK);
    }

    //나의 번개 페이지 조회
    @Transactional(readOnly = true)
    public ResponseEntity<FinalResponseDto<?>> getMyPage(UserDetailsImpl userDetails) {
        User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(
                ()-> new NullPointerException("해당 유저를 찾을 수 없습니다.")
        );
        MyPageDto myPageDto = new MyPageDto(user.getNickName(),user.getMannerTemp(),user.getProfileUrl(),user.getBungCount());
        return new ResponseEntity<>(new FinalResponseDto<>(true,"나의 번개 페이지 조회 성공", myPageDto), HttpStatus.OK);
    }
    //내 벙글 확인하기
    public ResponseEntity<FinalResponseDto<?>> getMyPagePost(UserDetailsImpl userDetails) throws ParseException {
        User user = userRepository.findById(userDetails.getUser().getId()).orElseThrow(
                ()-> new NullPointerException("해당 유저를 찾을 수 없습니다.")
        );
        Post post = postRepository.findByUserId(user.getId());

        Like like = likeRepository.findByUser_IdAndPost_Id(user.getId(), post.getId()).orElse(null);

        Boolean isLike;

        if(like == null){
            isLike = false;
        }else {
            isLike = like.getIsLike();
        }
        PostResponseDto postResponseDto =PostResponseDto.builder()
                .id(post.getId())
                .title(post.getTitle())
                .personnel(post.getPersonnel())
                .joinCount(1)                       //TODO 수정필요
                .place(post.getPlace())
                .postUrl(post.getPostUrls().get(0)) //TODO 수정필요
                .time(timeCheck(post.getTime()))
                .avgTemp(50)                      //TODO 수정필요
                .isLetter(post.getIsLetter())
                .isLike(isLike)
                .build();

        return new ResponseEntity<>(new FinalResponseDto<>(true,"나의 번개 페이지 조회 성공", postResponseDto), HttpStatus.OK);
    }

    // Time 변환
    public String timeCheck(String time) throws ParseException {
        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(time, inputFormat);
        if(!localDateTime.isAfter(LocalDateTime.now())){
            Duration duration = Duration.between(localDateTime, LocalDateTime.now());
            System.out.println(duration.getSeconds());
            return duration.getSeconds()/60 + "분 경과";
        }
        return localDateTime.getHour() + "시 시작 예정";
    }

    //    //게시글 조회 (제목에 포함된 단어로)
//    public ResponseEntity<FinalResponseDto<?>> getSearch(String keyword, Long userId) throws ParseException {
//        Optional<User> user = userRepository.findById(userId);
//
//        if (!user.isPresent()) {
//            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글 검색 실패"), HttpStatus.BAD_REQUEST);
//        }
//        List<PostResponseDto> postList = new ArrayList<>();
//        List<Post> posts = postRepository.findAllByTitleContainsOrderByCreatedAtDesc(keyword);
//
//        if(posts.size() < 1){
//            return new ResponseEntity<>(new FinalResponseDto<>(false, "게시글이 없습니다, 다른단어로 검색해주세요"), HttpStatus.BAD_REQUEST);
//        }
//
//        for (Post post : posts) {
//            Like like = likeRepository.findByUser_IdAndPost_Id(userId, post.getId()).orElse(null);
//            Boolean isLike;
//
//            if (like == null) {
//                isLike = false;
//            } else {
//                isLike = like.getIsLike();
//            }
//            PostResponseDto postResponseDto = new PostResponseDto(post, isLike, timeCheck(post.getTime()));
//            postList.add(postResponseDto);
//        }
//        return new ResponseEntity<>(new FinalResponseDto<>(true, "게시글 조회 성공", postList), HttpStatus.OK);
//    }

}

