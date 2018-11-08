package com.oceanprotocol.squid.dto;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.RawTransactionManager;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;

public class KeeperDto {

    protected static final Logger log = LogManager.getLogger(KeeperDto.class);

    private Web3j web3 = null;
    private String keeperUrl;
    private String address;
    private String password;
    private Credentials credentials= null;
    private String credentialsFile;

    private TransactionManager txManager;
    private ContractGasProvider gasProvider;

    private BigInteger gasPrice;
    private BigInteger gasLimit;

    private final BigInteger DEFAULT_GAS_PRICE= BigInteger.valueOf(1500l);
    private final BigInteger DEFAULT_GAS_LIMIT= BigInteger.valueOf(250000l);


    /**
     * Initializes the KeeperDto object given a Keeper url, user and password
     * @param url Parity Keeper url (ie. http://localhost:8545)
     * @param address User ethereum address
     * @param password User password
     * @param credentialsFile Path to the file with the local credentials
     * @return KeeperDto
     */
    public static KeeperDto getInstance(String url, String address, String password, String credentialsFile)
            throws IOException, CipherException {

        return new KeeperDto(url, address, password, credentialsFile);
    }

    public static KeeperDto getInstance(Web3jService web3jService) {
        return new KeeperDto(web3jService);
    }

    private KeeperDto(Web3jService web3jService)  {
        this.web3= Web3j.build(web3jService);
    }

    private KeeperDto(String url, String address, String password, String credentialsFile) throws IOException, CipherException {

        log.debug("Initializing KeeperDto: " + url);
        this.address= address;
        this.password= password;
        this.keeperUrl= url;
        this.credentialsFile= credentialsFile;
        this.gasPrice= DEFAULT_GAS_PRICE;
        this.gasLimit= DEFAULT_GAS_LIMIT;

        this.web3 = Web3j.build(new HttpService(this.keeperUrl));

        this.txManager= new RawTransactionManager(this.web3, getCredentials());
        this.gasProvider= new StaticGasProvider(this.gasPrice, this.gasLimit);

    }

    /**
     * Get the Web3j instance
     * @return web3j
     */
    public Web3j getWeb3()  {
        return web3;
    }

    public KeeperDto setCredentials(Credentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public Credentials getCredentials() throws IOException, CipherException {
        if (null == credentials)
            credentials= WalletUtils.loadCredentials(password, credentialsFile);
        return credentials;
    }

    public static ContractGasProvider getContractGasProviderInstance(BigInteger gasPrice, BigInteger gasLimit)  {
        return new StaticGasProvider(gasPrice, gasLimit);
    }

    public TransactionManager getTxManager() {
        return txManager;
    }

    public ContractGasProvider getContractGasProvider() {
        return gasProvider;
    }

    public BigInteger getGasPrice() {
        return gasPrice;
    }

    public BigInteger getGasLimit() {
        return gasLimit;
    }

    public KeeperDto setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
        this.gasProvider= getContractGasProviderInstance(gasPrice, gasLimit);
        return this;
    }

    public KeeperDto setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
        this.gasProvider= getContractGasProviderInstance(gasPrice, gasLimit);
        return this;
    }

    public String getAddress() {
        return address;
    }
}
