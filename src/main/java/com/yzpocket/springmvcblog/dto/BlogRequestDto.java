package com.yzpocket.springmvcblog.dto;

import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.Date;

//Memo 클래스와 비슷하게 생겼다. 요청에 대하여 매개변수로 받아내는 부분이다. 컨트롤러 생성 후 이것을 작성한다.
@Getter
@Setter
public class BlogRequestDto {
    //private int idx;
    private String title;
    private String author;
    private String contents;
    private Timestamp accessTime;
    private String password;
}