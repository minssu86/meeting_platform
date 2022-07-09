package com.sparta.meeting_platform.service;

import com.sparta.meeting_platform.Location;
import com.sparta.meeting_platform.domain.Like;
import com.sparta.meeting_platform.domain.Post;
import com.sparta.meeting_platform.domain.User;
import com.sparta.meeting_platform.dto.MapListDto;
import com.sparta.meeting_platform.dto.MapResponseDto;
import com.sparta.meeting_platform.dto.SearchMapDto;
import com.sparta.meeting_platform.repository.UserRepository;
import com.sparta.meeting_platform.exception.MapApiException;
import com.sparta.meeting_platform.repository.LikeRepository;
import com.sparta.meeting_platform.repository.PostRepository;
import com.sparta.meeting_platform.util.Direction;
import com.sparta.meeting_platform.util.GeometryUtil;
import lombok.RequiredArgsConstructor;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import java.io.IOException;
import java.util.List;

@RequiredArgsConstructor
@Service
public class MapService {
    private final EntityManager em;
    private final PostSearchService postSearchService;
    private final MapSearchService mapSearchService;
    private final UserRepository userRepository;

    //지도탭 입장
    @Transactional(readOnly = true)
    public ResponseEntity<MapResponseDto<?>> readMap(Double latitude, Double longitude, Long userId) {
        checkUser(userId);
        Double distance = 6.0;
        String pointFormat = mapSearchService.searchPointFormat(distance,latitude,longitude);

        Query query = em.createNativeQuery("SELECT * FROM post AS p "
                + "WHERE MBRContains(ST_LINESTRINGFROMTEXT(" + pointFormat + ", p.location)", Post.class); //거리순으로 정렬
        List<Post> posts = query.getResultList();

        if (posts.size() < 1) {
            throw new IllegalArgumentException( distance+"km 내에 모임이 존재하지 않습니다.");
        }
        List<MapListDto> mapListDtos = postSearchService.searchMapPostList(posts, userId);
        return new ResponseEntity<>(new MapResponseDto<>(true, "50km 내에 위치한 모임", mapListDtos), HttpStatus.OK);
    }
    // 순서는 어떻게?
    // 화면에 몇개? 밑 슬라이스에 몇개?


    // 주소 검색 결과
    @Transactional(readOnly = true)
    public ResponseEntity<MapResponseDto<?>> searchMap(String address, Long userId) throws IOException, ParseException{
        checkUser(userId);
        SearchMapDto searchMapDto = mapSearchService.findLatAndLong(address);
        Double distance = 6.0;
        String pointFormat
                = mapSearchService.searchPointFormat(distance,searchMapDto.getLatitude(),searchMapDto.getLongitude());

        Query query = em.createNativeQuery("SELECT * FROM post AS p "
                + "WHERE MBRContains(ST_DISTANCE_SPHERE(" + pointFormat + ", p.location)"
                + "AS distance FROM Post ORDER BY distance", Post.class); //거리순으로 정렬
        List<Post> posts = query.getResultList();

        if (posts.size() < 1) {
            throw new IllegalArgumentException(distance+"km 내에 모임이 존재하지 않습니다.");
        }
        List<MapListDto> mapListDtos = postSearchService.searchMapPostList(posts, userId);
        return new ResponseEntity<>(new MapResponseDto<>(true, "50km 내에 위치한 모임", mapListDtos), HttpStatus.OK);
    }//거리순


    //지도 세부 설정 검색
    @Transactional(readOnly = true)
    public ResponseEntity<MapResponseDto<?>> detailsMap(List<String> categories, int personnel, Double distance,
                                                        Double latitude, Double longitude, Long userId){
        checkUser(userId);
        String pointFormat = mapSearchService.searchPointFormat(distance,latitude,longitude);
        String mergeList = postSearchService.categoryOrTagListMergeString(categories);

        Query query = em.createNativeQuery("SELECT * FROM post AS p "
                        + "WHERE MBRContains(ST_LINESTRINGFROMTEXT(" + pointFormat + ", p.location)"
                        + "AND personnel <= " + personnel + " AND p.id in (select u.post_id from post_categories u"
                        + " WHERE u.category in (" + mergeList + "))", Post.class);
        List<Post> posts = query.getResultList();

        List<MapListDto> mapListDtos = postSearchService.searchMapPostList(posts, userId);
        return new ResponseEntity<>(new MapResponseDto<>(true, "세부 조회 성공!!", mapListDtos), HttpStatus.OK);
    }//거리순
    //디폴트 10km
    //

    public User checkUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new NullPointerException("해당 유저를 찾을 수 없습니다."));
        return user;
    }

