package br.ufrgs.inf.ppgc.contaudit;

import org.junit.jupiter.api.Test;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class ApplicationTest {
    @Test
    void testSetAndGetId() {
        Application app = new Application();
        UUID id = UUID.randomUUID();
        app.setId(id);
        assertEquals(id, app.getId());
    }

    @Test
    void testSetAndGetName() {
        Application app = new Application();
        String name = "TestApp";
        app.setName(name);
        assertEquals(name, app.getName());
    }

    @Test
    void testSetAndGetVersion() {
        Application app = new Application();
        String version = "1.0.0";
        app.setVersion(version);
        assertEquals(version, app.getVersion());
    }

    @Test
    void testSetAndGetHash() {
        Application app = new Application();
        String hash = "abc123";
        app.setHash(hash);
        assertEquals(hash, app.getHash());
    }

    @Test
    void testToString() {
        Application app = new Application();
        UUID id = UUID.randomUUID();
        String name = "TestApp";
        String version = "1.0.0";
        String hash = "abc123";

        app.setId(id);
        app.setName(name);
        app.setVersion(version);
        app.setHash(hash);

        String expectedString = "Application{id=" + id + ", name = " + name + ", version = " + version + ", hash = " + hash + "}";
        assertEquals(expectedString, app.toString());
    }
}