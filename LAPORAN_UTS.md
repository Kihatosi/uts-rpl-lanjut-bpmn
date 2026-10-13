# LAPORAN UJIAN TENGAH SEMESTER (UTS)
## REKAYASA PERANGKAT LUNAK LANJUT
**Topik: Pemodelan & Implementasi Business Process Management (BPM/BPMN) Menggunakan Framework Spring Boot (Jakarta EE / J2EE) dan Camunda BPM**

---

### IDENTITAS MAHASISWA & MATA KULIAH
* **Mata Kuliah:** Rekayasa Perangkat Lunak Lanjut
* **Dosen Pengampu:** Rizal Broer, M.Kom (`rizalbroer@uinjkt.ac.id`)
* **Program Studi:** Teknik Informatika
* **Fakultas:** Sains dan Teknologi, UIN Syarif Hidayatullah Jakarta
* **Semester / Tahun Akademik:** Semester 5 / 2026-2027
* **Nama Mahasiswa:** Rusmin Nuryadin
* **NIM:** 12409011050120

---

## BAB I: PENJELASAN 5W SPRING BOOT, BPM/BPMN, DAN KETERKAITANNYA DENGAN J2EE (BOBOT 10%)

### 1.1 Analisis 5W Framework Spring / Spring Boot
* **What (Apa):** Spring Boot adalah framework berbasis Java / Jakarta EE yang dirancang untuk mempermudah pembuatan aplikasi enterprise mandiri (*stand-alone*) dan siap produksi. Fitur utamanya adalah *Opinionated Auto-Configuration*, *Dependency Injection*, serta *Embedded Web Server* (Tomcat, Jetty).
* **Why (Mengapa):** Sebelum Spring Boot, konfigurasi J2EE konvensional sangat rumit (*XML hell*) dan membutuhkan server terpisah (*Application Server* seperti GlassFish atau WebLogic). Spring Boot menghilangkan kerumitan ini dan mempercepat *time-to-market*.
* **Who (Siapa):** Dikembangkan oleh tim Pivotal (sekarang bagian dari Broadcom/VMware) dan digunakan secara luas oleh jutaan software engineer enterprise di seluruh dunia.
* **Where (Di mana):** Diterapkan pada arsitektur microservices, backend RESTful web services, payment gateway, dan sistem informasi enterprise skala besar.
* **When (Kapan):** Diadopsi saat organisasi memerlukan platform pengembangan backend yang modular, scalable, mudah diuji secara otomatis (*automated testing*), dan terintegrasi baik dengan ekosistem cloud.

### 1.2 Analisis 5W Business Process Management (BPM) & BPMN
* **What (Apa):** Business Process Management (BPM) adalah disiplin manajerial dan teknologi perangkat lunak untuk memodelkan, mengotomatisasi, mengeksekusi, dan mengoptimalkan proses bisnis. Notasi standarnya adalah **BPMN 2.0 (Business Process Model and Notation)** yang distandarisasi oleh OMG (*Object Management Group*).
* **Why (Mengapa):** BPM memisahkan alur logika bisnis dari kode program (*hardcoded code*). Dengan demikian, perubahan kebijakan bisnis tidak mengharuskan tim developer merombak seluruh struktur kode, melainkan cukup memperbarui model proses bisnis secara visual.
* **Who (Siapa):** Digunakan oleh kolaborasi antara Business Analyst, System Analyst, dan Software Engineer.
* **Where (Di mana):** Dijalankan pada *Workflow / Process Engine* seperti Camunda BPM, Flowable, atau Activiti yang bertindak sebagai orkestrator transaksi.
* **When (Kapan):** Digunakan saat sistem memiliki rantai proses bisnis yang memiliki banyak tahapan (*multi-step*), melibatkan interaksi manusia (*human tasks*), batas waktu (*SLA/timer*), atau audit compliance yang ketat.

