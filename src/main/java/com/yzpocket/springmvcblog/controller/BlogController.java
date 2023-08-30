package com.yzpocket.springmvcblog.controller;


import com.yzpocket.springmvcblog.dto.BlogRequestDto;
import com.yzpocket.springmvcblog.dto.BlogResponseDto;
import com.yzpocket.springmvcblog.entity.Blog;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.web.bind.annotation.*;
//import org.mindrot.bcrypt.BCrypt; // BCrypt 패키지명 수정

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

    //[3]특정 게시글 내용 조회 - READ


    //[4]특정 게시글 수정 - UPDATE
    @PutMapping("/posts/{idx}/{password}")
    public Long updateBlog(@PathVariable Long idx, @PathVariable String password, @RequestBody BlogRequestDto requestDto) {
        // 선택 블로그가 DB에 존재하는지 확인 (중복되서 아래 확인 메소드를 따로 만듬)
        Blog blog = findByIdxPass(idx, password);
        if(blog != null) {
            // blog 내용 수정
            String sql = "UPDATE blog SET title = ?, author = ?,contents = ? WHERE idx = ?";
            jdbcTemplate.update(sql, requestDto.getTitle(), requestDto.getAuthor(), requestDto.getContents(), idx);

            return idx;
        } else {
            throw new IllegalArgumentException("선택한 메모는 존재하지 않습니다.");
        }
    }

    //[5]특정 게시글 삭제 - DELETE
    @DeleteMapping("/posts/{idx}")
    public Long deleteBlog(@PathVariable Long idx) {
        // 해당 메모가 DB에 존재하는지 확인 (중복되서 아래 존재 확인 메소드를 따로 만듬)
        Blog blog = findByIdx(idx);
        if (blog != null) {
            // 게시글 삭제
            String sql = "DELETE FROM blog WHERE idx = ?";
            jdbcTemplate.update(sql, idx);

            return idx;
        } else {
            throw new IllegalArgumentException("선택한 글은 존재하지 않습니다.");
        }
    }


    //---------------JDBC 블로그 찾기 추가--------------
    private Blog findByIdx(Long idx) {
        // DB 조회
        String sql = "SELECT * FROM blog WHERE idx = ?";

        return jdbcTemplate.query(sql, resultSet -> {
            if (resultSet.next()) {
                Blog blog = new Blog();
                blog.setTitle(resultSet.getString("title"));
                blog.setAuthor(resultSet.getString("author"));
                blog.setAccesstime(Timestamp.valueOf(resultSet.getString("accesstime")));
                blog.setContents(resultSet.getString("contents"));
                return blog;
            } else {
                return null;
            }
        }, idx);
    }

    //---------------JDBC 블로그 선택 및 비밀번호 확인 추가--------------
    private Blog findByIdxPass(Long idx, String password) {
        // DB 조회
        String sql = "SELECT * FROM blog WHERE idx = ?";

        return jdbcTemplate.query(sql, resultSet -> {
            if (resultSet.next()) {
                Blog blog = new Blog();
                blog.setTitle(resultSet.getString("title"));
                blog.setAuthor(resultSet.getString("author"));
                blog.setAccesstime(Timestamp.valueOf(resultSet.getString("accesstime")));
                blog.setContents(resultSet.getString("contents"));

                // 데이터베이스에서 저장된 비밀번호 가져오기
                String storedPassword = resultSet.getString("password");

                // 저장된 비밀번호와 입력된 비밀번호 비교
                if (storedPassword.equals(password)) {
                    return blog;
                } else {
                    return null;
                }
            } else {
                return null;
            }
        }, idx);
    }

    //생각되고있는 문제
    //현재 코드에서는 비밀번호가 URL 경로(@PathVariable)에 노출되고 있다.
    //해결 할 방안은?
    //1] HTTP POST 요청: 비밀번호를 전송할 때는 HTTP POST 요청을 사용하는 것이 좋다.
    //2] HTTPS 사용: HTTPS를 통해 통신하는 것이 중요하다.
    //3] Request Body에 비밀번호 포함: 비밀번호는 요청의 바디(body)에 포함시켜버리기
    //-->
    //비밀번호와 함께 게시글을 수정하는 것은 일반적으로 POST 메소드보다는 PUT 메소드로 수행하는 것이 RESTful한 방식이다라는 ChatGPT의 평가.

    //// 비밀번호 비교 메소드 //
    // <<------- 비밀번호 해싱을 사용하려했는데 Dependency 주입에 문제가 있는 것 같다.
    // https://mvnrepository.com/artifact/org.mindrot.bcrypt/bcrypt/0.3
    // build.gradle에 추가했는데 버전등 어떻게 해결해야 할까.

    //private boolean isPasswordCorrect(String inputPassword, String storedPasswordHash) {
    //    // 여기서는 저장된 비밀번호가 해싱되어 있다면,
    //
    //    return BCrypt.checkpw(inputPassword, storedPasswordHash);
    //}
}