package com.yzpocket.springmvcblog.dto;


import com.fasterxml.jackson.annotation.JsonFormat;
import com.yzpocket.springmvcblog.entity.Blog;
import lombok.Getter;
import lombok.Setter;

import java.sql.Timestamp;

//Blog 클래스와 비슷하게 생겼다. 요청에대한 반환 부분이다. 컨트롤러 생성 후 이것을 작성한다.
@Getter
@Setter
public class BlogResponseDto {
    private Long idx;
    private String title;
    private String author;
    private String contents;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone = "GMT+9")
    private Timestamp accesstime;
    private String password;

    public BlogResponseDto(Blog blog) {
        this.idx = blog.getIdx();
        this.title = blog.getTitle();
        this.author = blog.getAuthor();
        this.contents = blog.getContents();
        this.accesstime = blog.getAccesstime();
        this.password = blog.getPassword();

    }

    public BlogResponseDto(String title, String author, String contents, Timestamp accesstime) {
        this.title = title;
        this.author = author;
        this.contents = contents;
        this.accesstime = accesstime;
    }
    public BlogResponseDto(Long idx, String title, String author, String contents, Timestamp accesstime, String password) {
        this.idx = idx;
        this.title = title;
        this.author = author;
        this.contents = contents;
        this.accesstime = accesstime;
        this.password = password;
    }
}