package com.example.demo;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("notifikasiDitolakDelegate")
public class NotifikasiDitolakDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nama = (String) execution.getVariable("namaKaryawan");
        String departemen = (String) execution.getVariable("departemen");
        Number jumlahHari = (Number) execution.getVariable("jumlahHari");
        String catatan = (String) execution.getVariable("catatanReview");

        execution.setVariable("statusAkhir", "DITOLAK");

        System.out.println("==================================================");
        System.out.println("[SERVICE TASK] NOTIFIKASI: PENGAJUAN CUTI DITOLAK! \u274C");
        System.out.println("--------------------------------------------------");
        System.out.println("Pemberitahuan kepada : " + nama + " (" + departemen + ")");
        System.out.println("Durasi Cuti         : " + jumlahHari + " hari");
        System.out.println("Status              : DITOLAK");
        System.out.println("Alasan Penolakan    : " + (catatan != null ? catatan : "-"));
        System.out.println("==================================================");
    }
}
