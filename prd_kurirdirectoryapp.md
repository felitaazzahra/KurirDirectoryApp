# PRD — KurirDirectoryApp

**Product Requirements Document**
Aplikasi Android direktori kurir/pengguna berbasis konsumsi RESTful GET API publik, RecyclerView, dan live search.

| Owner | Status | Platform | Min SDK | Sumber |
|---|---|---|---|---|
| Engineering Manager | Draft — Siap Dikerjakan | Android (Kotlin) | API 24 (Nougat) | Modul Praktikum P10 |

---

## 1. Latar Belakang & Tujuan Produk

KurirDirectoryApp adalah aplikasi latihan tingkat dasar yang mendemonstrasikan alur end-to-end konsumsi RESTful API publik (JSONPlaceholder `/users`) ke dalam antarmuka Android modern menggunakan Retrofit 2, Gson, Kotlin Coroutines, dan RecyclerView. Tujuan bisnis dari sudut pandang pembelajaran: membangun kompetensi dasar networking + list rendering + filtering yang menjadi fondasi hampir semua aplikasi data-driven di industri.

**Goal produk:** pengguna dapat membuka aplikasi, langsung melihat daftar kurir/pengguna, dan mencari nama secara instan tanpa lag atau request tambahan ke server.

---

## 2. Target Pengguna & Persona

- **Pengguna akhir (dalam simulasi):** staf admin logistik yang perlu mencari data kurir dengan cepat.
- **Mahasiswa/Developer:** pengerja tugas yang perlu mengimplementasikan dan mengevaluasi kode sesuai modul serta menyelesaikan 2 tugas mandiri.

---

## 3. Ruang Lingkup (Scope)

### In-Scope
- GET list user dari `jsonplaceholder.typicode.com/users`
- Tampilan RecyclerView + CardView per item
- Live search in-memory berbasis nama
- Loading state & error state jaringan
- Filter ganda (nama ATAU perusahaan)
- Empty state ketika hasil pencarian kosong

### Out-of-Scope
- Autentikasi / login
- CRUD (create/update/delete) data user
- Local database / caching offline (Room)
- Pagination / infinite scroll
- Dark mode UI kustom

---

## 4. User Stories & Kriteria Penerimaan

### US-01 — Pemuatan Data Otomatis
*Sebagai pengguna, saya ingin daftar kurir langsung tampil saat aplikasi dibuka*, agar saya tidak perlu aksi tambahan.

- Given aplikasi baru dibuka, When proses fetch API berjalan, Then `ProgressBar` tampil.
- Given request berhasil (HTTP 200), When data diterima, Then 10 kartu user tampil rapi di RecyclerView dan ProgressBar hilang.
- Given request gagal / tidak ada koneksi, When error terjadi, Then pesan galat tampil di `textViewErrorMessage`, bukan crash.

### US-02 — Pencarian Nama Real-Time
*Sebagai pengguna, saya ingin mengetik nama dan melihat list menyusut seketika*, agar pencarian terasa instan.

- Given daftar penuh sudah dimuat, When user mengetik "ervin", Then hanya kartu dengan nama mengandung "ervin" (case-insensitive) yang tampil.
- Given kolom pencarian dikosongkan, When teks dihapus semua, Then seluruh 10 data tampil kembali.
- Filtering harus in-memory (tanpa request API baru) — respons instan, tanpa jeda loading.

### US-03 — Filter Ganda: Nama ATAU Perusahaan `Tugas 1`
*Sebagai pengguna, saya ingin pencarian juga mencocokkan nama perusahaan*, agar saya bisa mencari lewat afiliasi kurir.

- Given kata kunci "Crona", When difilter, Then user dengan `company.companyName` mengandung "Crona" ikut tampil meski namanya tidak cocok.
- Logika filter:
  ```kotlin
  pengguna.name.contains(kataKunci, true) || pengguna.company.companyName.contains(kataKunci, true)
  ```

### US-04 — Empty State Pencarian `Tugas 2`
*Sebagai pengguna, saya ingin diberi tahu jika pencarian tidak menemukan hasil*, agar saya tahu itu bukan bug.

- Given kata kunci "Zulkifli" tidak cocok dengan data manapun, When filter dijalankan, Then RecyclerView kosong dan TextView "Tidak ada kurir yang cocok dengan pencarian Anda" tampil di tengah layar.
- Given hasil filter kembali ada isinya, When user mengubah kata kunci, Then TextView empty-state otomatis disembunyikan lagi.

---

## 5. Spesifikasi Fungsional

| ID | Requirement | Prioritas |
|---|---|---|
| F-01 | Integrasi Retrofit 2 + Gson Converter ke endpoint `GET /users` | Must |
| F-02 | Model data `UserResponse` & `CompanyInfo` dengan `@SerializedName` | Must |
| F-03 | RecyclerView + Adapter menampilkan nama, perusahaan, email, telepon | Must |
| F-04 | TextWatcher pada `editTextSearchName` memicu filter on-type | Must |
| F-05 | Filter gabungan nama + nama perusahaan (case-insensitive, contains) | Should |
| F-06 | Empty-state view saat hasil filter = 0 item | Should |
| F-07 | Loading indicator saat fetch awal; disembunyikan setelah selesai | Must |
| F-08 | Pesan error yang informatif saat gagal koneksi / HTTP non-2xx | Must |

