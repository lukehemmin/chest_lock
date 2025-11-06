# ChestLock 🔒

[![Build & Test](https://github.com/lukehemmin/chest_lock/actions/workflows/build.yml/badge.svg)](https://github.com/lukehemmin/chest_lock/actions/workflows/build.yml)
[![Release](https://github.com/lukehemmin/chest_lock/actions/workflows/release.yml/badge.svg)](https://github.com/lukehemmin/chest_lock/actions/workflows/release.yml)
[![Latest Release](https://img.shields.io/github/v/release/lukehemmin/chest_lock?include_prereleases)](https://github.com/lukehemmin/chest_lock/releases)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](LICENSE)

**종속성 없는 Minecraft 블록 보호 플러그인**

BlockProt과 유사한 기능을 제공하지만, 외부 라이브러리 의존성 없이 Bukkit API만으로 구현되었습니다.

## ✨ 주요 기능

- 🔐 **블록 보호**: 상자, 화로, 셜커상자, 문, 트랩도어 등 모든 블록 잠금
- 👥 **친구 시스템**: 친구 추가 및 권한 관리 (읽기 전용 / 읽기+쓰기)
- 🗄️ **다중 저장소**: YAML 파일 또는 MySQL/MariaDB 데이터베이스
- 🔄 **자동 마이그레이션**: 안전한 데이터베이스 스키마 업데이트
- 🛡️ **고급 보호**: 폭발, 피스톤, 호퍼로부터 보호
- 🎨 **직관적인 GUI**: 복잡한 설정 없이 쉬운 사용
- 🌍 **한국어 지원**: 완전한 한국어 메시지 및 UI

## 🚀 설치 방법

### 요구사항
- Java 21+
- Spigot / Paper 1.21.3+
- (선택) MySQL/MariaDB 8.0+ (데이터베이스 저장 시)

### 설치
1. [Releases](https://github.com/lukehemmin/chest_lock/releases) 페이지에서 최신 버전 다운로드
2. `ChestLock-X.X.X.jar` 파일을 서버의 `plugins` 폴더에 복사
3. 서버 재시작
4. (선택) `plugins/ChestLock/config.yml` 에서 설정 변경

## 📖 사용법

### 블록 잠그기
1. 잠글 블록에 **Shift + 우클릭**
2. 메뉴에서 "블록 잠그기" 선택

### 친구 추가
1. 잠긴 블록에 **Shift + 우클릭**
2. "친구 관리" 선택
3. "온라인 플레이어 추가" 클릭
4. 추가할 플레이어 선택

### 권한 변경
- **좌클릭**: 권한 토글 (읽기 전용 ↔ 읽기+쓰기)
- **우클릭**: 친구 제거

### 명령어
```
/chestlock help     - 도움말 표시
/chestlock reload   - 설정 리로드 (관리자)
/chestlock about    - 플러그인 정보
```

### 권한
```yaml
chestlock.lock    - 블록 잠그기 (기본: true)
chestlock.admin   - 관리자 권한 (기본: op)
chestlock.bypass  - 모든 보호 무시 (기본: false)
```

## ⚙️ 설정

### config.yml
```yaml
# 저장소 타입 선택
storage:
  type: YAML  # YAML 또는 MYSQL

  # MySQL 설정 (type이 MYSQL일 때만 사용)
  mysql:
    host: localhost
    port: 3306
    database: chestlock
    username: root
    password: password
    pool:
      maximum-pool-size: 10
      minimum-idle: 2
      connection-timeout: 30000

# 잠글 수 있는 블록 목록
lockable-blocks:
  containers: [CHEST, BARREL, FURNACE, ...]
  shulker-boxes: [WHITE_SHULKER_BOX, ...]
  doors: [OAK_DOOR, IRON_DOOR, ...]
  trapdoors: [OAK_TRAPDOOR, ...]
  gates: [OAK_FENCE_GATE, ...]

# 메시지 커스터마이징
messages:
  prefix: '&8[&6ChestLock&8]&r'
  no-permission: '&c이 블록에 접근할 권한이 없습니다.'
  locked: '&a블록이 잠겼습니다!'
  # ...
```

## 🗄️ 데이터 저장

### YAML 모드 (기본)
- **위치**: `plugins/ChestLock/protections.yml`
- **장점**: 간단한 설정, 파일 기반 백업
- **추천**: 소규모 서버 (10,000개 이하 보호 블록)

### MySQL 모드
- **위치**: MySQL/MariaDB 데이터베이스
- **장점**: 빠른 성능, 멀티 서버 지원
- **추천**: 대규모 서버 (10,000개 이상 보호 블록)

자세한 내용은 [Wiki](https://github.com/lukehemmin/chest_lock/wiki)를 참고하세요.

## 🔨 빌드 방법

### 로컬 빌드
```bash
git clone https://github.com/lukehemmin/chest_lock.git
cd chest_lock
./gradlew build
```

빌드된 JAR 파일: `build/libs/ChestLock-1.0.0.jar`

### CI/CD
이 프로젝트는 GitHub Actions를 사용합니다:
- **Build & Test**: 모든 커밋에서 자동 빌드 및 테스트
- **Release**: 태그 생성 시 자동 릴리즈

## 📦 릴리즈 만들기

새 버전을 릴리즈하려면:
```bash
git tag v1.2.3
git push origin v1.2.3
```

GitHub Actions가 자동으로:
1. 빌드 수행
2. JAR 파일 생성
3. GitHub Release 생성
4. 변경 로그 자동 생성

## 🆚 BlockProt과의 비교

| 기능 | BlockProt | ChestLock |
|------|-----------|-----------|
| 외부 종속성 | 3개 (NBT-API, AnvilGUI, SquirrelID) | **0개** ✅ |
| 데이터 저장 | NBT-API | Bukkit PersistentDataContainer |
| 일반 블록 저장 | NBT-API | YAML 또는 MySQL |
| GUI | AnvilGUI | Bukkit Inventory |
| 데이터베이스 | ❌ | ✅ MySQL/MariaDB |
| 자동 마이그레이션 | ❌ | ✅ 안전한 스키마 업데이트 |
| 한국어 지원 | ⚠️ 부분 | ✅ 완전 지원 |

## 🛠️ 기술 스택

- **언어**: Java 21
- **빌드 도구**: Gradle 8.5
- **API**: Spigot API 1.21.3
- **데이터베이스**: MySQL/MariaDB (선택)
- **커넥션 풀**: HikariCP 5.1.0
- **CI/CD**: GitHub Actions

## 📊 프로젝트 구조

```
chest_lock/
├── src/main/
│   ├── java/com/chestlock/
│   │   ├── ChestLock.java              # 메인 클래스
│   │   ├── commands/                    # 명령어
│   │   ├── data/                        # 데이터 저장
│   │   │   ├── BlockDataHandler.java
│   │   │   ├── YamlStorage.java
│   │   │   ├── MySQLStorage.java
│   │   │   ├── DatabaseManager.java
│   │   │   └── migration/               # DB 마이그레이션
│   │   ├── gui/                         # GUI 메뉴
│   │   ├── listeners/                   # 이벤트 리스너
│   │   └── model/                       # 데이터 모델
│   └── resources/
│       ├── plugin.yml
│       └── config.yml
├── .github/workflows/                   # CI/CD
└── build.gradle
```

## 🤝 기여하기

기여를 환영합니다! 다음 단계를 따라주세요:

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/amazing-feature`)
3. Commit your changes (`git commit -m 'Add some amazing feature'`)
4. Push to the branch (`git push origin feature/amazing-feature`)
5. Open a Pull Request

## 📝 라이센스

이 프로젝트는 MIT 라이센스 하에 배포됩니다. 자세한 내용은 [LICENSE](LICENSE) 파일을 참고하세요.

## 💬 지원

- **버그 리포트**: [Issues](https://github.com/lukehemmin/chest_lock/issues)
- **기능 요청**: [Issues](https://github.com/lukehemmin/chest_lock/issues)
- **문의**: GitHub Issues 또는 Discussions

## 🌟 감사의 말

이 프로젝트는 [BlockProt](https://github.com/spnda/BlockProt)에서 영감을 받아 만들어졌습니다.

---

**Made with ❤️ for the Minecraft community**
