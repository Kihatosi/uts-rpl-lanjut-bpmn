package com.example.demo;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("notifikasiDisetujuiDelegate")
public class NotifikasiDisetujuiDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nama = (String) execution.getVariable("namaKaryawan");
        String departemen = (String) execution.getVariable("departemen");
        Number jumlahHari = (Number) execution.getVariable("jumlahHari");
        String catatan = (String) execution.getVariable("catatanReview");

        execution.setVariable("statusAkhir", "DISETUJUI");

        System.out.println("==================================================");
        System.out.println("[SERVICE TASK] NOTIFIKASI: PENGAJUAN CUTI DISETUJUI! \u2705");
        System.out.println("--------------------------------------------------");
        System.out.println("Pemberitahuan kepada : " + nama + " (" + departemen + ")");
        System.out.println("Durasi Cuti         : " + jumlahHari + " hari");
        System.out.println("Status              : DISETUJUI");
        System.out.println("Catatan             : " + (catatan != null ? catatan : "-"));
        System.out.println("==================================================");
    }
}
