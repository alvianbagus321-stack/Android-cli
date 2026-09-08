PROMPT LENGKAP — UNIVERSAL ADAPTIVE ANDROID AI AGENT

Buat sebuah aplikasi Android native bernama "Android AI Agent" yang berfungsi sebagai AI agent/client yang dapat memahami dan mengontrol perangkat Android milik pengguna melalui permission dan API Android yang tersedia.

TUJUAN UTAMA

Aplikasi harus memungkinkan AI agent untuk:
- Melihat layar melalui screenshot/screen capture.
- Memahami layar menggunakan computer vision/OCR/vision model jika tersedia.
- Membaca Accessibility Tree.
- Membuka aplikasi.
- Menutup aplikasi jika API/permission memungkinkan.
- Menekan tombol UI.
- Tap berdasarkan node Accessibility atau koordinat.
- Long press.
- Swipe/scroll.
- Mengetik teks.
- Menjalankan Back/Home/Recent Apps jika Android mengizinkan.
- Mengambil informasi perangkat.
- Menjalankan terminal/shell command dengan privilege yang benar-benar tersedia.
- Membaca stdout, stderr, dan exit code.
- Menjalankan rangkaian tindakan secara autonomous berdasarkan instruksi pengguna.
- Memverifikasi hasil setiap tindakan sebelum melanjutkan.
- Menghentikan agent secara langsung melalui tombol Stop/Emergency Stop.

PENTING:
Jangan membuat kemampuan palsu. Jangan mengklaim root, unrestricted shell, atau system-level control jika perangkat tidak memberikannya. Semua fitur harus tunduk pada Android security model dan permission pengguna.

==================================================
1. ARSITEKTUR
==================================================

Gunakan Kotlin dan Jetpack Compose.

Gunakan arsitektur modular berbasis MVVM atau Clean Architecture.

Struktur:

android-ai-agent/
├── app/
├── agent/
│   ├── core/
│   ├── planner/
│   ├── tools/
│   ├── context/
│   ├── verifier/
│   └── memory/
├── accessibility/
├── screen/
├── terminal/
├── network/
├── security/
├── device/
├── data/
└── ui/

Komponen:

Android App
|
├── AI Agent Core
│   ├── Planner
│   ├── Tool Executor
│   ├── Context Manager
│   ├── Screen Analyzer
│   ├── Action Verifier
│   └── Agent Memory
|
├── Android Control Layer
│   ├── Accessibility Service
│   ├── Screen Capture
│   ├── Input Controller
│   ├── App Launcher
│   └── Device Information
|
├── Terminal Layer
│   ├── Command Executor
│   ├── Output Reader
│   └── Command History
|
├── AI Provider Layer
│   ├── OpenAI-compatible API
│   ├── Local Model Support
│   └── Custom API Endpoint
|
├── Remote Connection
│   ├── WebSocket
│   ├── Authentication
│   ├── Heartbeat
│   └── Reconnection
|
└── UI

Setiap modul harus memiliki interface/abstraction agar implementasi dapat diganti tanpa mengubah Agent Core.

==================================================
2. UNIVERSAL ANDROID COMPATIBILITY
==================================================

Aplikasi harus dirancang sebagai Universal Adaptive Android AI Agent.

Target:
- Android 8.0 / API 26 atau lebih baru jika dependency memungkinkan.
- ARM64.
- ARM32 jika dependency memungkinkan.
- x86.
- x86_64.
- Smartphone.
- Tablet.
- Foldable jika memungkinkan.
- Android Go jika memungkinkan.

Target OEM:
- Samsung
- Xiaomi
- Redmi
- POCO
- OPPO
- vivo
- Realme
- OnePlus
- TECNO
- Infinix
- Motorola
- ASUS
- Sony
- Google Pixel
- Generic Android

