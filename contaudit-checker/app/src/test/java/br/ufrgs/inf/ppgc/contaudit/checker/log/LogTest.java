package br.ufrgs.inf.ppgc.contaudit.checker.log;

import static org.junit.jupiter.api.Assertions.*;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import br.ufrgs.inf.ppgc.contaudit.checker.application.Application;
import br.ufrgs.inf.ppgc.contaudit.checker.application.artifact.Artifact;

class LogTest {
    private Log log;
    private String command;
    private Application mockApplication;
    private List<Artifact> artifacts;
    private String environmentPreStateHash;
    private String output;
    private String environmentPostStateHash;
    private String environmentDiff;

    @BeforeEach
    void setUp() {
        command = "testCommand";
        mockApplication = new Application();
        artifacts = Arrays.asList(new Artifact(), new Artifact());
        environmentPreStateHash = "preStateHash";
        output = "outputData";
        environmentPostStateHash = "postStateHash";
        environmentDiff = "environmentDiffData";

        log = new Log(command, mockApplication, artifacts, environmentPreStateHash, output, environmentPostStateHash, environmentDiff);
    }

    @Test
    void testLogConstructor() {
        assertNotNull(log.getId());
        assertEquals(command, log.getCommand());
        assertEquals(mockApplication, log.getApplication());
        assertEquals(artifacts, log.getArtifacts());
        assertEquals(environmentPreStateHash, log.getEnvironmentPreStateHash());
        assertEquals(output, log.getOutput());
        assertEquals(environmentPostStateHash, log.getEnvironmentPostStateHash());
        assertEquals(environmentDiff, log.getEnvironmentDiff());
    }

    @Test
    void testGetId() {
        assertNotNull(log.getId());
    }

    @Test
    void testGetCommand() {
        assertEquals(command, log.getCommand());
    }

    @Test
    void testGetApplication() {
        assertEquals(mockApplication, log.getApplication());
    }

    @Test
    void testGetArtifacts() {
        assertEquals(artifacts, log.getArtifacts());
    }

    @Test
    void testGetEnvironmentPreStateHash() {
        assertEquals(environmentPreStateHash, log.getEnvironmentPreStateHash());
    }

    @Test
    void testGetOutput() {
        assertEquals(output, log.getOutput());
    }

    @Test
    void testGetEnvironmentPostStateHash() {
        assertEquals(environmentPostStateHash, log.getEnvironmentPostStateHash());
    }

    @Test
    void testGetEnvironmentDiff() {
        assertEquals(environmentDiff, log.getEnvironmentDiff());
    }
}