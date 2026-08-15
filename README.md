# FloatingCounter

로컬에 Android Studio 없이, GitHub Actions에서 빌드되는 플로팅(오버레이) 탭 카운터 앱입니다.

## 사용 흐름

1. **VS Code로 코드 확인/수정**
   - `app/src/main/java/com/example/floatingcounter/` 안의 `MainActivity.kt`, `FloatingService.kt`를 원하는 대로 수정합니다.
   - 버튼 색, 초기 위치 등은 `res/layout/floating_widget.xml`, `res/drawable/bg_floating.xml`에서 바꿀 수 있습니다.

2. **GitHub에 푸시**
   ```bash
   git init
   git add .
   git commit -m "init floating counter"
   git branch -M main
   git remote add origin <본인의 GitHub 저장소 URL>
   git push -u origin main
   ```

3. **자동 빌드 확인**
   - GitHub 저장소 → **Actions** 탭에서 "Build APK" 워크플로가 자동 실행됩니다 (약 3~5분 소요).
   - 완료되면 해당 실행 결과 페이지 하단 **Artifacts**에서 `floating-counter-debug.zip`을 다운로드합니다. 안에 `app-debug.apk`가 들어있습니다.

4. **폰에 설치**
   - APK 파일을 폰으로 옮긴 뒤 실행 → "출처를 알 수 없는 앱" 설치 허용 → 설치.
   - 앱 실행 → "플로팅 카운터 시작" 버튼 → 오버레이 권한 화면에서 허용 → 앱으로 돌아와 버튼 다시 탭.
   - 화면 위에 떠 있는 숫자를 탭하면 +1, 드래그하면 위치 이동, "리셋" 텍스트를 누르면 0으로 초기화됩니다.

## 참고

- `workflow_dispatch`가 켜져 있어서 push 없이도 Actions 탭에서 "Run workflow" 버튼으로 수동 빌드도 가능합니다.
- 오버레이 권한(`SYSTEM_ALERT_WINDOW`)은 안드로이드가 사용자 확인을 강제하는 권한이라 앱 최초 실행 시 한 번은 수동으로 켜줘야 합니다.