Jangan mengasumsikan:
- resolusi tertentu
- DPI tertentu
- navigation mode tertentu
- package manager tertentu
- Google Play Services
- root
- terminal tertentu
- vendor-specific API

Gunakan Generic Android sebagai fallback utama.

==================================================
3. ADAPTIVE UI
==================================================

Gunakan Jetpack Compose dan Material 3.

Gunakan:
- Window Size Classes.
- Responsive/adaptive layout.
- WindowInsets.
- Display cutout handling.
- Density-independent measurements.
- dp dan sp.
- BoxWithConstraints jika diperlukan.
- LazyColumn/LazyRow.
- Adaptive navigation.

UI harus mendukung:
- Small screen.
- Large screen.
- Tablet.
- Foldable.
- Portrait.
- Landscape.
- Notch.
- Punch-hole.
- Gesture navigation.
- 3-button navigation.
- Status bar.
- Navigation bar.

Jangan menggunakan hardcoded screen dimensions.

Contoh yang harus dihindari untuk layout utama:
Modifier.offset(100.dp, 200.dp)

Gunakan layout responsive.

Smartphone layout:

┌──────────────────┐
│ Android AI Agent │
├──────────────────┤
│ Live Screen      │
├──────────────────┤
│ Agent Status     │
├──────────────────┤
│ Chat             │
├──────────────────┤
│ Controls         │
└──────────────────┘

Tablet/wide layout:

┌──────────────┬───────────────────────┐
│ Agent        │                       │
│ Controls     │     Live Screen       │
│              │                       │
│ Chat         │                       │
└──────────────┴───────────────────────┘

==================================================
4. AI AGENT TOOL SYSTEM
==================================================

Gunakan tool/function calling.

Tools minimal:

get_screen()
take_screenshot()
analyze_screen()

open_app(package_name)
close_app(package_name)

tap(x, y)
long_press(x, y)
swipe(x1, y1, x2, y2, duration)
type_text(text)

press_back()
press_home()
open_recent_apps()

execute_shell(command)
get_shell_output()

get_device_info()
get_battery_info()
get_storage_info()
get_network_info()

wait(milliseconds)

Tools harus memiliki structured result.

Contoh:

{
  "success": true,
  "tool": "tap",
  "duration_ms": 142,
  "error": null
}

Jika gagal:

{
  "success": false,
  "tool": "tap",
  "error": {
    "code": "ACCESSIBILITY_UNAVAILABLE",
    "message": "Accessibility service is not enabled"
  }
}

Agent harus menggunakan error tersebut untuk menentukan fallback.

==================================================
5. AGENT LOOP
==================================================

Gunakan:

User Request
↓
Understand Intent
↓
Observe Device
↓
Plan
↓
Select Tool
↓
Execute Tool
↓
Observe Result
↓
Verify
↓
Continue / Finish

Gunakan batas:

MAX_STEPS = 30
ACTION_TIMEOUT = 10 seconds

Jangan membuat infinite loop.

Jika agent gagal beberapa kali:
- hentikan task
- simpan error
- tampilkan alasan
- jangan terus mengulangi action

==================================================
6. SCREEN CAPTURE
==================================================

Gunakan Android MediaProjection API.

MediaProjection harus meminta persetujuan pengguna.

Sediakan:
- Screenshot.
- Screen preview.
- Optional frequent capture.
- Image compression.
- Resolution adaptation.
- Frame-rate adaptation.

Jangan mengambil screenshot terus-menerus jika tidak diperlukan.

Untuk perangkat low-end, kurangi:
- frequency
- resolution
- processing
- network transfer

Screenshot dapat dikirim ke vision model hanya jika pengguna mengizinkan dan koneksi/provider mendukungnya.

==================================================
7. SCREEN UNDERSTANDING
==================================================

Gabungkan:

Screenshot
+
Accessibility Tree
+
Agent Context

AccessibilityNodeInfo harus menyediakan jika tersedia:
- text
- contentDescription
- clickable
- enabled
- bounds
- className
- resourceId

