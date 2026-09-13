# BUILD (jalankan di PC / Android Studio, BUKAN di sini)

Lingkungan saat ini diminta untuk tidak build/download library. Jadi langkah di bawah untuk mesin build kamu.

## Syarat
- JDK 17, Android Studio Ladybug+, Android SDK 35
- Internet untuk Gradle sync pertama kali

## Langkah
1. Copy folder `NexChat/` ke PC.
2. Buka Android Studio → Open → pilih `NexChat/`.
3. Tunggu Gradle sync (Compose BOM, Hilt, Room, DataStore, OkHttp, Coil, ZXing diunduh otomatis).
4. Tambahkan KSP plugin di root bila belum: `id("com.google.devtools.ksp") version "2.0.21-1.0.25" apply false` lalu `ksp` di app.
5. Run di emulator/device API 26+.

## Catatan implementasi lanjutan
- DOCX full-parse: tambah `implementation("org.apache.poi:poi-ooxml:5.2.5")` lalu gunakan XWPF di `FileTextExtractor`.
- PDF teks penuh: tambah PdfBox-Android atau ML Kit.
- Mermaid/KaTeX offline: simpan `katex.min.js`, `mermaid.min.js`, `marked.min.js` ke `app/src/main/assets/` dan ubah `MarkdownHtml` ke `file:///android_asset/`.
- MCP HTTP+SSE: `McpClient` sudah JSON-RPC; tambah auth header per-server di Settings.
- Search: isi Brave/Tavily key di Settings, teruskan `searchContext` ke `ChatRepository`.
