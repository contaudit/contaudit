package br.ufrgs.inf.ppgc.contaudit.wrapper.blockchain;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeoutException;

import org.hyperledger.fabric.gateway.Contract;
import org.hyperledger.fabric.gateway.ContractException;
import org.hyperledger.fabric.gateway.Gateway;
import org.hyperledger.fabric.gateway.Network;
import org.hyperledger.fabric.gateway.Transaction;
import org.hyperledger.fabric.gateway.Wallet;
import org.hyperledger.fabric.gateway.Wallets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockchainService {
    private Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    private boolean isDebug = java.lang.management.ManagementFactory.getRuntimeMXBean().getInputArguments().toString().indexOf("jdwp") >= 0;
    private String walletDir;
    private String networkConfigFileDir;
    private String identityName;
    private Gateway.Builder builder;

    public void submitTransaction(String channelName, String chaincodeName, String transactionName, String[] args) {
        try {
            this.initConfigProperties();
            this.configGateway();
            this.sendTransaction("submit", channelName, chaincodeName, transactionName, args);
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        }
    }

    public String evaluateTransaction(String channelName, String chaincodeName, String transactionName, String[] args) {
        try {
            this.initConfigProperties();
            this.configGateway();
            return this.sendTransaction("evaluate", channelName, chaincodeName, transactionName, args);
        } catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private String sendTransaction(String action, String channelName, String chaincodeName, String transactionName, String[] args) {
        // Create a gateway connection
        try (Gateway gateway = builder.connect()) {
            // Obtain the network through channel
            Network network = gateway.getNetwork(channelName);
            // Obtain the Chaincode Contract
            Contract contract = network.getContract(chaincodeName);
            // Creates the transaction
            Transaction transaction = contract.createTransaction(transactionName);

            // Sends the transaction
            byte[] response = null;
            if (action.equals("submit"))
                response = transaction.submit(args);
            else
                response = transaction.evaluate(args);
            
            String result = new String(response, StandardCharsets.UTF_8);
            String logStr = String.format("Response %s", result);
            logger.info(logStr);
            return result;
        } catch (ContractException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private Gateway.Builder configGateway() {
        try {
            // Load an existing wallet holding identities used to access the network.
            Path walletPath = Paths.get(walletDir);
            Wallet wallet = Wallets.newFileSystemWallet(walletPath);

            // Path to a common connection profile describing the network.
            Path networkConfigFile = Paths.get(networkConfigFileDir);

            // Configure the gateway connection used to access the network.
            builder = Gateway.createBuilder();
            builder.identity(wallet, identityName);
            builder.networkConfig(networkConfigFile);
            builder.discovery(false);

            return builder;
        }
        catch (Exception e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    private void initConfigProperties() throws IOException, URISyntaxException {
        String currentDirectory = new File(BlockchainService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        String systemFileSeparator = System.getProperty("file.separator");
        String configFilePath = currentDirectory + systemFileSeparator;
        if (this.isDebug)
            configFilePath = configFilePath + ".." + systemFileSeparator + ".." + systemFileSeparator + ".." + systemFileSeparator + "config.properties";
        else
            configFilePath = configFilePath + "config.properties";

        try (InputStream input = new FileInputStream(configFilePath)) {
            Properties prop = new Properties();
            prop.load(input);
            this.walletDir = prop.getProperty("WALLET_DIRECTORY");
            this.networkConfigFileDir = prop.getProperty("NETWORK_CONFIG_FILE_DIRECTORY");
            this.identityName = prop.getProperty("IDENTITY_NAME");
        }
    }
}