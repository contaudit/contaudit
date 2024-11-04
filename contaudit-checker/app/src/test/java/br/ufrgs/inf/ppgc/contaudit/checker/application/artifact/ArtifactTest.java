package br.ufrgs.inf.ppgc.contaudit.checker.application.artifact;

import static org.junit.jupiter.api.Assertions.*;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ArtifactTest {
    private Artifact artifact;

    @BeforeEach
    void setUp() {
        artifact = new Artifact();
    }

    @Test
    void testSetAndGetId() {
        UUID testId = UUID.randomUUID();
        artifact.setId(testId);

        assertEquals(testId, artifact.getId());
    }

    @Test
    void testSetAndGetApplication() {
        String testApplication = "Test Application";
        artifact.setApplication(testApplication);

        assertEquals(testApplication, artifact.getApplication());
    }

    @Test
    void testSetAndGetName() {
        String testName = "Test Name";
        artifact.setName(testName);

        assertEquals(testName, artifact.getName());
    }

    @Test
    void testSetAndGetFullPath() {
        String testFullPath = "/usr/local/bin/artifact";
        artifact.setFullPath(testFullPath);

        assertEquals(testFullPath, artifact.getFullPath());
    }

    @Test
    void testSetAndGetHash() {
        String testHash = "1234567890abcdef";
        artifact.setHash(testHash);

        assertEquals(testHash, artifact.getHash());
    }

    @Test
    void testSetAndGetContent() {
        String testContent = "Artifact Content";
        artifact.setContent(testContent);

        assertEquals(testContent, artifact.getContent());
    }

    @Test
    void testToString() {
        UUID testId = UUID.randomUUID();
        String testApplication = "Test Application";
        String testName = "Test Name";
        String testFullPath = "/usr/local/bin/artifact";
        String testHash = "1234567890abcdef";
        String testContent = "Artifact Content";
        artifact.setId(testId);
        artifact.setApplication(testApplication);
        artifact.setName(testName);
        artifact.setFullPath(testFullPath);
        artifact.setHash(testHash);
        artifact.setContent(testContent);

        String expectedString = "Artifact{id=" + testId + ", applicationVersion=" + testApplication + ", name = " + testName + ", fullPath = " + testFullPath + ", hash = " + testHash + ", content = " + testContent + "}";
        assertEquals(expectedString, artifact.toString());
    }
}