# LAPORAN UTS
## Rekayasa Perangkat Lunak Lanjut
Pemodelan & Implementasi BPM/BPMN pakai Spring Boot dan Camunda BPM

---

### Identitas
- Mata Kuliah: Rekayasa Perangkat Lunak Lanjut
- Dosen: Rizal Broer, M.Kom (rizalbroer@uinjkt.ac.id)
- Prodi: Teknik Informatika
- Fakultas: Sains dan Teknologi, UIN Syarif Hidayatullah Jakarta
- Semester: 5 / 2026-2027
- Nama: Rusmin Nuryadin
- NIM: 12409011050120

---

## BAB I: Penjelasan 5W (Bobot 10%)

### 1.1 Spring / Spring Boot

- **What:** Spring Boot itu framework Java buat bikin aplikasi enterprise yang bisa langsung jalan tanpa ribet konfigurasi. Ada fitur auto-configuration, dependency injection, dan embedded web server (Tomcat/Jetty), jadi ga perlu setup server terpisah.
- **Why:** Dulu konfigurasi J2EE itu panjang banget, banyak file XML. Spring Boot ngilangin semua itu dan bikin development jadi lebih cepat.
- **Who:** Dibuat sama Pivotal (sekarang bagian Broadcom/VMware), dipake sama banyak developer enterprise.
- **Where:** Banyak dipake buat microservices, REST API backend, payment gateway, dan sistem informasi perusahaan.
- **When:** Cocok kalau butuh backend yang modular, scalable, dan gampang di-test.

### 1.2 BPM & BPMN

- **What:** BPM itu cara buat memodelkan dan ngotomasi proses bisnis. Standar visualnya BPMN 2.0, dikelola sama OMG (Object Management Group).
- **Why:** Dengan BPM, logika bisnis dipisah dari kode program. Jadi kalau aturannya berubah, tinggal edit diagramnya aja, ga perlu bongkar kode.
- **Who:** Dipake bareng-bareng sama Business Analyst, System Analyst, dan Developer.
- **Where:** Jalan di process engine kayak Camunda, Flowable, atau Activiti.
- **When:** Dipake kalau sistemnya punya banyak tahapan, ada approval manusia, batas waktu SLA, atau perlu audit trail.

### 1.3 Keterkaitan keduanya di J2EE

Di arsitektur J2EE modern, Spring Boot dan BPM saling melengkapi:

1. BPM ngatur alur proses (urutan langkah, gateway keputusan, SLA). Spring Boot ngurus implementasi teknisnya (akses database, keamanan, integrasi API).
2. J2EE biasa itu stateless (request-response aja). Dengan BPM, tugas yang butuh nunggu manusia approve (User Task) bisa disimpan state-nya otomatis.
3. Proses yang lagi jalan bisa dipantau lewat Camunda Cockpit tanpa perlu bikin query logging sendiri.
4. Camunda terhubung sama Spring Transaction Management, jadi kalau satu tahap gagal, bisa di-rollback.

---

## BAB II: Instalasi, Konfigurasi, dan Hello World (Bobot 10%)

### 2.1 Stack yang dipake

- JDK: Java 21 LTS
- Framework: Spring Boot 3.3.6 (Jakarta EE 10)
- BPM Engine: Camunda 7.22.0
- Database: H2 In-Memory (`jdbc:h2:mem:camunda`)
- Build Tool: Maven (lewat `mvnw.cmd`)

### 2.2 Dependency utama di pom.xml

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

### 2.3 Konfigurasi application.properties

Admin Camunda di-set username `demo`, password `demo`, filter default `All tasks`.

### 2.4 Hello World

1. File BPMN: `helloworld.bpmn` isinya StartEvent → ServiceTask → EndEvent.
2. Java Delegate:
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
3. Akses `GET /test-hello`, muncul respon sukses di browser dan output di terminal. Berarti instalasi berhasil.

---

## BAB III: Studi Kasus - Sistem Pengajuan Cuti (Bobot 50%)

