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

## BAB I: Penjelasan 5W 

### 1.1 Spring / Spring Boot

- **What:** Framework Java open source untuk membangun aplikasi enterprise dan microservices yang berjalan mandiri (stand-alone) dengan konfigurasi minimal. Menyediakan Dependency Injection (DI), Inversion of Control (IoC), dan Auto-Configuration.
- **Why:** Mengurangi kode boilerplate dan konfigurasi J2EE tradisional yang berbasis XML. Auto-configuration dan embedded servlet container (Tomcat) membuat aplikasi bisa langsung dijalankan.
- **Who:**Dikembangkan oleh Pivotal Team (sekarang VMware/Broadcom). Penggunanya software engineer dan Java developer yang membangun solusi enterprise.
- **Where:** Sisi backend, mulai dari aplikasi web monolitik sampai arsitektur microservices terdistribusi dan cloud-native.
- **When:** Saat tim membutuhkan Rapid Application Development (RAD) agar bisa fokus pada logika bisnis, bukan konfigurasi infrastruktur.

### 1.2 BPM & BPMN

- **What:** BPM adalah metodologi untuk mengelola, mengotomatisasi, dan mengoptimalkan proses bisnis. BPMN 2.0 adalah standar internasional untuk memodelkan alur kerja bisnis secara grafis, dan modelnya bisa langsung dieksekusi oleh engine (executable flowchart).
- **Why:** Menjembatani komunikasi antara analis bisnis yang merancang alur kebutuhan dan developer yang mengimplementasikannya dalam kode.
- **Who:** Dirancang oleh business analyst atau system analyst, lalu diimplementasikan software engineer dengan process engine seperti Camunda.
- **Where:** Sistem enterprise dengan workflow multi-langkah, misalnya approval berjenjang, routing dokumen, dan orkestrasi transaksi antarsistem.
- **When:** Saat aturan proses sering berubah, perlu pelacakan (audit tracking) real-time, dan melibatkan banyak pihak, baik manusia maupun sistem otomatis.

### 1.3 Keterkaitan keduanya di J2EE

Pada pemrograman J2EE/Spring tradisional, alur bisnis yang rumit sering berakhir sebagai spaghetti code: banyak percabangan IF/ELSE manual dan manipulasi status langsung di database. Dengan Camunda BPM 7.22 yang diintegrasikan ke Spring Boot, ada empat manfaat:

1.	Separation of concerns. Routing status, gateway keputusan, timer, dan eskalasi ditangani file model BPMN, sedangkan Spring Boot hanya menjalankan tugas teknis lewat Java Delegate (Spring Bean).
2.	Human-in-the-loop. User Task tersimpan persisten tanpa kehilangan state dan bisa dikerjakan lewat Camunda Tasklist atau portal web buatan sendiri.
3.	Visibilitas real-time. Posisi token setiap instance proses bisa dipantau di Camunda Cockpit, sehingga audit dan troubleshooting tidak perlu bergantung pada log manual.
4.	Transaksi ACID. Camunda terhubung dengan TransactionManager Spring Boot, sehingga data tetap konsisten kalau terjadi error di tengah proses.


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