Contoh internal representation:

{
  "screen": {
    "width": 1080,
    "height": 2400
  },
  "elements": [
    {
      "text": "Settings",
      "type": "android.widget.TextView",
      "clickable": true,
      "bounds": [100, 300, 500, 380]
    }
  ]
}

==================================================
8. ACCESSIBILITY SERVICE
==================================================

Buat AccessibilityService.

Kemampuan:
- mencari node berdasarkan text
- contentDescription
- resource ID
- class name
- click node
- scroll node
- input text
- membaca hierarchy

Urutan pencarian:

1. Accessibility node
2. Resource ID
3. Content description
4. Visible text
5. Semantic hierarchy
6. Vision
7. Coordinate fallback

Jangan mengandalkan coordinate-only control.

Jika Accessibility Service disabled:
- jangan crash
- tampilkan permission screen
- agent hanya mengaktifkan fitur yang masih tersedia

==================================================
9. COORDINATE ADAPTATION
==================================================

Jangan mengasumsikan resolusi:

1080 x 2400

Agent harus mendapatkan:

{
  "screenWidth": 1080,
  "screenHeight": 2400,
  "density": 2.75,
  "orientation": "portrait"
}

Jika vision model memberikan koordinat screenshot, lakukan normalisasi.

x_normalized = x / screenshot_width
y_normalized = y / screenshot_height

actual_x = x_normalized * device_width
actual_y = y_normalized * device_height

Pertimbangkan:
- status bar
- navigation bar
- display cutout
- rotation
- screenshot scaling
- system insets

Screenshot coordinate tidak boleh dianggap selalu sama dengan physical display coordinate.

==================================================
10. APP LAUNCHER
==================================================

Buat AppManager.

Kemampuan:
- mendapatkan daftar aplikasi yang dapat diluncurkan
- mencari berdasarkan package name
- mencari berdasarkan label
- membuka aplikasi
- memeriksa apakah aplikasi tersedia

Jangan hardcode package name vendor kecuali sebagai fallback yang terdokumentasi.

Jika aplikasi tidak ditemukan:
return structured error.

==================================================
11. TERMINAL
==================================================

Buat TerminalManager.

Interface:

TerminalManager
├── execute(command)
├── stdout
├── stderr
├── exitCode
├── timeout
└── privilege

Pisahkan:
- normal shell
- optional root shell
- optional external terminal integration

Aplikasi harus mendeteksi kemampuan shell.

Jangan mengklaim root jika tidak ada root.

Command yang membutuhkan privilege tinggi:
- harus ditolak jika tidak tersedia
- atau meminta konfirmasi jika sesuai
- tidak boleh mencoba bypass security model Android

Gunakan:
- timeout
- process cancellation
- output size limits
- command validation
- audit logging

==================================================
12. COMMAND SECURITY
==================================================

Terminal adalah fitur sensitif.

Implementasikan:
- allowlist/denylist
- command validation
- timeout
- maximum output size
- confirmation untuk command berisiko
- audit log
- emergency stop

Jangan menjalankan arbitrary remote command tanpa authentication dan authorization.

Jangan menyimpan command sensitif secara permanen tanpa alasan.

==================================================
13. DEVICE CAPABILITY DETECTION
==================================================

Buat DeviceCompatibilityManager.

Deteksi:
- manufacturer
- model
- Android version
- SDK
- CPU ABI
- RAM
- storage
- screen width
- screen height
- density
- orientation
- refresh rate jika tersedia
- navigation mode jika dapat dideteksi
- accessibility
- MediaProjection
- shell
- root
- network
- battery
- available tools

Contoh:

{
  "manufacturer": "Generic",
  "model": "Example",
  "androidVersion": "14",
  "sdk": 34,
  "architecture": "arm64-v8a",
  "screen": {
    "width": 1080,
    "height": 2460,
    "density": 2.75
  }
}

