# Interplanetary Voyage: Space Survival — السفر بين الكواكب

Offline Android survival/strategy game. The whole game is one HTML page (`game/index.html`) with its art in `game/assets`, wrapped in a small Android WebView app (`android/`).

- Every push to `main` builds a test APK and publishes it as the latest GitHub Release (`InterplanetaryVoyage.apk`).
- Direct download of the newest test build: https://github.com/almarefa2015-tech/interplanetary-voyage/releases/latest/download/InterplanetaryVoyage.apk
- Test builds are signed with `android/app/test-builds.keystore` (test only). The Google Play release key is never stored in this repo.
- Open `game/index.html?nointro` in a desktop browser (served over http) to play-test quickly.
