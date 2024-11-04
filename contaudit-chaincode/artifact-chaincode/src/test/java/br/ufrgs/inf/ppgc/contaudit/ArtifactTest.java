package br.ufrgs.inf.ppgc.contaudit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.UUID;

class ArtifactTest {
    private Artifact artifact;
    private UUID uuid;
    
    @BeforeEach
    public void setUp() {
        artifact = new Artifact();
        uuid = UUID.randomUUID();
    }

    @Test
    void testSetAndGetId() {
        artifact.setId(uuid);
        assertEquals(uuid, artifact.getId(), "ID should match the UUID that was set.");
    }

    @Test
    void testSetAndGetApplication() {
        String applicationName = "TestApplication";
        artifact.setApplication(applicationName);
        assertEquals(applicationName, artifact.getApplication(), "Application name should match the one that was set.");
    }

    @Test
    void testSetAndGetName() {
        String name = "TestName";
        artifact.setName(name);
        assertEquals(name, artifact.getName(), "Name should match the one that was set.");
    }

    @Test
    void testSetAndGetHash() {
        String hash = "1234567890abcdef";
        artifact.setHash(hash);
        assertEquals(hash, artifact.getHash(), "Hash should match the one that was set.");
    }

    @Test
    void testSetAndGetContent() {
        String content = "This is a test content.";
        artifact.setContent(content);
        assertEquals(content, artifact.getContent(), "Content should match the one that was set.");
    }

    @Test
    void testToString() {
        artifact.setId(uuid);
        artifact.setApplication("TestApp");
        artifact.setName("TestArtifact");
        artifact.setHash("abcdef123456");
        artifact.setContent("Some content");

        String expectedString = "Artifact{id=" + uuid + ", application=TestApp, name = TestArtifact, hash = abcdef123456, content = Some content}";
        assertEquals(expectedString, artifact.toString(), "toString should return the correct formatted string.");
    }
}