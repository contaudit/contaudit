package br.ufrgs.inf.ppgc.contaudit.wrapper;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import org.hyperledger.fabric.sdk.exception.InvalidArgumentException;
import org.junit.jupiter.api.*;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.slf4j.MDC;

class StartupTest {
    private MockedStatic<Utils> utilsMock;

    @BeforeEach
    void setUp() {
        utilsMock = Mockito.mockStatic(Utils.class);
    }

    @AfterEach
    void tearDown() {
        utilsMock.close();
    }

    @Test
    void testMainWithValidArgs() throws Exception {
        utilsMock.when(Utils::isDebug).thenReturn(false);
        utilsMock.when(Utils::currentDirectory).thenReturn("/mocked/directory");
        String[] args = {"Instance1", "SomeCommand"};
        Command commandMock = mock(Command.class);
        CommandFactory commandFactoryMock = mock(CommandFactory.class);
        when(commandFactoryMock.createCommand(anyString())).thenReturn(commandMock);

        Startup.main(args);
        Startup.executeCommand(args[1], commandFactoryMock);

        assertEquals("Instance1", Startup.getInstanceName());
        assertEquals("Instance1", MDC.get("prefix"));
        verify(commandMock).execute();
    }

    @Test
    void testMainWithInvalidArgsCatchesInvalidArgumentException() {
        utilsMock.when(Utils::isDebug).thenReturn(false);
        String[] args = {"Instance1"};
        Logger mockLogger = mock(Logger.class);
        Startup.logger = mockLogger;

        Startup.main(args);

        verify(mockLogger).error(eq("Invalid argument exception: {}"), anyString());
    }

    @Test
    void testMainWithExceptionCatchesUnexpectedException() {
        utilsMock.when(Utils::isDebug).thenReturn(false);
        utilsMock.when(Utils::currentDirectory).thenThrow(new RuntimeException("Unexpected error"));
        String[] args = {"Instance1", "SomeCommand"};
        Logger mockLogger = mock(Logger.class);
        Startup.logger = mockLogger;

        Startup.main(args);

        verify(mockLogger).error(eq("Unexpected error occurred: {}"), anyString(), any(Throwable.class));
    }

    @Test
    void testConfigureArgsForDebug_DebugMode() {
        utilsMock.when(Utils::isDebug).thenReturn(true);

        String[] args = {"Instance1", "SomeCommand"};
        String[] debugArgs = Startup.configureArgsForDebug(args);

        assertEquals("Debug", debugArgs[0]);
        assertEquals("./../contaudit-samples/executor/executor ./../contaudit-samples/executor/artifacts/apache2.workflow", debugArgs[1]);
    }

    @Test
    void testConfigureArgsForDebug_NotDebugMode() {
        utilsMock.when(Utils::isDebug).thenReturn(false);

        String[] args = {"Instance1", "SomeCommand"};
        String[] debugArgs = Startup.configureArgsForDebug(args);

        assertEquals(args, debugArgs);
    }

    @Test
    void testValidateArgs_NullArgs() {
        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            Startup.validateArgs(null);
        });

        assertEquals("Incorrect number of arguments. Expecting an instance identifier and a command line text.", exception.getMessage());
    }

    @Test
    void testValidateArgs_InsufficientArgs() {
        String[] args = {"Instance1"};

        InvalidArgumentException exception = assertThrows(InvalidArgumentException.class, () -> {
            Startup.validateArgs(args);
        });

        assertEquals("Incorrect number of arguments. Expecting an instance identifier and a command line text.", exception.getMessage());
    }

    @Test
    void testValidateArgs_ValidArgs() {
        String[] args = {"Instance1", "SomeCommand"};

        assertDoesNotThrow(() -> {
            Startup.validateArgs(args);
        });
    }
}