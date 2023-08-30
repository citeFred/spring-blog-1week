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
        String sql = "SELECT title, author, contents, accesstime FROM blog ORDER BY accesstime DESC"; //내림차순 정렬 추가

        return jdbcTemplate.query(sql, new RowMapper<BlogResponseDto>() {
            @Override
            public BlogResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                //Long idx = rs.getLong("idx");
                String title = rs.getString("title");
                String author = rs.getString("author");
                String contents = rs.getString("contents");
                Timestamp accesstime = rs.getTimestamp("accesstime"); // 'accesstime' 컬럼이 datetime 유형이므로 getTimestamp 메서드를 사용

                //String password = rs.getString("password");
                return new BlogResponseDto(title, author, contents, accesstime); //아하! ResDto에서 메소드 오버로드로 이 4개 항목만 갖는 생성자를 만들어주었다.
                                                                                //[질문] 그런데 프론트엔드가 이것을 받아서 json을 {autho} 이렇게 VIEW에 뿌리려고 쓸텐데,
                                                                                //idx랑 password를 궂이 안보이게 null로 보내야 할까?? 아니면 그냥 다보내는게 나은지,
                                                                                //그런데 또 생각해보면 갑자기 뭐가 또 필요하다! 라는 요청이 오면 또 생성자를 바꿔야 된다고?
                                                                                //그러느니 그냥 다보내는게 좋지 않을까?? 협업에선? 또 수정해달라고 또 요청하는거도 힘들잖아..
                                                                                //내가 예전에 뷰페이지를 직접 만들땐 모든 데이터가 있으면 뭐 골라쓰면 되니까 문제가 안됬다.
                                                                                //없으면 또 뭘또 추가해야되나 찾아다니기 바빳고, 실제로 어떤지 궁금하다.

            }
        });
    }

    //[3]특정 게시글 내용 조회 - READ
    @GetMapping("/posts/{idx}")
    public BlogResponseDto getBlog(@PathVariable Long idx, String password) {
        // DB 조회
        String sql = "SELECT title, author, accesstime, contents FROM blog WHERE idx = ?";

        return jdbcTemplate.queryForObject(sql, new Object[]{idx}, new RowMapper<BlogResponseDto>() {
            @Override
            public BlogResponseDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                String title = rs.getString("title");
                String author = rs.getString("author");
                Timestamp accesstime = rs.getTimestamp("accesstime"); // 'accesstime' 컬럼이 datetime 유형이므로 getTimestamp 메서드를 사용
                String contents = rs.getString("contents");

                return new BlogResponseDto(title, author, contents, accesstime); //ResDto에서 생성자 메소드 오버로드로 이 4개 항목만 갖는 생성자를 만들어주었다.
            }
        });
    }

    //[4]특정 게시글 수정 - UPDATE
    @PutMapping("/posts/{idx}")
    public BlogResponseDto updateBlog(@PathVariable Long idx, @RequestParam String password, @RequestBody BlogRequestDto requestDto) {
        // 선택 블로그가 DB에 존재하는지 확인 + 암호 일치 확인
        Blog blog = findByIdxPass(idx, password);
        if(blog != null) {
            // blog 내용 수정
            String sql = "UPDATE blog SET title = ?, author = ?,contents = ? WHERE idx = ?";
            jdbcTemplate.update(sql, requestDto.getTitle(), requestDto.getAuthor(), requestDto.getContents(), idx);

            return getBlog(idx, password); //수정된 글을 바로 반환 할 수 있도록 Dto 객체로 반환하도록 선언부 반환 타입 수정, 반환값 수정
        } else {
            throw new IllegalArgumentException("선택한 게시글은 존재하지 않습니다.");
        }
    }

    //[5]특정 게시글 삭제 - DELETE
    @DeleteMapping("/posts/{idx}")
    public String deleteBlog(@PathVariable Long idx, @RequestParam String password) {
        // 해당 블로그가 DB에 존재하는지 확인
        Blog blog = findByIdxPass(idx, password);
        if (blog != null) {
            // 게시글 삭제
            String sql = "DELETE FROM blog WHERE idx = ?";
            jdbcTemplate.update(sql, idx);

            return idx+"번 글이 삭제되었습니다.";
        } else {
            throw new IllegalArgumentException("선택한 글은 존재하지 않습니다.");
        }
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
                    throw new IllegalArgumentException("비밀번호가 일치하지 않습니다."); // 예외 던지기
                    //ChatGPT를 통한 해당 부분의 트러블 슈팅 코드리뷰. <<-------- 자바의 정석 예외 부분을 좀 학습해봐야겠다!
                    //만약 Blog 객체를 반환하면서 동시에 비밀번호 비교 로직도 처리하려면,
                    // 비밀번호 비교에 실패한 경우 null 대신에 예외를 던지는 것이 더 명확!!!!!
                //    이렇게 수정하면 findByIdxPass 메소드가 Blog 객체를 반환하면서
                //    동시에 비밀번호가 일치하지 않으면 예외를 던지도록 되어 있습니다.
                //    이렇게 하면 예외를 처리할 수 있는 부분에서 오류를 빠르게 감지하고 처리할 수 있습니다.
                }
            } else {
                return null;
            }
        }, idx);
    }

    //생각되고있는 문제
    //현재 코드에서는 비밀번호가 URL 경로(@PathVariable)에 노출되고 있다. --------- @RequestParam 으로 해결
    //해결 할 방안은?
    //1] HTTP POST 요청: 비밀번호를 전송할 때는 HTTP POST 요청을 사용하는 것이 좋다.
    //2] HTTPS 사용: HTTPS를 통해 통신하는 것이 중요하다.
    //3] Request Body에 비밀번호 포함: 비밀번호는 요청의 바디(body)에 포함시켜버리기
    //-->
    //비밀번호와 함께 게시글을 수정하는 것은 일반적으로 POST 메소드보다는 PUT 메소드로 수행하는 것이 RESTful한 방식이다라는 ChatGPT의 평가.

    // ---------------- [질문] 음.. 예전에 Spring Security 사용했었는데 기억이 가물가물하다.
    // 내 레퍼런스 코드를 살펴봐야겠다. 그런데 Maven 빌드 환경이었고 JDK11이었기 때문에 그런 버전들좀 확인하고 라이브러리 찾아봐야겠다.
    // 일단 암호화의 전반적인 개념 자체는
    // 엄청 어려운건 아니고 그 주입하는 의존성이 뭔지랑, Hash로 변환되는 임시 난수같은것?
    // 여기 저장되고 또 해석할때도 반대로 난수와 난수끼리 맞는지 알아서 판단해주는 그런게 있다.
    // 그걸 사용하면 DB에 저장 할 때 그 난수를 ASDL@(1a88d 이런것을 저장한다. 반대로 이렇게 암호 비교 같은 경우
    // DB에서 빼오는 난수를 디코딩해주는 함수가 또 있다. 뭐 깊은 내용은 그 라이브러리 개발자가 알겠지만
    // 전체적인 흐름은 그렇다는 것이다. 결국 나는 그 라이브러리를 의존성 잘 설치하고 임포트하고 불러오고
    // 내가 받는 실질 값인 JSON 형태넘어오는 암호 변수를 잘 시큐리티 함수에 전달하고 그렇게 하면 된다.

                                        // 아무튼, 당장 좀 급하게 ChatGPT한테 물어보니 아래 같은걸 쓰는데
    // <<------- 비밀번호 해싱을 사용하려했는데 Dependency 주입에 문제가 있는 것 같다. 당장엔 오류가 나타난다.
    // https://mvnrepository.com/artifact/org.mindrot.bcrypt/bcrypt/0.3

    //private boolean isPasswordCorrect(String inputPassword, String storedPasswordHash) {
    //    // 여기서는 저장된 비밀번호가 해싱되어 있다면,
    //
    //    return BCrypt.checkpw(inputPassword, storedPasswordHash);
    //}
}