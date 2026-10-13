# UTS RPL Lanjut - Sistem Pengajuan Cuti

Project UTS mata kuliah Rekayasa Perangkat Lunak Lanjut. Implementasi sistem pengajuan cuti karyawan pakai Spring Boot + Camunda BPM.

## Tech Stack

- Java 21
- Spring Boot 3.3.6
- Camunda BPM 7.22.0
- H2 Database (in-memory)

## Cara jalanin

```powershell
cd demo
.\mvnw.cmd spring-boot:run
```

Tunggu sampe muncul `Started DemoApplication`, terus buka browser:

- Web app: http://localhost:8080/
- Camunda Cockpit: http://localhost:8080/camunda/app/welcome/default/ (login `demo` / `demo`)
- Test Hello World: http://localhost:8080/test-hello
- H2 Console: http://localhost:8080/h2-console

## Fitur

- Form pengajuan cuti karyawan
- Auto-approve kalau cuti <= 2 hari
- Review manajer kalau cuti > 2 hari (approve/reject lewat Camunda Tasklist atau web portal)
- Riwayat proses dan status pengajuan

## API Endpoint

| Method | URL | Fungsi |
| :--- | :--- | :--- |
| `POST` | `/api/cuti/ajukan` | Submit pengajuan cuti |
| `GET` | `/api/cuti/tasks` | Daftar tugas manajer |
| `POST` | `/api/cuti/tasks/{taskId}/review` | Approve/reject |
| `GET` | `/api/cuti/history` | Riwayat proses |

## Identitas

- Nama: Rusmin Nuryadin
- NIM: 12409011050120
- Dosen: Rizal Broer, M.Kom
