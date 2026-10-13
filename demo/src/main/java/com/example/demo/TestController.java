package com.example.demo;

import org.camunda.bpm.engine.RuntimeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TestController {

    @Autowired
    private RuntimeService runtimeService;

    @GetMapping("/test-hello")
    public String testHelloWorld() {
        runtimeService.startProcessInstanceByKey("ProsesHelloWorld");
        return "Proses Hello World Berhasil! Silakan cek terminal IDE Anda.";
    }
}