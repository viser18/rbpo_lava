package com.example.support.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class CertificateServiceTest {

    @InjectMocks
    private CertificateService certificateService;

    @Test
    void testValidateCertificate_ValidInput() {
        assertTrue(certificateService.validateCertificate("testAlias", "password123"));
    }

    @Test
    void testValidateCertificate_InvalidAlias() {
        assertFalse(certificateService.validateCertificate("", "password123"));
        assertFalse(certificateService.validateCertificate(null, "password123"));
    }

    @Test
    void testValidateCertificate_InvalidPassword() {
        assertFalse(certificateService.validateCertificate("testAlias", "short"));
    }

    @Test
    void testGetStudentInfo() {
        String studentId = "1БИБ23251";
        String result = certificateService.getStudentInfo(studentId);
        assertTrue(result.contains(studentId));
        assertTrue(result.contains("Support System"));
    }
}