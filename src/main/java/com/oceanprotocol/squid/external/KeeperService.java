/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external;

import com.oceanprotocol.keeper.contracts.OceanToken;
import com.oceanprotocol.squid.exceptions.TokenApproveException;
import com.oceanprotocol.squid.external.parity.JsonRpcSquidAdmin;
import com.oceanprotocol.squid.external.web3.PersonalTransactionManager;
import com.oceanprotocol.squid.models.Account;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.admin.methods.response.PersonalUnlockAccount;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.gas.ContractGasProvider;
import org.web3j.tx.gas.StaticGasProvider;

import java.io.IOException;
import java.math.BigInteger;

public class KeeperService {

    protected static final Logger log = LogManager.getLogger(KeeperService.class);

    private Admin web3 = null;
    private String address;
    private String password;
    private Credentials credentials = null;
    private String credentialsFile;

    private TransactionManager txManager;
    private ContractGasProvider gasProvider;

    private BigInteger gasPrice;
    private BigInteger gasLimit;

    private static final BigInteger DEFAULT_GAS_PRICE = BigInteger.valueOf(1500l);
    private static final BigInteger DEFAULT_GAS_LIMIT = BigInteger.valueOf(250000l);



    /**
     * Initializes the KeeperService object given a Keeper url, user and password
     *
     * @param url             Parity Keeper url (ie. http://localhost:8545)
     * @param address         User ethereum address
     * @param password        User password
     * @param credentialsFile Path to the file with the local credentials
     * @param txAttempts      attempts to get the transaction receipt
     * @param txSleepDuration time in milliseconds between each attempt
     * @return KeeperService
     * @throws IOException     IOException
     * @throws CipherException CipherException
     */
    public static KeeperService getInstance(String url, String address, String password, String credentialsFile, int txAttempts, long txSleepDuration)
            throws IOException, CipherException {

        return new KeeperService(url, address, password, credentialsFile, txAttempts, txSleepDuration);
    }

    public static KeeperService getInstance(Web3jService web3jService) {
        return new KeeperService(web3jService);
    }

    private KeeperService(Web3jService web3jService) {
        this.web3 = Admin.build(web3jService);
    }

    private KeeperService(String url, String address, String password, String credentialsFile, int txAttempts, long txSleepDuration) throws IOException, CipherException {

        log.debug("Initializing KeeperService: " + url);
        this.address = address;
        this.password = password;
        this.credentialsFile = credentialsFile;
        this.gasPrice = DEFAULT_GAS_PRICE;
        this.gasLimit = DEFAULT_GAS_LIMIT;
        String keeperUrl = url;

        this.web3 = new JsonRpcSquidAdmin(new HttpService(keeperUrl));

        // TODO: Web3j only supports a ChainId in byte formUrl, so any ChainId of a
        // private network is not supported. By the time being we can't specify that
        // parameter in the TransactionManager
        // https://github.com/web3j/web3j/issues/234
        //this.chainId= this.web3.netVersion().send().getNetVersion();


        //this.txManager= new RawTransactionManager(this.web3, getCredentials());
        this.txManager = new PersonalTransactionManager(this.web3, getCredentials(), password, txAttempts, txSleepDuration);
        this.gasProvider = new StaticGasProvider(this.gasPrice, this.gasLimit);

    }

    /**
     * Get the Web3j instance
     *
     * @return web3j
     */
    public Admin getWeb3() {
        return web3;
    }

    public KeeperService setCredentials(Credentials credentials) {
        this.credentials = credentials;
        return this;
    }

    public Credentials getCredentials() throws IOException, CipherException {
        if (null == credentials)
            credentials = WalletUtils.loadCredentials(password, credentialsFile);
        return credentials;
    }

    public static ContractGasProvider getContractGasProviderInstance(BigInteger gasPrice, BigInteger gasLimit) {
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

    public KeeperService setGasPrice(BigInteger gasPrice) {
        this.gasPrice = gasPrice;
        this.gasProvider = getContractGasProviderInstance(gasPrice, gasLimit);
        return this;
    }

    public KeeperService setGasLimit(BigInteger gasLimit) {
        this.gasLimit = gasLimit;
        this.gasProvider = getContractGasProviderInstance(gasPrice, gasLimit);
        return this;
    }

    public String getAddress() {
        return address;
    }

    public boolean unlockAccount(Account account) throws Exception {

        PersonalUnlockAccount personalUnlockAccount =
                this
                        .getWeb3()
                        // From JsonRpc2_0Admin:
                        // Parity has a bug where it won't support a duration
                        // See https://github.com/ethcore/parity/issues/1215

                        //From https://wiki.parity.io/JSONRPC-personal-module#personal_unlockaccount
                        /*
                        If permanent unlocking is disabled (the default) then the duration argument will be ignored,
                        and the account will be unlocked for a single signing. With permanent locking enabled, the duration sets the number
                        of seconds to hold the account open for. It will default to 300 seconds. Passing 0 unlocks the account indefinitely.

                        There can only be one unlocked account at a time. (?????)

                        https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_sign

                        The sign method calculates an Ethereum specific signature with:
                        sign(keccak256("\x19Ethereum Signed Message:\n" + len(message) + message))).

                         By adding a prefix to the message makes the calculated signature recognisable as an Ethereum specific signature.
                         This prevents misuse where a malicious DApp can sign arbitrary data (e.g. transaction) and use the signature to impersonate the victim.

                         Note the address to sign with must be unlocked.

                         */
                        .personalUnlockAccount(account.getAddress(), account.getPassword(), null)
                        .sendAsync().get();
        //this.getWeb3().personalSendTransaction().send()
        return personalUnlockAccount.accountUnlocked();
    }

    public boolean tokenApprove(OceanToken tokenContract, String spenderAddress, String price) throws TokenApproveException {

        String checksumAddress = Keys.toChecksumAddress(spenderAddress);

        try {

            TransactionReceipt receipt = tokenContract.approve(
                    checksumAddress,
                    new BigInteger(price)
            ).send();

            if (!receipt.getStatus().equals("0x1")) {
                String msg = "The Status received is not valid executing Token Approve: " + receipt.getStatus();
                log.error(msg);
                throw new TokenApproveException(msg);
            }

            log.debug("Token Approve transactionReceipt OK ");
            return true;

        } catch (Exception e) {

            String msg = "Error executing Token Approve ";
            log.error(msg + ": " + e.getMessage());
            throw new TokenApproveException(msg, e);
        }

    }
}