==================================================
14. CAPABILITY REGISTRY
==================================================

Buat ToolRegistry.

Contoh:

{
  "tool": "execute_shell",
  "available": true,
  "privilege": "user"
}

Agent hanya boleh memanggil tool yang tersedia.

Status:

Supported
Partially Supported
Unavailable
Permission Required

Tampilkan status kepada user.

==================================================
15. OEM COMPATIBILITY
==================================================

Buat compatibility layer:

compatibility/
├── generic/
├── samsung/
├── xiaomi/
├── oppo/
├── vivo/
├── realme/
├── tecno/
├── infinix/
└── other/

Jangan membuat vendor-specific implementation sebagai default.

Gunakan Generic Android fallback.

Jika OEM memiliki restriction terhadap:
- background execution
- accessibility
- battery optimization
- screen capture

tampilkan limitation dengan jelas.

==================================================
16. DIFFERENT ANDROID VERSIONS
==================================================

Gunakan runtime API checks.

Contoh konsep:

if (Build.VERSION.SDK_INT >= REQUIRED_API) {
    // new implementation
} else {
    // fallback
}

Jangan menggunakan API baru tanpa version check.

Dokumentasikan:
- minimum supported API
- recommended API
- unsupported features

==================================================
17. NAVIGATION COMPATIBILITY
==================================================

Dukung:
- 3-button navigation
- 2-button navigation
- gesture navigation

Jangan mengasumsikan posisi Back/Home/Recent Apps.

Jika API tidak mengizinkan action tertentu:
- gunakan fallback
- return limitation
- jangan fake success

==================================================
18. MULTI-WINDOW & FOLDABLE
==================================================

Jika perangkat mendukung:
- split screen
- multi-window
- foldable
- large screen

UI harus tetap usable.

Agent harus memahami perubahan window size dan orientation.

==================================================
19. PERFORMANCE ADAPTATION
==================================================

Deteksi:
- RAM
- CPU cores
- CPU architecture
- battery
- thermal state jika tersedia
- network

Mode:
- Performance
- Balanced
- Battery Saver

Low-end:
- lower screenshot frequency
- lower resolution
- less animation
- less background processing
- avoid heavy local AI
- image compression

High-end:
- higher quality analysis jika tersedia
- parallel processing jika aman

Jangan menjalankan image processing berat di main thread.

Gunakan Coroutine, Dispatchers.IO, Dispatchers.Default, dan struktur asynchronous yang benar.

==================================================
20. NETWORK ADAPTATION
==================================================

Dukung:
- Wi-Fi
- mobile data
- metered network
- unstable connection
- offline mode jika local model tersedia

Gunakan:
- WebSocket
- heartbeat
- reconnect
- exponential backoff
- timeout
- connection state
- request cancellation

Jangan mengulangi device action hanya karena network request di-retry.

Gunakan unique action ID.

==================================================
21. REMOTE AGENT MODE
==================================================

Tambahkan mode client-server opsional.

Android:
Android AI Agent Client
↕ encrypted connection
AI Agent Server
↕
LLM / Vision Model

Gunakan WebSocket.

Message:

{
  "type": "tool_call",
  "id": "abc123",
  "tool": "tap",
  "arguments": {
    "x": 540,
    "y": 1200
  }
}

Response:

{
  "type": "tool_result",
  "id": "abc123",
  "success": true
}

Tambahkan:
- authentication
- authorization
- TLS/WSS
- heartbeat
- reconnect
- request timeout
- unique request ID
- replay protection jika diperlukan

Remote control harus OFF secara default sampai user mengaktifkannya.

==================================================
22. AI PROVIDER
==================================================

Buat interface AIProvider.

Dukung:
- OpenAI-compatible API
- custom REST endpoint
- local AI provider jika tersedia

Jangan hardcode API key.

