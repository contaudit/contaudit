package br.ufrgs.inf.ppgc.contaudit.checker.blockchain;

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
    protected Logger logger = LoggerFactory.getLogger(BlockchainService.class);
    protected String walletDir;
    protected String networkConfigFileDir;
    protected String identityName;
    protected Gateway.Builder builder;

    public BlockchainService() throws IOException, URISyntaxException {
        this.initConfigProperties();
        this.configGateway();
    }

    public void submitTransaction(String channelName, String chaincodeName, String transactionName, String[] args) {
        this.sendTransaction("submit", channelName, chaincodeName, transactionName, args);
    }

    public String evaluateTransaction(String channelName, String chaincodeName, String transactionName, String[] args) {
        return this.sendTransaction("evaluate", channelName, chaincodeName, transactionName, args);
    }

    protected String sendTransaction(String action, String channelName, String chaincodeName, String transactionName, String[] args) {
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
            String logString = String.format("Response %s", result);
            logger.info(logString);
            return result;
        } catch (ContractException | TimeoutException | InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
            return null;
        }
    }

    protected Gateway.Builder configGateway() {
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

            return builder;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    protected void initConfigProperties() throws IOException, URISyntaxException {
        String currentDirectory = new File(BlockchainService.class.getProtectionDomain().getCodeSource().getLocation().toURI()).getParent();
        String systemFileSeparator = System.getProperty("file.separator");

        Properties prop = new Properties();
        try (InputStream input = new FileInputStream(currentDirectory + systemFileSeparator + "config.properties")) {
            prop.load(input);
        }

        this.walletDir = prop.getProperty("WALLET_DIRECTORY");
        this.networkConfigFileDir = prop.getProperty("NETWORK_CONFIG_FILE_DIRECTORY");
        this.identityName = prop.getProperty("IDENTITY_NAME");
    }
}