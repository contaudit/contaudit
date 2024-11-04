package br.ufrgs.inf.ppgc.contaudit.wrapper.wrapper;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class WrapperTest {
    private Wrapper wrapper;

    @BeforeEach
    void setUp() {
        wrapper = new Wrapper();
    }

    @Test
    void testSetHash() {
        String testHash = "testHashValue";
        wrapper.setHash(testHash);

        assertEquals(testHash, wrapper.getHash());
    }

    @Test
    void testGetHash_withoutSetting() {
        assertNull(wrapper.getHash());
    }

    @Test
    void testSetHash_nullValue() {
        wrapper.setHash(null);

        assertNull(wrapper.getHash());
    }

    @Test
    void testSetHash_emptyValue() {
        String emptyHash = "";
        wrapper.setHash(emptyHash);

        assertEquals(emptyHash, wrapper.getHash());
    }

    @Test
    void testToString() {
        String expectedHash = "12345abcde";
        wrapper.setHash(expectedHash);

        String result = wrapper.toString();

        assertEquals("Wrapper{hash=12345abcde}", result, "The toString method should return the correct string representation.");
    }
}