Simpan credential menggunakan Android Keystore atau secure storage yang sesuai.

Dukung:
- text model
- vision model
- tool calling
- streaming response jika tersedia

Jika provider tidak mendukung vision:
gunakan Accessibility Tree dan fallback screen analysis yang tersedia.

==================================================
23. AGENT MEMORY
==================================================

ContextManager menyimpan:
- current task
- previous actions
- tool results
- current screen state
- errors
- user instructions

Jangan menyimpan:
- password
- authentication token
- API key
- sensitive screenshot
- private data

secara permanen tanpa alasan dan persetujuan yang sesuai.

==================================================
24. STATE VALIDATION
==================================================

Agent harus selalu memeriksa apakah state masih valid.

Jika:
- user melakukan tindakan manual
- screen berubah
- app berpindah
- process crash
- accessibility berubah
- permission dicabut

maka:
Old State
↓
State Invalid
↓
Observe Again
↓
Rebuild Context
↓
Re-plan
↓
Continue

Jangan menggunakan stale screenshot/state untuk action penting.

==================================================
25. ACTION VERIFICATION
==================================================

Setiap action:

Observe
↓
Plan
↓
Act
↓
Verify

Contoh:
User: "Open Chrome and search Minecraft."

Agent:
1. Cari Chrome.
2. open_app().
3. get_screen().
4. baca Accessibility Tree.
5. cari search/address field.
6. tap.
7. type_text().
8. submit jika tersedia.
9. get_screen().
10. verify search results.

Jika gagal:
- coba metode alternatif
- lakukan re-observation
- maksimal retry tertentu
- abort safely jika tetap gagal

==================================================
26. USER INTERRUPT
==================================================

User harus selalu dapat menekan Stop.

Flow:

Agent Running
↓
User presses STOP
↓
Cancel current task
↓
Cancel pending actions
↓
Cancel network requests jika memungkinkan
↓
Agent IDLE

Stop button harus memiliki prioritas tinggi.

Agent tidak boleh melanjutkan task setelah Stop.

==================================================
27. SECURITY
==================================================

Implementasikan:
- authentication
- authorization
- encrypted communication
- secure credential storage
- command validation
- action confirmation
- audit log
- emergency stop
- local privacy controls

Remote flow:

Request
↓
Authenticate
↓
Authorize
↓
Validate
↓
Confirm if required
↓
Execute
↓
Verify
↓
Audit

Jangan membuat remote endpoint yang bisa menjalankan command hanya karena mengetahui URL/port.

==================================================
28. PRIVACY
==================================================

Screenshot dan Accessibility Tree dapat berisi data pribadi.

Berikan pengaturan:
- screen capture permission
- remote sharing permission
- AI provider permission
- data retention
- log retention

Jangan mengirim screenshot ke server tanpa izin yang sesuai.

Berikan indikator saat screen sharing aktif.

==================================================
29. UI
==================================================

Buat halaman:
- Dashboard
- Agent Chat
- Live Screen
- Terminal
- Action History
- Permissions
- Device Info
- Connection
- AI Provider
- Security
- Settings

Dashboard:

Android AI Agent
● Connected

Live Screen

Agent Status:
Observing screen...

Current Task:
Opening application...

[ STOP AGENT ]

Terminal:

$ getprop ro.product.model
...

$ uname -a
...

Action History:

17:20:01 OBSERVE
17:20:02 OPEN_APP
17:20:04 SCREEN_CAPTURE
17:20:05 TAP
17:20:06 TYPE_TEXT
17:20:08 VERIFY SUCCESS

==================================================
30. LIVE SCREEN
==================================================

Tampilkan:
- current screen preview
- screenshot refresh
- optional live stream
- tap visualization
- agent cursor
- action overlay
- pause
- stop

Agent cursor harus menggunakan koordinat yang sudah disesuaikan dengan screen dimensions dan insets.

==================================================
31. PERMISSIONS
==================================================