### 3.1 Kenapa pilih kasus ini

Proses pengajuan cuti cocok buat studi kasus karena ada kombinasi otomasi sistem (Service Task), keputusan percabangan (Exclusive Gateway), dan keterlibatan manusia (User Task).

### 3.2 Aturan bisnisnya

- Karyawan isi form: Nama, Departemen, Jumlah Hari, Alasan.
- Kalau cuti **<= 2 hari**, langsung di-approve otomatis sama sistem.
- Kalau cuti **> 2 hari**, masuk ke User Task. Manajer harus review dan bisa setujui atau tolak.
- Setelah ada keputusan, sistem kirim notifikasi dan catat ke log.

### 3.3 Komponen BPMN

| Elemen | ID | Tipe | Fungsi |
| :--- | :--- | :--- | :--- |
| Start Event | `StartEvent_Pengajuan` | Start Event | Mulai proses, terima variabel form |
| Service Task | `Task_Validasi` | JavaDelegate (`validasiCutiDelegate`) | Cek durasi cuti |
| Gateway 1 | `Gateway_CekHari` | Exclusive Gateway | Percabangan: <= 2 hari atau > 2 hari |
| User Task | `Task_ReviewManajer` | User Task | Manajer review pengajuan |
| Gateway 2 | `Gateway_Keputusan` | Exclusive Gateway | Setuju atau tolak |
| Service Task | `Task_NotifikasiDisetujui` | JavaDelegate | Notifikasi approve |
| Service Task | `Task_NotifikasiDitolak` | JavaDelegate | Notifikasi reject |
| End Event | `EndEvent_Disetujui` / `EndEvent_Ditolak` | End Event | Proses selesai |

### 3.4 Web dashboard

Ada halaman web di `http://localhost:8080/` (`src/main/resources/static/index.html`) yang isinya:
1. Form pengajuan cuti dengan tombol preset buat testing cepat.
2. Panel tugas manajer dengan tombol Setujui/Tolak.
3. Tabel riwayat proses yang ambil data dari Camunda History API.

---

## BAB IV: Hasil Pengujian dan Dokumentasi (Bobot 7.5%)

### 4.1 Log pengujian

Ini log asli dari server waktu testing tiga skenario:

**Skenario 1 - Cuti 1 hari (auto-approve):**
```
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

**Skenario 2 - Cuti 5 hari (disetujui manajer):**
```
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

**Skenario 3 - Cuti 7 hari (ditolak manajer):**
```
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

### 4.2 Screenshot & Video

- Screenshot ada di folder `docs/screenshots/` atau dilampirin di laporan PDF.

---

## BAB V: Prinsip ABC (Bobot 7.5%)

Laporan ini ditulis pakai prinsip ABC:

1. **Accuracy:** Semua versi library yang dipake udah dicek kompatibel dan ga ada konflik. BPMN-nya valid XML sesuai standar Camunda. Response API udah dicek formatnya bener.
2. **Brevity:** Penjelasan langsung ke inti, pake tabel dan poin biar ga perlu paragraf panjang.
3. **Clarity:** Urutan bab ngikutin nomor soal. Command yang ditampilin bisa langsung di-copy-paste buat dicoba ulang.

---

## BAB VI: Git & GitHub (Bobot 15%)

### 6.1 Repository

- URL: `https://github.com/Kihatosi/uts-rpl-lanjut-bpmn`
- Branch: `main`

### 6.2 Sharing akses ke dosen

Repo udah disiapkan buat di-share ke dosen:
- Email dosen: `rizalbroer@uinjkt.ac.id`
- Caranya: Settings > Collaborators > Add people, masukin email dosen. Kalau repo public, tinggal kirim linknya aja lewat email.

---

### Kesimpulan

Project UTS ini mengintegrasikan BPMN 2.0 dengan Spring Boot 3 dan Camunda sebagai process engine. Ada web interface buat testing skenario auto-approve dan manual review manajer. Semua didokumentasiin dan di-push ke GitHub.
