# NexChat AI — Native Android + Web AI Chat Client

Polished ChatGPT-style client. Switch providers/models easily. Material You + dark mode on Android, editorial minimal UI on web.

> Disiapkan tanpa build/download di lingkungan ini sesuai permintaan. Untuk build, buka di Android Studio (PC) — lihat `BUILD.md`.

## Fitur
- Multi-provider: OpenAI-compatible, Anthropic, Gemini, Ollama, Custom URL + API key + custom headers
- Model picker per-conversation + default global
- Chat streaming (SSE), stop/retry, token estimate
- Upload: image (vision), PDF, DOCX, TXT/MD (ekstrak teks lokal)
- Markdown: code block + copy, tabel, KaTeX formula, Mermaid diagram (WebView)
- Branching: setiap assistant message bisa di-retry menjadi cabang, navigasi prev/next
- Memory: ringkasan otomatis + fakta jangka panjang (Room)
- Prompt variables: `{{nama}}`, `{{gaya}}` → dialog isi sebelum kirim + template tersimpan
- Search integration: Brave / Tavily / custom (opsional grounding)
- QR import/export provider (JSON → QR)
- Workspace agent: folder kerja, baca/tulis file, tool-call loop
- MCP: tambah MCP server (SSE/HTTP), list tools, panggil dari chat
- Web version: `web/` PWA statis, gaya Premium Utilitarian Minimalism

## Struktur
```
NexChat/
  app/               # native Android (Kotlin + Compose Material3)
  web/               # versi web statis (tanpa build step)
  BUILD.md           # cara build di PC / Android Studio
  README.md
```

## Keamanan
- API key hanya di EncryptedSharedPreferences / DataStore + SQLCipher opsional
- Tidak ada key yang di-log. Export QR bersifat opt-in dan berisi secret — jangan bagikan sembarangan.

## Stack Android (best practice 2025-2026)
- Kotlin 2.x, Compose BOM, Material3 Expressive, Navigation Compose
- MVVM + StateFlow, Hilt, Room, DataStore, OkHttp SSE, Coil, ZXing, PdfRenderer, POI (docx), WebView Markdown
- MinSdk 26, TargetSdk 35, edge-to-edge, dynamic color, dark mode
