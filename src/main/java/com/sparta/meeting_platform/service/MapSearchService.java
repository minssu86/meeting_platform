package com.sparta.meeting_platform.service;


import com.sparta.meeting_platform.dto.MapDto.SearchMapDto;
import com.sparta.meeting_platform.exception.MapApiException;
import lombok.RequiredArgsConstructor;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.WKTReader;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.Charset;

@RequiredArgsConstructor
@Service
public class MapSearchService {

    @Value("${geocoding}")
    private String geocoding;

    // point 구하기
    public Point makePoint(Double longitude, Double latitude) throws org.locationtech.jts.io.ParseException {
        String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
        // WKTReader를 통해 WKT를 실제 타입으로 변환합니다.
        return (Point) new WKTReader().read(pointWKT);
    }

    //위도 경도 찾아 오기 함수
    public SearchMapDto findLatAndLong(String location) throws IOException, ParseException {

        URL obj;

        String geocodingUrl = "http://dapi.kakao.com/v2/local/search/address.json?query=";
        //인코딩한 String을 넘겨야 원하는 데이터를 받을 수 있다.
        String address = URLEncoder.encode(location, "UTF-8");

        obj = new URL(geocodingUrl + address);

        HttpURLConnection con = (HttpURLConnection) obj.openConnection();
        String auth = "KakaoAK " + geocoding;
        con.setRequestMethod("GET");
        con.setRequestProperty("Authorization", auth);

        con.setRequestProperty("content-type", "application/json");
        con.setDoOutput(true);
        con.setUseCaches(false);
        con.setDefaultUseCaches(false);

        Charset charset = Charset.forName("UTF-8");
        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charset));

        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }

        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject jsonObject = (JSONObject) jsonParser.parse(response.toString());
            JSONArray documents = (JSONArray) jsonObject.get("documents");
            JSONObject thisAddress = (JSONObject) documents.get(0);
            String longitude = (String) thisAddress.get("x");
            String latitude = (String) thisAddress.get("y");
            double longi = Double.parseDouble(longitude);
            double lati = Double.parseDouble(latitude);

            return new SearchMapDto(longi,lati);
        } catch (IndexOutOfBoundsException e){
            throw new MapApiException("잘못된 주소값입니다.");
        }

    }
}