Buat permission onboarding.

Kemungkinan permission:
- Accessibility Service
- MediaProjection
- Notification
- Foreground Service
- Media/storage jika benar-benar diperlukan

Jangan meminta permission yang tidak diperlukan.

Status:

Screen Capture       ✓ Enabled
Accessibility        ✓ Enabled
Terminal             ⚠ Limited
Remote Connection    ✗ Disabled

Jika permission ditolak:
- app tetap terbuka
- fitur terkait disabled
- tampilkan cara mengaktifkan permission

==================================================
32. BACKGROUND EXECUTION
==================================================

Hormati Android background restrictions.

Jika background execution dibatasi:
- gunakan foreground service hanya jika benar-benar diperlukan
- tampilkan notification yang sesuai
- jangan menyembunyikan service
- jangan membuat mekanisme untuk menghindari sistem Android

Tangani process death dan restore state.

==================================================
33. ERROR HANDLING
==================================================

Tangani:
- Accessibility disabled
- MediaProjection denied
- app not found
- command unavailable
- timeout
- AI API failure
- network disconnect
- malformed tool call
- invalid coordinates
- inaccessible UI
- OEM restriction
- process death
- permission revoked
- memory pressure

Aplikasi tidak boleh crash karena kondisi tersebut.

Gunakan error code yang konsisten.

==================================================
34. LOGGING
==================================================

Buat ActionLog.

Contoh:

17:20:01 OBSERVE
17:20:02 OPEN_APP Chrome
17:20:04 SCREEN_CAPTURE
17:20:05 TAP x=520 y=340
17:20:06 TYPE_TEXT
17:20:08 VERIFY SUCCESS

User dapat:
- melihat log
- menghapus log
- mengekspor log jika diizinkan

Jangan menyimpan sensitive data di log.

==================================================
35. STRATEGI PENGUJIAN PERANGKAT
==================================================

Aplikasi harus diuji secara multi-device, multi-Android-version, multi-OEM, multi-resolution, dan multi-form-factor.

DEVICE TEST MATRIX:

Android:
- API 26
- API 28
- API 29
- API 30
- API 31
- API 32
- API 33
- API 34
- API 35+
- current API yang tersedia

Architecture:
- ARM64
- ARM32 jika memungkinkan
- x86_64 jika memungkinkan

Screen:
- small
- normal
- large
- tablet
- foldable jika tersedia

Navigation:
- gesture
- 3-button
- 2-button jika tersedia

Orientation:
- portrait
- landscape

Network:
- Wi-Fi
- mobile data
- offline
- slow network
- unstable network

RAM:
- low-end
- mid-range
- high-end

OEM representative:
- Samsung
- Xiaomi/Redmi/POCO
- OPPO
- vivo
- Realme
- TECNO
- Infinix
- Motorola
- OnePlus
- Google Pixel
- Generic Android

Tidak perlu menguji semua kombinasi. Gunakan representative device matrix.

==================================================
36. AUTOMATED TESTING
==================================================

Gunakan:
- JUnit
- AndroidX Test
- Compose UI Testing
- Espresso jika diperlukan
- MockWebServer
- instrumentation tests

Test:
- unit
- integration
- UI
- Accessibility
- permission
- network
- agent tools
- screen analysis
- recovery
- security
- performance

Untuk UI internal aplikasi, gunakan semantic selectors, bukan hardcoded coordinates.

==================================================
37. DEVICE SMOKE TEST
==================================================

Setiap release harus menjalani:

1. Install APK
2. Launch
3. Check UI
4. Permission flow
5. Enable Accessibility
6. Enable Screen Capture
7. Start Agent
8. Capture screen
9. Open app
10. Tap
11. Type
12. Swipe
13. Execute allowed shell command
14. Receive tool result
15. Stop Agent
16. Restart app
17. Verify state recovery

Critical failure = release blocked.