### 1.3 Keterkaitan dan Manfaat BPM dalam Pemrograman J2EE / Jakarta EE
Dalam arsitektur J2EE / Jakarta EE modern:
1. **Separation of Business Flow and Technical Execution:** BPM menangani status alur proses (*orchestration*), sementara Jakarta EE / Spring Boot menangani implementasi teknis (*service layer*, *persistence*, dan *security*).
2. **Human-in-the-Loop Workflow:** J2EE murni berorientasi pada request-response pendek (HTTP stateless). Dengan BPM, tugas yang membutuhkan penundaan waktu hingga manusia menyetujuinya (*User Task*) dapat disimpan state-nya secara otomatis (*state machine persistence*).
3. **Visibilitas dan Pemantauan Transaksi (Cockpit):** Status proses yang sedang berjalan (*process instance*) dan bottleneck proses dapat dipantau secara langsung melalui GUI Camunda Cockpit tanpa perlu menulis *query logging manual*.
4. **Keamanan Transaksional ACID:** Engine Camunda terhubung langsung dengan *Spring Transaction Management* (`PlatformTransactionManager`), sehingga kegagalan satu tahapan tugas dapat di-rollback dengan aman.

---

## BAB II: INSTALASI, KONFIGURASI TOOLS, DAN PENGUJIAN HELLO WORLD (BOBOT 10%)

### 2.1 Konfigurasi Stack Teknologi
* **JDK:** Java SE 21 LTS
* **Framework:** Spring Boot 3.3.6 (Jakarta EE 10 baseline)
* **BPM Engine:** Camunda BPM Spring Boot Starter 7.22.0
* **Database:** H2 Database Engine In-Memory (`jdbc:h2:mem:camunda`)
* **Build Tool:** Apache Maven 3.9+ (via `mvnw.cmd` wrapper)

### 2.2 Dependensi Kunci (`pom.xml`)
Dependensi dikonfigurasi untuk menyatukan Spring Web, Camunda REST Engine, Camunda Webapp, dan driver JDBC H2:
```xml
<dependency>
    <groupId>org.camunda.bpm.springboot</groupId>
    <artifactId>camunda-bpm-spring-boot-starter-webapp</artifactId>
    <version>7.22.0</version>
</dependency>
<dependency>
    <groupId>org.camunda.bpm.springboot</groupId>
    <artifactId>camunda-bpm-spring-boot-starter-rest</artifactId>
    <version>7.22.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
</dependency>
<dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
</dependency>
```

### 2.3 Konfigurasi `application.properties`
Dikonfigurasi admin default untuk Webapp Camunda:
* **Username Admin:** `demo`
* **Password:** `demo`
* **Filter Default:** `All tasks`

### 2.4 Pembuktian Pengujian "Hello World"
1. **Model BPMN:** `helloworld.bpmn` mendefinisikan `StartEvent` &rarr; `ServiceTask (id="Task_HelloWorld")` &rarr; `EndEvent`.
2. **Java Delegate:**
```java
@Component("helloWorld")
public class HelloWorldDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("==================================================");
        System.out.println("HELLO WORLD! INSTALASI CAMUNDA & SPRING BOOT SUKSES!");
        System.out.println("==================================================");
    }
}
```
3. **Hasil Eksekusi:** Endpoint `GET /test-hello` sukses memicu `ProcessInstance` dan mencetak output ke terminal dengan status HTTP 200 OK.

---

## BAB III: STUDI KASUS - SISTEM PENGAJUAN CUTI KARYAWAN (BOBOT 50%)

### 3.1 Latar Belakang & Deskripsi Studi Kasus
Proses pengajuan cuti karyawan (*Employee Leave Approval Process*) dipilih sebagai studi kasus karena mencerminkan proses bisnis nyata yang menggabungkan:
1. Otomasi sistem (*Service Task*).
2. Keputusan percabangan (*Exclusive Gateway*).
3. Partisipasi aktor manusia (*User Task*).
4. Persistensi riwayat audit (*Historic Variable & Process Instance*).

### 3.2 Spesifikasi Aturan Bisnis (*Business Rules*)
* **Input Formulir:** Karyawan mengisi Nama, Departemen, Jumlah Hari Cuti, dan Alasan.
* **Validasi Otomatis (Aturan 1):**
  * Jika durasi cuti **&le; 2 hari**, maka sistem secara otomatis menyetujui permohonan (*Auto-Approval*) tanpa intervensi manajer. Token langsung diarahkan ke notifikasi disetujui.
