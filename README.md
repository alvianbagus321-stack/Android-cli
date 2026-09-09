# Android AI Agent

Fondasi full-stack untuk agent Android adaptif. Aplikasi native Kotlin/Compose berbicara ke server Node.js; server menjadi adapter provider dan registry tools. Project sengaja tidak mengklaim root atau kontrol tanpa izin Android.

## Fitur yang tersedia
- Provider configurable: **GPT/OpenAI, Gemini, DeepSeek, Claude, Kimi/Moonshot, MiniMax, custom/local OpenAI-compatible**.
- Provider menyimpan API key di server environment, bukan APK. Base URL dan daftar model dapat diedit melalui REST API.
- Gemini/Claude/OpenAI-compatible memiliki jalur vision; DeepSeek dapat dipakai untuk reasoning/text dan screen understanding dilakukan lewat provider vision yang dikonfigurasi (atau model DeepSeek vision bila endpoint custom mendukungnya).
- Tool registry dengan tool bawaan, schema JSON, template HTTP JSON/prompt, CRUD custom tools, serta penolakan arbitrary server-side code.
- Android Compose dashboard responsif, status permission, provider, tool, dan emergency Stop UI.
- Accessibility service minimal untuk membaca tree bila user mengaktifkannya. Screen capture harus melalui MediaProjection consent flow (belum diaktifkan otomatis).

## Menjalankan server
```bash
cd server && npm install
cp .env.example .env # isi hanya key yang diperlukan
node src/index.mjs
```
Endpoints utama: `GET /health`, `GET/PUT /api/providers/:id`, `GET/POST/DELETE /api/tools`, `POST /api/chat`.
`POST /api/chat` memerlukan `approved:true` ketika mengirim gambar. Untuk production tambahkan autentikasi, TLS/WSS, rate limiting, persistent encrypted secret store, allowlist egress, dan audit storage.

## Build Android
Buka root di Android Studio (AGP 8.5, JDK 17), lalu:
```bash
./gradlew assembleDebug
```
Atau gunakan Android Studio agar Gradle wrapper dibuat/diunduh sesuai instalasi lokal. Minimum API 26, target API 35.

## Menambah tool
POST contoh:
```json
{"name":"My calendar","description":"Read approved calendar data","type":"http","endpoint":"https://approved.example/api/calendar","inputSchema":{"type":"object","properties":{}}}
```
Tool harus deklaratif dan memakai schema. Tidak ada eksekusi arbitrary Kotlin/JavaScript dari API. Untuk tool yang mengontrol perangkat, implementasikan adapter di Android dan laporkan `success`, `tool`, `errorCode`, `errorMessage`; jangan fake-success saat permission unavailable.

## Chat AI dan agent actions
Dashboard sekarang adalah chat dua arah, bukan hanya tombol Run task. User dapat mengirim pesan, melihat bubble balasan, dan app memanggil `POST /api/chat` ke backend menggunakan provider yang dikonfigurasi. Backend mengembalikan jawaban model dan error yang aman ketika provider belum dikonfigurasi.

Action nyata seperti tap, type, swipe, dan open app tetap harus melewati Agent Core + permission Android; chat tidak boleh berpura-pura sudah melakukan action jika Accessibility/MediaProjection/ADB belum aktif. Tahap berikutnya adalah menghubungkan balasan tool call terstruktur ke executor Android, lalu mengirim `tool_result` kembali ke chat.

