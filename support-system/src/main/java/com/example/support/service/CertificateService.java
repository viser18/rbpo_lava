package com.example.support.service;

import org.springframework.stereotype.Service;

@Service
public class CertificateService {

    public boolean validateCertificate(String alias, String password) {
        // Логика проверки сертификата
        return alias != null && !alias.isEmpty()
                && password != null && password.length() >= 8;
    }

    public String getStudentInfo(String studentId) {
        return "Student ID: " + studentId + " - Support System User";
    }
}