==================================================
38. TOOL TESTING
==================================================

Test semua tool:

get_screen
take_screenshot
analyze_screen
open_app
close_app
tap
long_press
swipe
type_text
press_back
press_home
open_recent_apps
execute_shell
get_device_info
get_battery_info
get_storage_info
get_network_info
wait

Pastikan setiap tool:
- timeout
- cancellation
- structured result
- error handling
- logging

==================================================
39. SCREEN ANALYSIS TESTING
==================================================

Test:
- Home screen
- Settings
- keyboard
- permission dialog
- dark mode
- light mode
- notch
- navigation bar
- WebView
- RecyclerView
- dialog
- bottom sheet
- popup
- full-screen app
- landscape app
- loading screen
- error screen
- login screen

Gunakan beberapa resolusi:
- 720x1600
- 1080x2400
- 1440x3200
- tablet resolutions
- real device resolutions

==================================================
40. PERMISSION TESTING
==================================================

Test:
Accessibility granted
Accessibility denied
MediaProjection granted
MediaProjection denied
Notification granted
Notification denied
Background restrictions enabled
Background restrictions disabled

Tidak boleh crash ketika permission ditolak.

==================================================
41. OEM TESTING
==================================================

Untuk setiap OEM test:

Device
Manufacturer
Model
Android
ROM
SDK

Accessibility:
PASS / PARTIAL / FAIL

Screen Capture:
PASS / PARTIAL / FAIL

App Launch:
PASS / PARTIAL / FAIL

Input:
PASS / PARTIAL / FAIL

Shell:
PASS / PARTIAL / FAIL

Background:
PASS / PARTIAL / FAIL

Battery Optimization:
PASS / PARTIAL / FAIL

Catat workaround secara terpisah.

==================================================
42. LOW-END TESTING
==================================================

Test low-end device.

Pastikan:
- tidak ANR
- tidak OutOfMemoryError
- screenshot tidak crash
- image processing tidak memblokir UI
- agent tidak infinite loop
- background work terkendali
- recovery setelah process killed

==================================================
43. NETWORK FAILURE TESTING
==================================================

Simulasikan:
- offline
- slow
- high latency
- timeout
- server unavailable
- WebSocket disconnect
- reconnect
- malformed response
- API error
- rate limit

Gunakan unique action ID agar retry network tidak mengulang physical action.

==================================================
44. AGENT RECOVERY TESTING
==================================================

Test:
- AI API disconnect
- app background
- screen off
- accessibility service stop
- permission revoked
- target app closes
- target app crashes
- screen changes
- user manually interacts
- network disconnect
- Android kills app process

Agent harus re-observe dan re-plan.

==================================================
45. SECURITY TESTING
==================================================

Test:
- unauthorized remote connection
- invalid authentication
- malformed tool call
- command injection
- privilege escalation attempt
- unauthorized command
- excessive command
- command timeout
- malicious server response
- API key exposure
- insecure local storage
- replayed request

Critical security failure = release blocked.

==================================================
46. PERFORMANCE BENCHMARK
==================================================

Catat:
- startup time
- screenshot latency
- accessibility query latency
- AI latency
- tool execution latency
- memory
- CPU
- battery
- network bandwidth
- screenshot size
- task completion time

Bandingkan antar perangkat.

==================================================
47. COMPATIBILITY SCORE
==================================================

Buat Compatibility Score:

Overall
Core Agent
Screen Capture
Accessibility
Input
Terminal
Background
Performance

Status:

90-100 = Excellent
75-89 = Good
50-74 = Limited
<50 = Unsupported

Jangan menyebut device 100% compatible hanya berdasarkan smoke test.

==================================================
48. AUTOMATED COMPATIBILITY REPORT
==================================================

Setiap test menghasilkan:

Android AI Agent
Compatibility Test Report

Device:
Android:
SDK:
OEM:
Model:
ABI:

