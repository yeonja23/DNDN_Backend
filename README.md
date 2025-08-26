# DNDN-BackEnd-Repository

든든팀의 백엔드 레포지토리

당신에게 필요한 복지, 든든하게 챙기세요
맞춤형 복지 안내 플랫폼
---

<img width="7680" height="4320" alt="든든compression" src="https://github.com/user-attachments/assets/cf0901e9-8b97-4ac7-928f-d96e4e362250" />


---

## 🌿 브랜치 전략

```bash
- main
- develop
  - [팀원 이름]
    - 라벨/기능명

- `main` : 배포 가능한 안정화 버전  
- `develop` : 다음 배포를 위한 통합 브랜치  
- `[팀원 이름]` : 팀원별 브랜치  
- `feature/*` : 기능 단위 작업용 브랜치  
- `chore` : 빌드 설정, 프로젝트 초기 설정, 환경 구성 등
```


## 🧱 코드 컨벤션

### 📦 도메인 기반 패키지 구조

```java
└── global
    ├── config
    ├── exception
    └── s3
    ...

└── domain(기능명)
    └── api
        ├── controller
        └── dto
            ├── request
            │   └── {어떤 dto인지}ReqDto
            └── response
                └── {어떤 dto인지}ResDto
    └── application
        └── service
    └── domain
        ├── repository
        │   └── Repository
        └── entity
```


## 📝 네이밍 컨벤션

| 항목                     | 스타일                | 예시                                                       |
|------------------------|---------------------|----------------------------------------------------------|
| 패키지명 / 변수명 / 메서드명 | `lowerCamelCase`      | `addUser()`, `userName`, `findByEmail()`                 |
| 클래스명 / 인터페이스명    | `UpperCamelCase`      | `UserService`, `LoginRequestDto`, `UserController`       |
| 상수명 (static final)   | `SCREAMING_SNAKE_CASE` (전체 대문자 + 언더스코어) | `DEFAULT_PAGE_SIZE`, `MAX_LOGIN_ATTEMPTS`                |

※ `/`를 입력해 명령어처럼 사용 가능

---


## 🌐 API 엔드포인트 네이밍

- **기능**: `/quest/list`  
- **파라미터**: `{quest-id}`


## ✅ Issue 네이밍 규칙

- 기능 추가 : `feat`
- 버그 수정 : `fix`
- 리팩토링 : `refactor`
- 문서 작업 : `docs`

📌 이슈 이름 예시:  
`[Feat] 작업 내용`


## 🧩 Issue Template

```
## 어떤 기능인가요?

> 추가하려는 기능에 대해 간결하게 설명해주세요

## 작업 상세 내용

- [ ] 새로운 기능
- [ ] 테스트
- [ ] TODO
```


## ✅ PR Template

```
#️⃣연관된 이슈
ex) #이슈번호, #이슈번호

📝작업 내용
이번 PR에서 작업한 내용을 간략히 설명해주세요

💬리뷰 요구사항(선택)
리뷰어가 특별히 봐주었으면 하는 부분이 있다면 작성해주세요

ex) 메서드 XXX의 이름을 더 잘 짓고 싶은데 혹시 좋은 명칭이 있을까요?
```


## 🧾 커밋 규칙

- feat[#23]: 로그인 API 구현


## 🗃️ ERD

<img width="2030" height="1112" alt="DNDN_ERD_최종 (1)" src="https://github.com/user-attachments/assets/d10c8942-0dfe-4763-92f6-df98013f1fd4" />

## ☁️ Architecture

<img width="1328" height="1186" alt="Infra Architecture" src="https://github.com/user-attachments/assets/c2f42b80-2919-4b43-8ab2-aa25196b71c6" />

---
## 👨‍👩‍👧‍👦 Back Team

- BE: 김지원, 김채민, 박연지

---
## ⚙️ Backend Skill

### 🔐 Security
![Spring Security](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=white)  
![JWT](https://img.shields.io/badge/JWT-000000?logo=jsonwebtokens&logoColor=white)  
![CORS](https://img.shields.io/badge/CORS-FF6F00?logo=icloud&logoColor=white)

### 🗄 Database
![MySQL](https://img.shields.io/badge/MySQL-4479A1?logo=mysql&logoColor=white)  
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-59666C?logo=spring&logoColor=white)  
![Redis](https://img.shields.io/badge/Redis-DC382D?logo=redis&logoColor=white)  

### 🌐 Infra & Deployment
![AWS EC2](https://img.shields.io/badge/AWS%20EC2-FF9900?logo=amazonaws&logoColor=white)  
![MinIO (S3 Compatible)](https://img.shields.io/badge/MinIO%20(S3%20Compatible)-C72E49?logo=minio&logoColor=white)  
![DuckDNS](https://img.shields.io/badge/DuckDNS-2C8EBB?logo=duckduckgo&logoColor=white)  
![Nginx](https://img.shields.io/badge/Nginx-009639?logo=nginx&logoColor=white)  
![HTTPS](https://img.shields.io/badge/HTTPS-0052CC?logo=letsencrypt&logoColor=white)  

### 📑 API & Docs
![OpenAPI](https://img.shields.io/badge/OpenAPI-6BA539?logo=openapiinitiative&logoColor=white)  
![Swagger](https://img.shields.io/badge/Swagger-85EA2D?logo=swagger&logoColor=black)  

### 💻 Dev Tools
![Java](https://img.shields.io/badge/Java-007396?logo=java&logoColor=white)  
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=white)  

### ⚡ CI/CD & Collaboration
![GitHub](https://img.shields.io/badge/GitHub-181717?logo=github&logoColor=white)  
![GitHub Actions](https://img.shields.io/badge/GitHub%20Actions-2088FF?logo=githubactions&logoColor=white)  


