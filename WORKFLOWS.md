# GitHub Actions Workflow 추가 방법

이 프로젝트에는 3개의 GitHub Actions workflow 파일이 로컬에 생성되어 있습니다:
- `.github/workflows/build.yml` (88 lines)
- `.github/workflows/release.yml` (94 lines)
- `.github/workflows/pr-check.yml` (70 lines)

GitHub App의 보안 정책으로 인해 자동으로 푸시할 수 없으므로, 다음 방법 중 하나를 선택해서 추가해주세요.

## 방법 1: GitHub Web UI로 추가 (가장 쉬움) ✅

### 1. build.yml 추가
1. GitHub 저장소 페이지로 이동
2. "Add file" → "Create new file" 클릭
3. 파일 이름 입력: `.github/workflows/build.yml`
4. 로컬의 `.github/workflows/build.yml` 파일 내용을 복사해서 붙여넣기
5. "Commit new file" 클릭

### 2. release.yml 추가
- 위와 동일한 방법으로 `.github/workflows/release.yml` 생성

### 3. pr-check.yml 추가
- 위와 동일한 방법으로 `.github/workflows/pr-check.yml` 생성

## 방법 2: git clone 후 직접 푸시

```bash
# 1. 새로운 디렉토리에 저장소 클론 (GitHub 계정 사용)
git clone https://github.com/lukehemmin/chest_lock.git temp_repo
cd temp_repo

# 2. workflow 파일 복사
mkdir -p .github/workflows
cp /home/user/chest_lock/.github/workflows/* .github/workflows/

# 3. 커밋 및 푸시
git add .github/workflows/
git commit -m "Add GitHub Actions CI/CD workflows"
git push origin main
```

## 방법 3: Pull Request로 추가

```bash
# 1. 새 브랜치 생성
git checkout -b add-workflows

# 2. workflow 파일 커밋 (권한 있는 계정 필요)
git add .github/workflows/
git commit -m "Add CI/CD workflows"
git push origin add-workflows

# 3. GitHub에서 PR 생성 및 머지
```

---

## Workflow 파일 내용

### build.yml - 빌드 및 테스트
```yaml
트리거: push, pull_request (main/master/develop)
기능:
- 멀티 OS 빌드 (Ubuntu, Windows, macOS)
- Java 21 + Gradle
- 빌드 아티팩트 업로드
- 코드 품질 체크
```

### release.yml - 자동 릴리즈
```yaml
트리거: 버전 태그 push (v*.*.*)
기능:
- 자동 버전 추출
- JAR 빌드
- Changelog 자동 생성
- GitHub Release 생성
- 아티팩트 업로드
```

### pr-check.yml - PR 검증
```yaml
트리거: Pull Request
기능:
- Gradle wrapper 검증
- 빌드 및 테스트
- PR에 자동 코멘트
```

---

## 추가 후 확인

workflow를 추가한 후:

1. **Actions 탭 확인**
   - https://github.com/lukehemmin/chest_lock/actions
   - workflow가 정상적으로 표시되는지 확인

2. **테스트 실행**
   ```bash
   # main 브랜치에 커밋 푸시
   git push origin main

   # Actions 탭에서 build.yml 실행 확인
   ```

3. **릴리즈 테스트**
   ```bash
   # 버전 태그 생성
   git tag v1.0.0
   git push origin v1.0.0

   # Actions 탭에서 release.yml 실행 확인
   # Releases 페이지에서 릴리즈 생성 확인
   ```

---

## 문제 해결

### Q: workflow 파일이 실행되지 않아요
A: GitHub 저장소 설정에서 Actions를 활성화해야 합니다:
   - Settings → Actions → General
   - "Allow all actions and reusable workflows" 선택

### Q: 빌드가 실패해요
A: Java 버전과 Gradle 설정을 확인하세요:
   - build.gradle에 Java 21 설정 확인
   - Spigot API 의존성이 올바른지 확인

### Q: 릴리즈가 자동 생성되지 않아요
A: 태그 형식을 확인하세요:
   - 올바른 형식: v1.0.0, v2.1.3
   - 잘못된 형식: 1.0.0, version-1.0

---

더 자세한 정보는 [GitHub Actions 문서](https://docs.github.com/actions)를 참고하세요.
