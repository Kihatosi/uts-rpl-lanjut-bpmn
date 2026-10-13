package com.example.demo;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("helloWorld")
public class HelloWorldDelegate implements JavaDelegate {
    @Override
    public void execute(DelegateExecution execution) throws Exception {
        System.out.println("==================================================");
        System.out.println("HELLO WORLD! INSTALASI CAMUNDA & SPRING BOOT SUKSES!");
        System.out.println("==================================================");
    }
}