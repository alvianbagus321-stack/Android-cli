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
