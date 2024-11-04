package br.ufrgs.inf.ppgc.contaudit.checker;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import java.io.Console;
import java.io.IOException;
import java.security.InvalidParameterException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import br.ufrgs.inf.ppgc.contaudit.checker.environment.EnvironmentService;

class StartupTest {
    private Startup startup;
    private EnvironmentService mockEnvService;
    private Console mockConsole;

    @BeforeEach
    public void setUp() {
        mockEnvService = mock(EnvironmentService.class);
        mockConsole = mock(Console.class);
        startup = new Startup(mockEnvService);
        startup.console = mockConsole;
        startup.logger = mock(Logger.class);
    }
    
    @Test
    void testRunWithValidInputs() {
        when(mockConsole.readLine()).thenReturn("0");

        startup.run();

        verify(startup.logger).info("Initializing ContAudIT Checker...");
        verify(startup.logger).info("Finishing ContAudIT Checker...");
    }

    @Test
    void testRunInterruptedException() throws InterruptedException, IOException {
        doThrow(new InterruptedException("Thread interrupted")).when(mockEnvService).saveCurrentState();
        when(mockConsole.readLine()).thenReturn("1");

        startup.run();

        verify(startup.logger).error("Thread interrupted");
        assertTrue(Thread.currentThread().isInterrupted());
    }

    @Test
    void testRunException() throws InterruptedException, IOException {
        doThrow(new IOException("IO Exception")).when(mockEnvService).saveCurrentState();
        when(mockConsole.readLine()).thenReturn("1");

        startup.run();

        verify(startup.logger).error("IO Exception");
    }

    @Test
    void testShowMenuExit() throws InterruptedException, IOException {
        when(mockConsole.readLine()).thenReturn("0");

        int result = startup.showMenu();

        assertEquals(0, result);
    }

    @Test
    void testShowMenuSaveEnvironmentState() throws InterruptedException, IOException {
        when(mockConsole.readLine()).thenReturn("1");

        int result = startup.showMenu();

        verify(mockEnvService).saveCurrentState();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuValidateStateByLastHashStored() throws InterruptedException, IOException {
        when(mockConsole.readLine()).thenReturn("2");

        int result = startup.showMenu();

        verify(mockEnvService).validateStateByLastHashStored();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuValidateStateByCalculatedHash() throws InterruptedException, IOException {
        when(mockConsole.readLine()).thenReturn("3");

        int result = startup.showMenu();

        verify(mockEnvService).validateStateByCalculatedHash();
        assertEquals(1, result);
    }

    @Test
    void testShowMenuInvalidOption() {
        when(mockConsole.readLine()).thenReturn("invalid");

        assertThrows(InvalidParameterException.class, () -> startup.showMenu());
    }
}