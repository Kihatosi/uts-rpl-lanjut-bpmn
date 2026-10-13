package com.example.demo;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.history.HistoricVariableInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/cuti")
@CrossOrigin(origins = "*")
public class CutiController {

    @Autowired
    private RuntimeService runtimeService;

    @Autowired
    private TaskService taskService;

    @Autowired
    private HistoryService historyService;

    // 1. Endpoint untuk mengajukan cuti (Start Process Instance)
    @PostMapping("/ajukan")
    public ResponseEntity<Map<String, Object>> ajukanCuti(@RequestBody Map<String, Object> payload) {
        String nama = (String) payload.getOrDefault("namaKaryawan", "Karyawan Tanpa Nama");
        String departemen = (String) payload.getOrDefault("departemen", "Umum");
        Number hariNum = (Number) payload.getOrDefault("jumlahHari", 1);
        int jumlahHari = hariNum.intValue();
        String alasan = (String) payload.getOrDefault("alasan", "Urusan pribadi");

        Map<String, Object> variables = new HashMap<>();
        variables.put("namaKaryawan", nama);
        variables.put("departemen", departemen);
        variables.put("jumlahHari", jumlahHari);
        variables.put("alasan", alasan);

        ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("pengajuanCutiProcess", variables);

        // Cek apakah langsung selesai (auto-approved <= 2 hari) atau berhenti di user task
        List<Task> activeTasks = taskService.createTaskQuery()
                .processInstanceId(processInstance.getId())
                .list();

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("processInstanceId", processInstance.getId());
        response.put("namaKaryawan", nama);
        response.put("jumlahHari", jumlahHari);

        if (activeTasks.isEmpty()) {
            response.put("pesan", "Pengajuan cuti berhasil diproses dan OTOMATIS DISETUJUI oleh sistem (<= 2 hari)!");
            response.put("statusCuti", "DISETUJUI (AUTO-APPROVED)");
            response.put("perluReviewManajer", false);
        } else {
            Task task = activeTasks.get(0);
            response.put("pesan", "Pengajuan cuti berhasil diajukan dan MENUNGGU PERSETUJUAN MANAJER (> 2 hari).");
            response.put("statusCuti", "PENDING_APPROVAL");
            response.put("perluReviewManajer", true);
            response.put("taskId", task.getId());
            response.put("taskName", task.getName());
        }

        return ResponseEntity.ok(response);
    }

    // 2. Endpoint untuk melihat daftar User Task yang butuh review manajer
    @GetMapping("/tasks")
    public ResponseEntity<List<Map<String, Object>>> getPendingTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("pengajuanCutiProcess")
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> item = new HashMap<>();
            item.put("taskId", task.getId());
            item.put("taskName", task.getName());
            item.put("processInstanceId", task.getProcessInstanceId());
            item.put("createTime", task.getCreateTime());
            item.put("assignee", task.getAssignee());

            // Ambil variabel dari process instance
            Map<String, Object> vars = runtimeService.getVariables(task.getProcessInstanceId());
            item.put("namaKaryawan", vars.get("namaKaryawan"));
            item.put("departemen", vars.get("departemen"));
            item.put("jumlahHari", vars.get("jumlahHari"));
            item.put("alasan", vars.get("alasan"));

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }

    // 3. Endpoint bagi Manajer untuk Approve atau Reject cuti
    @PostMapping("/tasks/{taskId}/review")
    public ResponseEntity<Map<String, Object>> reviewTask(
            @PathVariable String taskId,
            @RequestBody Map<String, Object> payload) {

        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) {
            Map<String, Object> err = new HashMap<>();
            err.put("status", "ERROR");
            err.put("pesan", "Task tidak ditemukan atau sudah selesai!");
            return ResponseEntity.badRequest().body(err);
        }

        boolean disetujui = Boolean.TRUE.equals(payload.get("disetujui"));
        String catatan = (String) payload.getOrDefault("catatanReview", disetujui ? "Disetujui manajer" : "Ditolak manajer");

        Map<String, Object> taskVariables = new HashMap<>();
        taskVariables.put("disetujui", disetujui);
        taskVariables.put("catatanReview", catatan);

        taskService.complete(taskId, taskVariables);

        Map<String, Object> response = new HashMap<>();
        response.put("status", "SUCCESS");
        response.put("taskId", taskId);
        response.put("disetujui", disetujui);
        response.put("catatanReview", catatan);
        response.put("pesan", disetujui ? "Cuti berhasil DISETUJUI oleh Manajer!" : "Cuti telah DITOLAK oleh Manajer!");

        return ResponseEntity.ok(response);
    }

    // 4. Endpoint riwayat proses pengajuan cuti (History)
    @GetMapping("/history")
    public ResponseEntity<List<Map<String, Object>>> getHistory() {
        List<HistoricProcessInstance> historicInstances = historyService.createHistoricProcessInstanceQuery()
                .processDefinitionKey("pengajuanCutiProcess")
                .orderByProcessInstanceStartTime().desc()
                .list();

        List<Map<String, Object>> result = new ArrayList<>();
        for (HistoricProcessInstance hpi : historicInstances) {
            Map<String, Object> item = new HashMap<>();
            item.put("processInstanceId", hpi.getId());
            item.put("startTime", hpi.getStartTime());
            item.put("endTime", hpi.getEndTime());
            item.put("isEnded", hpi.getEndTime() != null);

            // Ambil nilai variabel historis
            List<HistoricVariableInstance> vars = historyService.createHistoricVariableInstanceQuery()
                    .processInstanceId(hpi.getId())
                    .list();

            Map<String, Object> varMap = new HashMap<>();
            for (HistoricVariableInstance v : vars) {
                varMap.put(v.getName(), v.getValue());
            }

            item.put("namaKaryawan", varMap.get("namaKaryawan"));
            item.put("departemen", varMap.get("departemen"));
            item.put("jumlahHari", varMap.get("jumlahHari"));
            item.put("alasan", varMap.get("alasan"));
            item.put("statusAkhir", varMap.getOrDefault("statusAkhir", hpi.getEndTime() == null ? "MENUNGGU_APPROVAL" : "SELESAI"));
            item.put("catatanReview", varMap.get("catatanReview"));

            result.add(item);
        }

        return ResponseEntity.ok(result);
    }
}
