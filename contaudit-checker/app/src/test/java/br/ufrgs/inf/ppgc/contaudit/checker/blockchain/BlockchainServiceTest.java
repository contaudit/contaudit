package br.ufrgs.inf.ppgc.contaudit.checker.blockchain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.Properties;
import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Transaction;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.slf4j.Logger;

import java.io.InputStream;

class BlockchainServiceTest {
    @Mock
    Logger logger;

    @Mock
    Gateway.Builder builder;

    @Mock
    Gateway gateway;

    @Mock
    Network network;

    @Mock
    Contract contract;

    @Mock
    Transaction transaction;

    @InjectMocks
    BlockchainService blockchainService;

    @TempDir
    Path tempWalletDir;

    @TempDir
    Path tempNetworkConfigFileDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        doNothing().when(logger).info(anyString());

        blockchainService.walletDir = tempWalletDir.toString();
        blockchainService.networkConfigFileDir = tempNetworkConfigFileDir.toString();
    }

    @Test
    void testInitConfigProperties() throws IOException {
        Properties mockProperties = mock(Properties.class);
        when(mockProperties.getProperty("WALLET_DIRECTORY")).thenReturn(tempWalletDir.toString());
        when(mockProperties.getProperty("NETWORK_CONFIG_FILE_DIRECTORY")).thenReturn(tempNetworkConfigFileDir.toString());
        when(mockProperties.getProperty("IDENTITY_NAME")).thenReturn("identityName");

        doNothing().when(mockProperties).load(any(InputStream.class));

        blockchainService.walletDir = tempWalletDir.toString();
        blockchainService.networkConfigFileDir = tempNetworkConfigFileDir.toString();
        blockchainService.identityName = "identityName";

        assertEquals(tempWalletDir.toString(), blockchainService.walletDir);
        assertEquals(tempNetworkConfigFileDir.toString(), blockchainService.networkConfigFileDir);
        assertEquals("identityName", blockchainService.identityName);
    }

    @Test
    void testSubmitTransaction() {
        String[] args = {"arg1", "arg2"};
        BlockchainService spyBlockchainService = spy(blockchainService);
        doReturn("submitResponse").when(spyBlockchainService).sendTransaction(anyString(), anyString(), anyString(), anyString(), any(String[].class));
    
        spyBlockchainService.submitTransaction("channelName", "chaincodeName", "transactionName", args);
        
        verify(spyBlockchainService).sendTransaction("submit", "channelName", "chaincodeName", "transactionName", args);
    }

    @Test
    void testEvaluateTransaction() {
        String[] args = {"arg1", "arg2"};
        BlockchainService spyBlockchainService = spy(blockchainService);
        doReturn("evaluateResponse").when(spyBlockchainService).sendTransaction(anyString(), anyString(), anyString(), anyString(), any(String[].class));
    
        String response = spyBlockchainService.evaluateTransaction("channelName", "chaincodeName", "transactionName", args);
    
        assertEquals("evaluateResponse", response);
        verify(spyBlockchainService).sendTransaction("evaluate", "channelName", "chaincodeName", "transactionName", args);
    }

    @Test
    void testSendTransactionSubmit() throws Exception {
        String[] args = {"arg1", "arg2"};
        byte[] mockResponse = "mockSubmitResponse".getBytes(StandardCharsets.UTF_8);
        when(builder.connect()).thenReturn(gateway);
        when(gateway.getNetwork(anyString())).thenReturn(network);
        when(network.getContract(anyString())).thenReturn(contract);
        when(contract.createTransaction(anyString())).thenReturn(transaction);
        when(transaction.submit(args)).thenReturn(mockResponse);

        String result = blockchainService.sendTransaction("submit", "channelName", "chaincodeName", "transactionName", args);

        assertEquals("mockSubmitResponse", result);
        verify(logger).info("Response mockSubmitResponse");
    }

    @Test
    void testSendTransactionEvaluate() throws Exception {
        String[] args = {"arg1", "arg2"};
        byte[] mockResponse = "mockEvaluateResponse".getBytes(StandardCharsets.UTF_8);
        when(builder.connect()).thenReturn(gateway);
        when(gateway.getNetwork(anyString())).thenReturn(network);
        when(network.getContract(anyString())).thenReturn(contract);
        when(contract.createTransaction(anyString())).thenReturn(transaction);
        when(transaction.evaluate(args)).thenReturn(mockResponse);

        String result = blockchainService.sendTransaction("evaluate", "channelName", "chaincodeName", "transactionName", args);

        assertEquals("mockEvaluateResponse", result);
        verify(logger).info("Response mockEvaluateResponse");
    }

    @Test
    void testSendTransactionExceptionHandling() throws Exception {
        String[] args = {"arg1", "arg2"};
        when(builder.connect()).thenReturn(gateway);
        when(gateway.getNetwork(anyString())).thenReturn(network);
        when(network.getContract(anyString())).thenReturn(contract);
        when(contract.createTransaction(anyString())).thenReturn(transaction);
        when(transaction.submit(args)).thenThrow(new ContractException("ContractException"));

        String result = blockchainService.sendTransaction("submit", "channelName", "chaincodeName", "transactionName", args);

        assertNull(result);
    }

    @Test
    void testConfigGatewayExceptionHandling() {
        blockchainService.walletDir = tempWalletDir.resolve("invalidPath").toString();
        blockchainService.networkConfigFileDir = tempNetworkConfigFileDir.resolve("invalidPath").toString();
        blockchainService.identityName = "identityName";

        Gateway.Builder resultBuilder = blockchainService.configGateway();

        assertNull(resultBuilder);
    }
}
