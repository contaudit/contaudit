package br.ufrgs.inf.ppgc.contaudit;

import org.hyperledger.fabric.shim.Chaincode.Response;
import org.hyperledger.fabric.shim.Chaincode.Response.Status;
import org.hyperledger.fabric.shim.ChaincodeStub;
import org.hyperledger.fabric.shim.ResponseUtils;
import org.hyperledger.fabric.shim.ledger.KeyValue;
import org.hyperledger.fabric.shim.ledger.QueryResultsIterator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LogChaincodeTest {
    @InjectMocks
    private LogChaincode logChaincode;

    @Mock
    private ChaincodeStub stub;

    @BeforeEach
    void setUp() {
        logChaincode = new LogChaincode();
    }

    @Test
    void testInit() {
        Response response = logChaincode.init(stub);
        assertEquals(Status.SUCCESS, response.getStatus());
        assertNull(response.getMessage());
    }

    @Test
    void testInvokeCatchException() {
        when(stub.getFunction()).thenThrow(new RuntimeException("Simulated Exception"));

        Response response = logChaincode.invoke(stub);
        Response expectedResponse = ResponseUtils.newErrorResponse("Unexpected error");

        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals(expectedResponse.getMessage(), response.getMessage());
        verify(stub, times(1)).getFunction();
    }

    @Test
    void testInvokeInsertSuccess() {
        when(stub.getFunction()).thenReturn("insert");
        List<String> params = new ArrayList<>();
        params.add("logKey1");
        params.add("{\"message\": \"Test log\"}");
        when(stub.getParameters()).thenReturn(params);

        Response response = logChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("Log saved successfully.", response.getMessage());
        verify(stub, times(1)).putStringState("logKey1", "{\"message\": \"Test log\"}");
    }

    @Test
    void testInvokeInsertWithInvalidParameters() {
        when(stub.getFunction()).thenReturn("insert");
        List<String> params = new ArrayList<>();
        params.add("logKey1");
        when(stub.getParameters()).thenReturn(params);

        Response response = logChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting a log key and a log object.", response.getMessage());
        verify(stub, never()).putStringState(anyString(), anyString());
    }

    @Test
    void testInsertCatchException() {
        when(stub.getFunction()).thenReturn("insert");
        List<String> params = new ArrayList<>();
        params.add("logKey1");
        params.add("{\"message\": \"Test log\"}");
        when(stub.getParameters()).thenReturn(params);
        doThrow(new RuntimeException("Simulated Exception during insert")).when(stub).putStringState(anyString(), anyString());
    
        Response response = logChaincode.invoke(stub);
        Response expectedResponse = ResponseUtils.newErrorResponse("Error during insert operation.");

        assertEquals(expectedResponse.getStatus(), response.getStatus());
        assertEquals("Error during insert operation.", response.getMessage());
        verify(stub, times(1)).putStringState("logKey1", "{\"message\": \"Test log\"}");
    }    

    @SuppressWarnings("unchecked")
    @Test
    void testInvokeQueryByRangeSuccess() throws Exception {
        when(stub.getFunction()).thenReturn("queryByRange");
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        params.add("logKeyEnd");
        when(stub.getParameters()).thenReturn(params);

        QueryResultsIterator<KeyValue> queryResultsIterator = mock(QueryResultsIterator.class);
        KeyValue keyValue1 = mock(KeyValue.class);
        when(keyValue1.getStringValue()).thenReturn("{\"message\": \"Log1\"}");
        KeyValue keyValue2 = mock(KeyValue.class);
        when(keyValue2.getStringValue()).thenReturn("{\"message\": \"Log2\"}");

        List<KeyValue> keyValueList = List.of(keyValue1, keyValue2);
        Iterator<KeyValue> iterator = keyValueList.iterator();
        when(queryResultsIterator.iterator()).thenReturn(iterator);
        when(stub.getStateByRange("logKeyStart", "logKeyEnd")).thenReturn(queryResultsIterator);

        Response response = logChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getMessage()).contains("Logs found."));
        verify(stub, times(1)).getStateByRange("logKeyStart", "logKeyEnd");
        verify(queryResultsIterator, times(1)).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testInvokeQueryByRangeWithNoResults() throws Exception {
        when(stub.getFunction()).thenReturn("queryByRange");
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        params.add("logKeyEnd");
        when(stub.getParameters()).thenReturn(params);

        QueryResultsIterator<KeyValue> queryResultsIterator = mock(QueryResultsIterator.class);
        Iterator<KeyValue> emptyIterator = mock(Iterator.class);
        when(emptyIterator.hasNext()).thenReturn(false);
        when(queryResultsIterator.iterator()).thenReturn(emptyIterator);
        when(stub.getStateByRange("logKeyStart", "logKeyEnd")).thenReturn(queryResultsIterator);

        Response response = logChaincode.invoke(stub);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("No logs found.", response.getMessage());
        verify(stub, times(1)).getStateByRange("logKeyStart", "logKeyEnd");
        verify(queryResultsIterator, times(1)).close();
    }

    @Test
    void testInvokeQueryByRangeWithInvalidParameters() {
        when(stub.getFunction()).thenReturn("queryByRange");
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        when(stub.getParameters()).thenReturn(params);

        Response response = logChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting a start log key and an end log key.", response.getMessage());
        verify(stub, never()).getStateByRange(anyString(), anyString());
    }

    @Test
    void testQueryByRangeCatchException() {
        when(stub.getFunction()).thenReturn("queryByRange");
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        params.add("logKeyEnd");
        when(stub.getParameters()).thenReturn(params);
        when(stub.getStateByRange(anyString(), anyString())).thenThrow(new RuntimeException("Simulated Exception during queryByRange"));
    
        Response response = logChaincode.invoke(stub);

        assertEquals(ResponseUtils.newErrorResponse("Error during queryByRange operation.").getStatus(), response.getStatus());
        assertEquals("Error during queryByRange operation.", response.getMessage());
        verify(stub, times(1)).getStateByRange("logKeyStart", "logKeyEnd");
    }    
    
    @Test
    void testInvokeWithInvalidFunction() {
        when(stub.getFunction()).thenReturn("invalidFunction");
        Response response = logChaincode.invoke(stub);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Invalid invoke function name. Expecting one of: [\"insert\", \"queryByRange\"]", response.getMessage());
    }

    @Test
    void testInsertSuccess() {
        List<String> params = new ArrayList<>();
        params.add("logKey1");
        params.add("{\"message\": \"Test log\"}");

        Response response = logChaincode.insert(stub, params);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("Log saved successfully.", response.getMessage());
        verify(stub, times(1)).putStringState("logKey1", "{\"message\": \"Test log\"}");
    }

    @Test
    void testInsertWithInvalidParameters() {
        List<String> params = new ArrayList<>();
        params.add("logKey1");

        Response response = logChaincode.insert(stub, params);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting a log key and a log object.", response.getMessage());
        verify(stub, never()).putStringState(anyString(), anyString());
    }

    @SuppressWarnings("unchecked")
    @Test
    void testQueryByRangeSuccess() throws Exception {
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        params.add("logKeyEnd");

        QueryResultsIterator<KeyValue> queryResultsIterator = mock(QueryResultsIterator.class);
        KeyValue keyValue1 = mock(KeyValue.class);
        when(keyValue1.getStringValue()).thenReturn("{\"message\": \"Log1\"}");
        KeyValue keyValue2 = mock(KeyValue.class);
        when(keyValue2.getStringValue()).thenReturn("{\"message\": \"Log2\"}");

        List<KeyValue> keyValueList = List.of(keyValue1, keyValue2);
        Iterator<KeyValue> iterator = keyValueList.iterator();
        when(queryResultsIterator.iterator()).thenReturn(iterator);
        when(stub.getStateByRange("logKeyStart", "logKeyEnd")).thenReturn(queryResultsIterator);

        Response response = logChaincode.queryByRange(stub, params);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertTrue(new String(response.getMessage()).contains("Logs found."));
        verify(stub, times(1)).getStateByRange("logKeyStart", "logKeyEnd");
        verify(queryResultsIterator, times(1)).close();
    }

    @SuppressWarnings("unchecked")
    @Test
    void testQueryByRangeWithNoResults() throws Exception {
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");
        params.add("logKeyEnd");

        QueryResultsIterator<KeyValue> queryResultsIterator = mock(QueryResultsIterator.class);
        Iterator<KeyValue> emptyIterator = mock(Iterator.class);
        when(emptyIterator.hasNext()).thenReturn(false);
        when(queryResultsIterator.iterator()).thenReturn(emptyIterator);
        when(stub.getStateByRange("logKeyStart", "logKeyEnd")).thenReturn(queryResultsIterator);

        Response response = logChaincode.queryByRange(stub, params);

        assertEquals(Status.SUCCESS, response.getStatus());
        assertEquals("No logs found.", response.getMessage());
        verify(stub, times(1)).getStateByRange("logKeyStart", "logKeyEnd");
        verify(queryResultsIterator, times(1)).close();
    }

    @Test
    void testQueryByRangeWithInvalidParameters() {
        List<String> params = new ArrayList<>();
        params.add("logKeyStart");

        Response response = logChaincode.queryByRange(stub, params);

        assertEquals(Status.INTERNAL_SERVER_ERROR, response.getStatus());
        assertEquals("Incorrect number of arguments. Expecting a start log key and an end log key.", response.getMessage());
        verify(stub, never()).getStateByRange(anyString(), anyString());
    }
}