## Permission, Accessibility Restricted Settings, dan MediaProjection
Manifest sekarang mendeklarasikan permission yang benar-benar dipakai oleh fondasi app: `INTERNET`, `ACCESS_NETWORK_STATE`, `ACCESS_WIFI_STATE`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_MEDIA_PROJECTION`, `POST_NOTIFICATIONS`, `WAKE_LOCK`, dan `SYSTEM_ALERT_WINDOW` untuk agent cursor/overlay. Accessibility tidak diberikan melalui `<uses-permission>`; Android mengaktifkannya melalui service `BIND_ACCESSIBILITY_SERVICE` setelah user menyetujuinya di Settings. `ScreenCaptureService` dideklarasikan dengan `foregroundServiceType="mediaProjection"` dan hanya boleh dijalankan setelah Activity mendapat hasil consent MediaProjection.

Tidak ada storage permission yang sengaja ditambahkan: screenshot sementara memakai app cache, sedangkan import/export seharusnya memakai Storage Access Framework/Photo Picker sehingga tidak membutuhkan akses seluruh storage. Ini mencegah over-declare dan Play Protect warning.

Pada Android 13+ APK sideload dapat terkena **Restricted Settings**. App sekarang menampilkan guidance saat membuka Accessibility: App Info → menu titik tiga → **Allow restricted settings / Izinkan akses terbatas** → kembali ke Accessibility → aktifkan Android AI Agent. Ini tidak dapat di-bypass dari kode. Distribusi Play Store biasanya menghindari status sideload tersebut, tetapi tetap mengikuti kebijakan Play Protect.

Verifikasi setelah build/install ulang:
1. Build APK lalu uninstall versi lama bila service lama masih tercache, install ulang, dan buka app.
2. Buka Access → Accessibility service → ikuti guidance Restricted Settings bila muncul.
3. Aktifkan Android AI Agent di Settings → Accessibility.
4. Kembali ke app dan setujui Screen capture; service foreground akan menampilkan notification saat benar-benar dipakai.
5. App Info → Permissions sekarang harus menampilkan permission runtime yang relevan seperti Notifications (dan akses khusus overlay di menu Special app access). Accessibility tetap berada di Settings → Accessibility karena bukan runtime permission biasa.

## Adaptive Observation dan penghematan Vision API
Agent core sekarang memiliki `ObservationPolicyEngine`, `ScreenStateManager`, `ScreenshotCache`, `VisionResultCache`, dan `VisionUsageManager` di `app/src/main/java/com/androidaiagent/agent/AdaptiveObservation.kt`. Urutannya adalah no observation untuk shell/wait, Accessibility/event, local analysis, cached screenshot, lalu Vision API hanya saat state berubah, confidence rendah, visual context diperlukan, atau task berisiko. Setiap task memiliki budget request, byte limit, TTL cache, confidence threshold, dan usage counters (requests/cache hits/skipped/estimated tokens). Accessibility service menerbitkan event melalui `AgentAccessibilityService.events`, sehingga agent tidak perlu polling screenshot terus-menerus.

`ObservationPolicyEngine` mengembalikan `NO_OBSERVATION`, `ACCESSIBILITY`, `LOCAL_ANALYSIS`, `SCREENSHOT`, atau `VISION_API` beserta alasan. Ini membuat debug dan pengujian policy dapat dilakukan tanpa API key. Fingerprint Accessibility memakai node relevan, sedangkan screenshot/result cache mencegah pengiriman ulang state dan pertanyaan yang sama.

## UI, permission, dan ADB power-user mode
UI memakai dark Aurora theme, responsive cards, model hub, tool studio, live-screen placeholder, safety defaults, dan halaman Access khusus. Dari halaman Access user dapat membuka Android Accessibility Settings dan meminta consent MediaProjection. Permission tidak diaktifkan diam-diam.

ADB bukan permission yang bisa diberikan oleh aplikasi biasa. App Android tidak boleh menjalankan `adb connect` secara diam-diam atau menjanjikan full access. Untuk power-user mode, gunakan komputer/local bridge milik user dengan USB debugging atau Wireless debugging:
1. Aktifkan Developer options dan Wireless debugging.
2. Pair memakai pairing code pada komputer.
3. Jalankan bridge hanya di jaringan privat dan tambahkan authentication/TLS.
4. Minta approval untuk command destruktif dan audit semua action.

ADB tetap tunduk pada Android version, OEM policy, app sandbox, lock state, dan approval user. Jangan expose port ADB ke internet.

## Batasan saat ini
Agent loop, MediaProjection foreground service penuh, WebSocket auth, secure server secret store, ADB bridge terautentikasi, dan eksekutor seluruh Android tools masih merupakan tahap berikutnya. Accessibility hanya aktif setelah user menyalakannya di Settings. Shell Android dibatasi security model OS. Jangan menyalakan remote control di internet tanpa auth + TLS.