//    //위도 경도 찾아 오기 함수
//    public SearchMapDto findLatAndLong(String location) throws IOException, ParseException {
//
//        URL obj;
//
//        String geocodingUrl = "http://dapi.kakao.com/v2/local/search/address.json?query=";
//        //인코딩한 String을 넘겨야 원하는 데이터를 받을 수 있다.
//        String address = URLEncoder.encode(location, "UTF-8");
//
//        obj = new URL(geocodingUrl + address);
//
//        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
//        String auth = "KakaoAK " + geocoding;
//        con.setRequestMethod("GET");
//        con.setRequestProperty("Authorization", auth);
//
//        con.setRequestProperty("content-type", "application/json");
//        con.setDoOutput(true);
//        con.setUseCaches(false);
//        con.setDefaultUseCaches(false);
//
//        Charset charset = Charset.forName("UTF-8");
//        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));
//
//        String inputLine;
//        StringBuffer response = new StringBuffer();
//
//        while ((inputLine = in.readLine()) != null) {
//            response.append(inputLine);
//        }
//
//        System.out.println(response);
//
//        JSONParser jsonParser = new JSONParser();
//        JSONObject jsonObject = (JSONObject) jsonParser.parse(response.toString());
//        System.out.println("-----------------------------------------------");
//        JSONArray documents = (JSONArray) jsonObject.get("documents");
//        System.out.println(documents);
//        JSONObject thisAddress = (JSONObject) documents.get(0);
//        String longitude = (String) thisAddress.get("x");
//        String latitude = (String) thisAddress.get("y");
//        double longi = Double.parseDouble(longitude);
//        double lati = Double.parseDouble(latitude);
//
//        System.out.println(longi);
//        System.out.println(lati);
//        return new SearchMapDto(longi, lati);
//    }

//    //특정 반경 내에 위치한 모임 찾아오기 메소드
//    public List<Post> findPostsWithinDistance(Double latitude, Double longitude, Double distance) {
//        Location northEast = GeometryUtil
//                .calculate(latitude, longitude, distance, Direction.NORTHEAST.getBearing());
//        Location southWest = GeometryUtil
//                .calculate(latitude, longitude, distance, Direction.SOUTHWEST.getBearing());
//
//        double x1 = northEast.getLatitude();
//        double y1 = northEast.getLongitude();
//        double x2 = southWest.getLatitude();
//        double y2 = southWest.getLongitude();
//
//        String pointFormat = String.format("'LINESTRING(%f %f, %f %f)')", x1, y1, x2, y2);
//        Query query = em.createNativeQuery("SELECT * FROM post AS p "
//                + "WHERE MBRContains(ST_LINESTRINGFROMTEXT(" + pointFormat + ", p.location)", Post.class);
////                .setMaxResults(3);
//
//        return query.getResultList();
//    }

//    double theta = longitude - post.getLongitude();
//    double dist = Math.sin(deg2rad(latitude)) * Math.sin(deg2rad(post.getLatitude()))
//            + Math.cos(deg2rad(latitude)) * Math.cos(deg2rad(post.getLatitude())) * Math.cos(deg2rad(theta));
//
//    dist = Math.acos(dist);
//    dist = rad2deg(dist);
//    dist = dist * 60 * 1.1515 * 1.609344;//
//    public double deg2rad(double deg) {
//        return (deg * Math.PI / 180.0);
//    }
//
//    public double rad2deg(double rad) {
//        return (rad * 180 / Math.PI);
//    }

    //pointFormat 구하기
//    public String searchPointFormat(Double distance, Double latitude, Double longitude){
//        Location northEast = GeometryUtil
//                .calculate(latitude, longitude, distance, Direction.NORTHEAST.getBearing());
//        Location southWest = GeometryUtil
//                .calculate(latitude, longitude, distance, Direction.SOUTHWEST.getBearing());
//
//        double x1 = northEast.getLatitude();
//        double y1 = northEast.getLongitude();
//        double x2 = southWest.getLatitude();
//        double y2 = southWest.getLongitude();
//        String pointFormat = String.format("'LINESTRING(%f %f, %f %f)')", x1, y1, x2, y2);
//        return pointFormat;
//    }

//    //카페고리및태그 리스트->스트링 변환
//    public String categoryOrTagListMergeString (List<String> categoryOrTagList){
//        String mergeList = "";
//        for (String string : categoryOrTagList) {
//            mergeList += "'" + string + "',";
//        }
//        mergeList = mergeList.substring(0, mergeList.length() - 1);
//        return mergeList;
//    }
//
//    //postlist 찾기
//    public String timeCheck(String time) throws java.text.ParseException {
//        DateTimeFormatter inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
//        LocalDateTime localDateTime = LocalDateTime.parse(time, inputFormat);
//        if (!localDateTime.isAfter(LocalDateTime.now())) {
//            Duration duration = Duration.between(localDateTime, LocalDateTime.now());
//            System.out.println(duration.getSeconds());
//            return duration.getSeconds() / 60 + "분 경과";
//        }
//        return localDateTime.getHour() + "시 시작 예정";
//    }

}