* **Review Manual Manajer (Aturan 2):**
  * Jika durasi cuti **&gt; 2 hari**, sistem membentuk **User Task** yang dialokasikan kepada akun `demo` (Manajer).
  * Manajer dapat meninjau data pengajuan lalu memilih opsi **Disetujui** (*Approved*) atau **Ditolak** (*Rejected*) beserta catatan manajerial.
* **Notifikasi & Pencatatan Akhir:**
  * Node Service Task mengeksekusi konfirmasi status akhir (`DISETUJUI` / `DITOLAK`) dan menyimpan ke database H2.

### 3.3 Rincian Komponen Arsitektur BPMN
| Elemen BPMN | ID Elemen | Tipe Komponen | Fungsi & Aksi |
| :--- | :--- | :--- | :--- |
| **Start Event** | `StartEvent_Pengajuan` | Start Event | Menginisiasi alur proses dengan variabel form |
| **Service Task** | `Task_Validasi` | JavaDelegate (`validasiCutiDelegate`) | Memvalidasi durasi dan mengevaluasi aturan bisnis |
| **Gateway 1** | `Gateway_CekHari` | Exclusive Gateway (XOR) | Percabangan: &le; 2 hari vs &gt; 2 hari |
| **User Task** | `Task_ReviewManajer` | User Task | Tugas peninjauan oleh akun manajer |
| **Gateway 2** | `Gateway_Keputusan` | Exclusive Gateway (XOR) | Percabangan keputusan: Disetujui vs Ditolak |
| **Service Task** | `Task_NotifikasiDisetujui` | JavaDelegate (`notifikasiDisetujuiDelegate`) | Logging notifikasi penerimaan & update status |
| **Service Task** | `Task_NotifikasiDitolak` | JavaDelegate (`notifikasiDitolakDelegate`) | Logging notifikasi penolakan & catatan alasan |
| **End Event** | `EndEvent_Disetujui` & `EndEvent_Ditolak` | End Event | Mengakhiri siklus hidup instance proses |

### 3.4 Antarmuka Web Dashboard & REST Controller
Disediakan antarmuka web modern pada `http://localhost:8080/` (`src/main/resources/static/index.html`) yang menyuguhkan:
1. Formulir pengajuan cuti dengan tombol uji cepat (*preset buttons*).
2. Panel peninjauan tugas aktif manajer dengan tombol aksi *Setujui* dan *Tolak*.
3. Tabel riwayat proses dan status terkini yang terhubung langsung ke Camunda History API.

---

## BAB IV: DOKUMENTASI HASIL PENGUJIAN, PRINT SCREEN, DAN REKAMAN (BOBOT 7.5%)

### 4.1 Log Pengujian Eksekusi Nyata
Berikut adalah log riil dari server Spring Boot yang membuktikan ketiga skenario berhasil dieksekusi:

**Skenario 1: Cuti 1 Hari (Auto-Approval Sistem):**
```text
==================================================
[SERVICE TASK] VALIDASI PENGAJUAN CUTI
--------------------------------------------------
Karyawan   : Ahmad Rusmi
Departemen : IT Engineering
Jumlah Hari: 1 hari
Alasan     : Keperluan keluarga
Evaluasi   : KUOTA AMAN (<= 2 hari). Auto-Approved!
==================================================
[SERVICE TASK] NOTIFIKASI: PENGAJUAN CUTI DISETUJUI!
Pemberitahuan kepada : Ahmad Rusmi (IT Engineering)
Durasi Cuti         : 1 hari
Status              : DISETUJUI
Catatan             : Disetujui otomatis oleh sistem (<= 2 hari)
==================================================
```

**Skenario 2: Cuti 5 Hari (Disetujui oleh Manajer):**
```text
==================================================
[SERVICE TASK] VALIDASI PENGAJUAN CUTI
--------------------------------------------------
Karyawan   : Siti Rahma
Departemen : Finance
Jumlah Hari: 5 hari
Evaluasi   : CUTI > 2 HARI. Memerlukan persetujuan Manajer!
==================================================
[SERVICE TASK] NOTIFIKASI: PENGAJUAN CUTI DISETUJUI!
Pemberitahuan kepada : Siti Rahma (Finance)
Durasi Cuti         : 5 hari
Status              : DISETUJUI
Catatan             : Disetujui oleh Pak Manager
==================================================
```

