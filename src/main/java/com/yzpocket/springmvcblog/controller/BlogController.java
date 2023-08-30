package com.yzpocket.springmvcblog.controller;


import com.yzpocket.springmvcblog.dto.BlogRequestDto;
import com.yzpocket.springmvcblog.dto.BlogResponseDto;
import com.yzpocket.springmvcblog.entity.Blog;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;

import java.sql.*;
import java.util.List;

@RestController
@RequestMapping("/api")
public class BlogController {
    //--------JDBC DB 커넥션 부분---------
    private final JdbcTemplate jdbcTemplate;

    public BlogController(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    //[1]게시글 작성 - CREATE
    @PostMapping("/posts")
    public BlogResponseDto postBlog(@RequestBody BlogRequestDto requestDto) {
        // RequestDto -> Entity
        Blog blog = new Blog(requestDto);

        // DB 저장
        KeyHolder keyHolder = new GeneratedKeyHolder(); // 기본 키를 반환받기 위한 객체

        //String sql DML문장이 실행됨
        String sql = "INSERT INTO blog (title, author, contents, password) VALUES (?, ?, ?, ?)"; // <--- title, author, contents, password 각각 동적으로 아래 setString으로 결정하게함
        jdbcTemplate.update(con -> {
                    PreparedStatement preparedStatement = con.prepareStatement(sql,
                            Statement.RETURN_GENERATED_KEYS);
                    //idx 자동 증가
                    preparedStatement.setString(1, blog.getTitle()); // <--- 이것이 title
                    preparedStatement.setString(2, blog.getAuthor()); // <--- 이것이 author
                    preparedStatement.setString(3, blog.getContents()); // <--- 이것이 contents
                    preparedStatement.setString(4, blog.getPassword()); // <--- 이것이 password
                    return preparedStatement;
                },
                keyHolder);

        // DB Insert 후 받아온 기본키 확인
        Long idx = keyHolder.getKey().longValue();
        blog.setIdx(idx);

        // Entity -> ResponseDto
        BlogResponseDto blogResponseDto = new BlogResponseDto(blog);

        return blogResponseDto;
    }

    //[2]전체 게시글 목록 조회 - READ
    @GetMapping("/posts")
    public List<BlogResponseDto> getBlogs() {
        // DB 조회
        String sql = "SELECT * FROM blog";

        return jdbcTemplate.query(sql, new RowMapper<BlogResponseDto>() {
            @Override
            public BlogResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                Long idx = rs.getLong("idx");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String contents = rs.getString("contents");
                Timestamp accesstime = rs.getTimestamp("accesstime"); // 'accesstime' 컬럼이 datetime 유형이므로 getTimestamp 메서드를 사용

                String password = rs.getString("password");
                return new BlogResponseDto(idx, title, author, contents, accesstime, password);
            }
        });
    }
}