package com.example.demo;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("validasiCutiDelegate")
public class ValidasiCutiDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) throws Exception {
        String nama = (String) execution.getVariable("namaKaryawan");
        String departemen = (String) execution.getVariable("departemen");
        Number jumlahHariNum = (Number) execution.getVariable("jumlahHari");
        int jumlahHari = jumlahHariNum != null ? jumlahHariNum.intValue() : 1;
        String alasan = (String) execution.getVariable("alasan");

        System.out.println("==================================================");
        System.out.println("[SERVICE TASK] VALIDASI PENGAJUAN CUTI");
        System.out.println("--------------------------------------------------");
        System.out.println("Karyawan   : " + nama);
        System.out.println("Departemen : " + departemen);
        System.out.println("Jumlah Hari: " + jumlahHari + " hari");
        System.out.println("Alasan     : " + alasan);

        // Aturan Bisnis (Business Rule):
        // Jika cuti <= 2 hari -> Auto-Approved oleh sistem
        // Jika cuti > 2 hari  -> Butuh Approval Manual dari Manajer (User Task)
        boolean perluReviewManajer = jumlahHari > 2;
        execution.setVariable("perluPersetujuanManajer", perluReviewManajer);

        if (!perluReviewManajer) {
            execution.setVariable("disetujui", true);
            execution.setVariable("catatanReview", "Disetujui otomatis oleh sistem (<= 2 hari)");
            System.out.println("Evaluasi   : KUOTA AMAN (<= 2 hari). Auto-Approved!");
        } else {
            execution.setVariable("disetujui", false);
            execution.setVariable("catatanReview", "Menunggu persetujuan manajer");
            System.out.println("Evaluasi   : CUTI > 2 HARI. Memerlukan persetujuan Manajer!");
        }
        System.out.println("==================================================");
    }
}