**Skenario 3: Cuti 7 Hari (Ditolak oleh Manajer):**
```text
==================================================
[SERVICE TASK] VALIDASI PENGAJUAN CUTI
--------------------------------------------------
Karyawan   : Bambang Wijaya
Departemen : Marketing
Jumlah Hari: 7 hari
Evaluasi   : CUTI > 2 HARI. Memerlukan persetujuan Manajer!
==================================================
[SERVICE TASK] NOTIFIKASI: PENGAJUAN CUTI DITOLAK!
Pemberitahuan kepada : Bambang Wijaya (Marketing)
Durasi Cuti         : 7 hari
Status              : DITOLAK
Alasan Penolakan    : Ditolak: Beban kuartal akhir sangat tinggi
==================================================
```

### 4.2 Tautan Hasil Tangkapan Layar & Video
* **Folder Tangkapan Layar:** Berada pada folder `docs/screenshots/` (atau dilampirkan pada laporan PDF).
* **Tautan Rekaman Demonstrasi (YouTube / Google Drive):** `[Masukkan Link Rekaman Video Anda di sini]`

---

## BAB V: PENERAPAN PRINSIP ABC DALAM PENULISAN LAPORAN (BOBOT 7.5%)

Penulisan laporan ini mengadopsi standar penulisan teknis **Prinsip ABC**:
1. **Accuracy (Akurat):**
   * Semua versi pustaka tertera secara eksplisit dan tervalidasi berjalan tanpa konflik dependensi.
   * Diagram BPMN 2.0 memenuhi kaidah formal sintaks XML OMG dan standar Camunda engine.
   * Parameter input dan output REST API disajikan dengan format JSON yang akurat.
2. **Brevity (Ringkas & Padat):**
   * Setiap bab langsung menjawab inti pertanyaan yang diuji tanpa menyertakan narasi yang tidak relevan.
   * Penggunaan tabel matriks mempermudah pembacaan spesifikasi tanpa membutuhkan paragraf panjang.
3. **Clarity (Jelas & Sistematis):**
   * Struktur bab mengikuti urutan nomor soal 1 hingga 6 secara berurutan.
   * Disertai panduan langkah demi langkah (*reproducible steps*) yang dapat diuji ulang oleh Dosen Penguji kapan saja.

---

## BAB VI: PENGGUNAAN TOOLS GITHUB DAN SHARING REPOSITORY (BOBOT 15%)

### 6.1 Tautan Repositori GitHub
* **URL GitHub Repository:** `https://github.com/Kihatosi/uts-rpl-lanjut-bpmn`
* **Branch Utama:** `main`

### 6.2 Konfigurasi Akses Kolaborator Dosen
Repository telah dipersiapkan untuk dibagikan kepada Dosen Pengampu:
* **Email Dosen Penerima:** `rizalbroer@uinjkt.ac.id`
* **Metode Kolaborasi:**
  1. Menambahkan akun sebagai **Collaborator** melalui menu *Settings* &rarr; *Collaborators* &rarr; *Add people* di GitHub.
  2. Mengirimkan konfirmasi tautan repositori melalui email resmi universitas kepada Bapak Rizal Broer, M.Kom.

---

### KESIMPULAN
Proyek UTS Rekayasa Perangkat Lunak Lanjut ini telah sukses mengintegrasikan:
1. Pemodelan alur kerja bisnis standar BPMN 2.0.
2. Orkestrasi otomatis dengan Spring Boot 3 dan Java 21 via Camunda Engine.
3. Antarmuka web interaktif yang memudahkan pengujian menyeluruh skenario otomatisasi maupun peninjauan manual oleh manusia.
4. Dokumentasi teknis terstruktur dengan prinsip ABC serta manajemen versioning berbasis Git dan GitHub.
