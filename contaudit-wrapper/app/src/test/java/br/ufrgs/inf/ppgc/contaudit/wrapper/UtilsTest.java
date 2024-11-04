package br.ufrgs.inf.ppgc.contaudit.wrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Collections;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

class UtilsTest {
    @BeforeEach
    void setUp() {
        System.clearProperty("java.class.path");
    }

    @Test
    void testConstructor() {
        try {
            java.lang.reflect.Constructor<Utils> constructor = Utils.class.getDeclaredConstructor();
            constructor.setAccessible(true);
            Utils utils = constructor.newInstance();
            assertNotNull(utils);
        } catch (Exception e) {
            fail("Construtor protegido não deveria lançar exceção: " + e.getMessage());
        }
    }

    @Test
    void testIsDebug_withDebugMode() {
        RuntimeMXBean mockRuntimeMXBean = mock(RuntimeMXBean.class);
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(Collections.singletonList("jdwp"));
    
        try (MockedStatic<ManagementFactory> mockedManagementFactory = mockStatic(ManagementFactory.class)) {
            mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
            assertTrue(Utils.isDebug());
        }
    }

    @Test
    void testIsDebug_withoutDebugMode() {
        RuntimeMXBean mockRuntimeMXBean = mock(RuntimeMXBean.class);
        when(mockRuntimeMXBean.getInputArguments()).thenReturn(Collections.emptyList());
    
        try (MockedStatic<ManagementFactory> mockedManagementFactory = mockStatic(ManagementFactory.class)) {
            mockedManagementFactory.when(ManagementFactory::getRuntimeMXBean).thenReturn(mockRuntimeMXBean);
            assertFalse(Utils.isDebug());
        }
    }

    @Test
    void testDateFormatter() {
        SimpleDateFormat sdf = Utils.dateFormatter();
        assertEquals("yyyy_MM_dd_HH_mm_ss_SSS", sdf.toPattern());
    }

    @Test
    void testTransformInputStreamToString_withShowDate() {
        String input = "First line\nSecond line";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        String result = Utils.transformInputStreamToString(inputStream, true);
    
        assertTrue(result.contains("First line"));
        assertTrue(result.contains("Second line"));
        assertTrue(result.contains(": "));
    }

    @Test
    void testTransformInputStreamToString_withoutShowDate() {
        String input = "First line\nSecond line";
        InputStream inputStream = new ByteArrayInputStream(input.getBytes(StandardCharsets.UTF_8));
        String result = Utils.transformInputStreamToString(inputStream, false);
    
        assertEquals("First line\nSecond line\n", result);
    }

    @Test
    void testFormatLog_withShowDate() {
        String message = "Test log message";
        String formattedLog = Utils.formatLog(message, true);
    
        assertTrue(formattedLog.contains("Test log message"));
        assertTrue(formattedLog.contains(": "));
    }
    
    @Test
    void testFormatLog_withoutShowDate() {
        String message = "Test log message";
        String formattedLog = Utils.formatLog(message, false);
    
        assertEquals("Test log message\n", formattedLog);
    }
}