CORE FEATURES
✓ App Launch
✓ UI Rendering
✓ Accessibility
✓ Screen Capture
✓ Tap
✓ Swipe
✓ Text Input
✓ App Launching
✓ Agent Loop

LIMITATIONS
⚠ Shell limited
⚠ Background restricted

RESULT
PASS
Compatibility Score: 94%

Export JSON.

==================================================
49. CI/CD
==================================================

Pipeline:

Git Push
↓
Build
↓
Unit Test
↓
Static Analysis
↓
Instrumentation Test
↓
Compose UI Test
↓
Android Emulator Matrix
↓
Compatibility Test
↓
Security Test
↓
Performance Test
↓
Generate Report
↓
Release

Gunakan beberapa Android API level dalam CI.

Untuk OEM-specific testing gunakan physical devices/device farm jika tersedia.

==================================================
50. RELEASE GATE
==================================================

Jangan release production jika:
- startup crash
- critical permission flow rusak
- agent tidak dapat dihentikan
- screen capture crash
- Accessibility integration crash
- unauthorized command execution
- remote authentication bypass
- infinite agent loop
- serious memory leak
- critical UI broken
- state corruption setelah process restart

Setiap release harus memiliki:
- Test Report
- Known Issues
- Supported Android Versions
- Supported Architectures
- Known OEM Limitations
- Security Status
- Performance Summary

==================================================
51. BUILD QUALITY
==================================================

Project harus:
- compile
- lint clean atau memiliki alasan terdokumentasi
- modular
- maintainable
- testable
- documented
- production-oriented

Gunakan dependency version yang stabil dan kompatibel.

Jangan membuat mock untuk fitur inti jika API Android yang diperlukan tersedia.

Jika sebuah fitur tidak dapat dilakukan oleh Android biasa:
- implementasikan fallback terbaik
- jelaskan limitation
- return structured capability status
- jangan membuat fake implementation

==================================================
52. DOCUMENTATION
==================================================

README harus menjelaskan:
- requirement
- Android versions
- permissions
- build instructions
- installation
- setup AI provider
- API key configuration
- Accessibility setup
- Screen Capture setup
- remote connection
- security
- limitations
- OEM compatibility
- testing
- troubleshooting

==================================================
53. FINAL PRINCIPLE
==================================================

Gunakan prinsip:

"BUILD ONCE, ADAPT EVERYWHERE."

Dan:

"TEST THE CAPABILITY, NOT THE DEVICE."

Satu codebase harus dapat beradaptasi terhadap:
- Android versions
- OEM
- ROM
- resolution
- DPI
- architecture
- navigation
- permissions
- performance
- network
- battery
- background restrictions

Gunakan capability detection, compatibility layer, graceful degradation, fallback, verification, recovery, security, dan automated testing.

==================================================
54. DEVELOPMENT OUTPUT
==================================================

Hasil akhir harus berupa project Android Studio lengkap dengan:
- source code
- Gradle files
- AndroidManifest
- AccessibilityService
- MediaProjection
- Compose UI
- Agent Core
- Tool system
- AI provider abstraction
- WebSocket client
- Terminal abstraction
- Device capability detection
- Compatibility layer
- Permission flow
- Security layer
- Logging
- Testing
- CI/CD configuration
- README

Sebelum menyatakan project selesai:
1. Build project.
2. Jalankan unit tests.
3. Jalankan lint/static analysis.
4. Jalankan instrumentation/UI tests yang tersedia.
5. Verifikasi permission flow.
6. Verifikasi agent stop mechanism.
7. Verifikasi capability detection.
8. Verifikasi fallback ketika permission/API tidak tersedia.
9. Verifikasi tidak ada fake root/unrestricted control.
10. Buat daftar limitation yang masih ada.

Jangan hanya memberikan source code secara teori. Pastikan project memiliki struktur yang konsisten dan seluruh modul utama saling terhubung.