---

## 6. Spesifikasi Non-Fungsional

- **Performa:** filtering in-memory harus terasa instan (<16ms per ketikan, tanpa jank), tidak melakukan request API tambahan saat mengetik.
- **Reliabilitas:** semua exception jaringan ditangkap (try/catch) agar aplikasi tidak crash saat offline.
- **Threading:** panggilan API dijalankan di `Dispatchers.IO` via `lifecycleScope`, update UI kembali di `Dispatchers.Main`.
- **Kompatibilitas:** minimum API 24 (Android 7.0), diuji di emulator dan/atau perangkat fisik.
- **Keamanan dasar:** izin `INTERNET` & `ACCESS_NETWORK_STATE` di `AndroidManifest.xml`.

---

## 7. Arsitektur & Desain Teknis

### Struktur Proyek
- Package: `com.latihan.kurirdirectoryapp`
- `UserResponse.kt` — model data (+ `CompanyInfo` nested)
- `ApiService.kt` — interface Retrofit, `suspend fun getAllUsers()`
- `ApiClient.kt` — singleton `object` pembangun instance Retrofit + Gson
- `UserAdapter.kt` — `RecyclerView.Adapter`, method `perbaruiDaftar()`
- `MainActivity.kt` — orkestrasi fetch, binding, TextWatcher

### Alur Data
`MainActivity` → `ApiClient.apiService.getAllUsers()` (Coroutine, IO) → `Response<List<UserResponse>>` → `withContext(Main)` → `daftarPenggunaAsli` (source of truth) → `UserAdapter.perbaruiDaftar()` → RecyclerView re-render.

Saat mengetik: `TextWatcher` → filter lokal pada `daftarPenggunaAsli` → hasil dikirim ke adapter, tanpa menyentuh jaringan lagi.

### Dependensi Utama
```kotlin
implementation("com.squareup.retrofit2:retrofit:2.11.0")
implementation("com.squareup.retrofit2:converter-gson:2.11.0")
implementation("androidx.recyclerview:recyclerview:1.3.2")
implementation("androidx.cardview:cardview:1.0.0")
implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.8.0")
implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
```

---

## 8. Kontrak Data (API)

| Field JSON | Tipe | Mapping Kotlin |
|---|---|---|
| id | Int | `UserResponse.id` |
| name | String | `UserResponse.name` |
| username | String | `UserResponse.username` |
| email | String | `UserResponse.email` |
| phone | String | `UserResponse.phone` |
| company.name | String | `CompanyInfo.companyName` |

> ⚠️ Endpoint `/users` mengembalikan **array** (bukan objek tunggal) → tipe balikan Retrofit wajib `Response<List<UserResponse>>`, bukan `Response<UserResponse>`.

---

## 9. Skenario Pengujian (QA)

| # | Skenario | Hasil Diharapkan |
|---|---|---|
| 1 | Buka aplikasi pertama kali | ProgressBar tampil sejenak → 10 kartu user terpasang rapi |
| 2 | Ketik "ervin" | List menyusut, hanya "Ervin Howell" tampil |
| 3 | Hapus semua teks pencarian | Seluruh 10 data tampil kembali |
| 4 | Ketik "Crona" (nama perusahaan) | User dari perusahaan "Romaguera-Crona" ikut tampil |
| 5 | Ketik "Zulkifli" (tidak ada data cocok) | List kosong + pesan empty-state tampil |
| 6 | Matikan koneksi internet lalu buka aplikasi | Pesan error koneksi tampil, aplikasi tidak crash |

---

## 10. Risiko & Mitigasi

- **Ketergantungan pada API publik pihak ketiga** — jika JSONPlaceholder down, aplikasi hanya menampilkan error state (tidak ada fallback data lokal). Mitigasi: tangani secara eksplisit dengan pesan error yang jelas (sudah tercakup di F-08).
- **`notifyDataSetChanged()` kurang efisien** untuk skala besar — dapat diganti `DiffUtil` di iterasi berikutnya, namun untuk 10 data ini bukan prioritas.
- **Tidak ada caching offline** — di luar scope versi ini; dicatat sebagai backlog.

---

## 11. Backlog / Peningkatan Selanjutnya

- Migrasi filtering ke `DiffUtil` agar animasi list lebih halus.
- Debounce pada TextWatcher (mis. 250ms) untuk dataset besar.
- Caching lokal dengan Room untuk mode offline.
- Migrasi ke ViewBinding/Jetpack Compose.

---

## 12. Definition of Done

- [ ] Seluruh langkah 3.1–3.10 pada modul diimplementasikan dan berjalan tanpa crash.
- [ ] Tugas 1 (filter ganda nama/perusahaan) dan Tugas 2 (empty state) terintegrasi dan lulus 6 skenario QA di atas.
- [ ] Kode mengikuti struktur package `com.latihan.kurirdirectoryapp` dan penamaan sesuai modul.
- [ ] Build Gradle sukses sync tanpa error, aplikasi teruji di emulator API 24+.

---

*PRD ini disusun berdasarkan Modul Praktikum Dasar (Level 1) — Pertemuan 10.*
