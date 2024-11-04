package br.ufrgs.inf.ppgc.contaudit.wrapper.application;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ApplicationTest {
    private Application application;

    @BeforeEach
    void setUp() {
        application = new Application();
    }

    @Test
    void testSetAndGetId() {
        UUID testId = UUID.randomUUID();
        application.setId(testId);

        assertEquals(testId, application.getId());
    }

    @Test
    void testSetAndGetName() {
        String testName = "Test Application";
        application.setName(testName);

        assertEquals(testName, application.getName());
    }

    @Test
    void testSetAndGetVersion() {
        String testVersion = "1.0.0";
        application.setVersion(testVersion);

        assertEquals(testVersion, application.getVersion());
    }

    @Test
    void testSetAndGetFullPath() {
        String testFullPath = "/usr/local/bin/application";
        application.setFullPath(testFullPath);

        assertEquals(testFullPath, application.getFullPath());
    }

    @Test
    void testSetAndGetHash() {
        String testHash = "1234567890abcdef";
        application.setHash(testHash);

        assertEquals(testHash, application.getHash());
    }

    @Test
    void testToString() {
        UUID testId = UUID.randomUUID();
        String testName = "Test Application";
        String testVersion = "1.0.0";
        String testFullPath = "/usr/local/bin/application";
        String testHash = "1234567890abcdef";
        application.setId(testId);
        application.setName(testName);
        application.setVersion(testVersion);
        application.setFullPath(testFullPath);
        application.setHash(testHash);

        String expectedString = "Application{id=" + testId + ", name = " + testName + ", version = " + testVersion + ", fullPath = " + testFullPath + ", hash = " + testHash + "}";
        assertEquals(expectedString, application.toString());
    }
}