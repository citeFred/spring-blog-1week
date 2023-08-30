package com.yzpocket.springmvcblog.entity;

// 이부분, 예전에 했었던 MemoVO 이거랑 개념이 살짝 다르다
// DTO는 계층간 데이터 전송&이동을 위해 사용되는 객체 (Data Transfer Object)
// - 순수 자바 클래스인데 데이터이동을 위해 사용되는 것이구나!
// -- 서버 계층간? 계층은뭘까 레이어드 아키텍쳐
// VO는 값을 갖는 순수한 도메인
// Entity는 이를 DB테이블과 매핑하는객체. (DTO로 변환되서 이동해야한다?)
// 잘이해가 안된다. 좀 구현하고 찾아보도록하자.
// https://youtu.be/J_Dr6R0Ov8E?si=yiyzhHaur2uHfzNF << 테코톡 우아한테크 라흐의 DTO vs VO

// DTO 클래스 명칭은 RequestDTO, ResponseDTO 처럼 한다.
import com.yzpocket.springmvcblog.dto.BlogRequestDto;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
public class Blog {
    private Long idx; // 글번호
    private String title; //글제목
    private String author; //작성자
    private String contents; //내용
    private Timestamp accesstime; //시간
    private String password; //비밀번호

    public Blog(BlogRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.author = requestDto.getAuthor();
        this.contents = requestDto.getContents();
        this.password = requestDto.getPassword();
        //this.accesstime = requestDto.getAccesstime();
        this.accesstime = new Timestamp(System.currentTimeMillis());
    }

    public void update(BlogRequestDto requestDto) {
        this.title = requestDto.getTitle();
        this.author = requestDto.getAuthor();
        this.contents = requestDto.getContents();
        this.password = requestDto.getPassword();
        this.accesstime = new Timestamp(System.currentTimeMillis());
    }
}
