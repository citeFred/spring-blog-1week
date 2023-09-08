# Spring Mini Project repository
Spring MVC 패턴을 공부하고 주간 테스트를 기록하는 공간입니다.

## 🖥️ 스터디 저장소 소개
* 문제를 통한 요구사항 실습
* 코드 수정 및 기능 구현을 통해 부족한 부분을 체크하는 테스트

Spring Framework를 활용한 웹 서비스로 간단한 블로그 게시글을 생성, 전체 게시글 목록보기, 수정, 삭제 할 수 있습니다.

## 📊 프로젝트 Usecase Diagram
![image](https://github.com/yzpocket/spring-mvc-BlogApp/assets/67217259/5a345915-3eb1-4d39-8e9a-03c867e41334)

## 🧩 프로젝트 API 명세서 및 요구조건
![image](https://github.com/yzpocket/spring-mvc-BlogApp/assets/67217259/fdf9df85-025e-46a7-9eda-96f3aff85b94)
![image](https://github.com/yzpocket/spring-mvc-BlogApp/assets/67217259/76575f64-4aac-49a1-8ceb-12a9487bae74)



## 🕰️ 개발 기간
* 23.08.29 - 23.08.30

### 🧑‍🤝‍🧑 맴버구성
- 김인용 - CRUD 기능

### 👋🏻 Contact
- **Email** : citefred@yzpocket.com
- **Blog** : https://www.citefred.com
  
### ⚙️ 개발 환경
- **MainLanguage** : `Java` - JDK 17
- **IDE** : IntelliJ IDEA Ultimate
- **Framework** : Spring Framework
- **Database** : MySQL(Local)
- **SERVER** : Spring inner server(not published)

## 📌 주요 기능
#### 블로깅 기본 기능
* [1] 게시글 생성(CREATE)
    - POST 방식 API를 통한 게시글 저장
* [2] 게시글 전체 목록 조회(READ)
    - GET 방식 API를 통한 게시글 목록 불러오기
* [3] 선택한 게시글 보기(READ)
    - GET 방식 API를 통한 게시글 내용 보기
* [4] 게시글 수정(UPDATE)
    - PUT 방식 API 사용해서 게시글 내용 수정
* [5] 게시글 삭제(DELETE)
    - DELETE 방식 API 사용해서 특정 게시글 삭제

## ⚠️ 주의
#### 추적 예외
* src/main/resources/application.properties 파일은 DB 접속 정보가 있어 추적이 제외되어 있습니다.
* 테스트를 진행 하시려면 위 경로와 파일(application.properties)을 생성해주세요.
- 다음과 코드를 입력해주세요 < ... > 부분을 작성해주셔야 합니다. "<", ">" 괄호도 제거되어야 합니다.
- ex) spring.datasource.username=root
```
spring.datasource.url=jdbc:mysql://localhost:3306/blog
spring.datasource.username=<USERNAME>
spring.datasource.password=<PASSWORD>
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```
