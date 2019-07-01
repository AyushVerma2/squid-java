/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external.web3;

import com.oceanprotocol.squid.external.parity.SquidTransactionReceiptProcessor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.RawTransaction;
import org.web3j.crypto.TransactionEncoder;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;
import org.web3j.tx.exceptions.TxHashMismatchException;
import org.web3j.utils.Numeric;
import org.web3j.utils.TxHashVerifier;

import java.io.IOException;
import java.math.BigInteger;

public class PersonalTransactionManager extends TransactionManager {

    private static final Logger log = LogManager.getLogger(PersonalTransactionManager.class);

    private final Admin web3j;
    private final Credentials credentials;
    private final String password;

    protected TxHashVerifier txHashVerifier = new TxHashVerifier();

    public PersonalTransactionManager(Admin web3j, Credentials credentials, String password, int attempts, long sleepDuration) {

        super(new SquidTransactionReceiptProcessor(web3j, sleepDuration, attempts), credentials.getAddress());
        this.web3j = web3j;
        this.credentials = credentials;
        this.password = password;
    }

    public PersonalTransactionManager(Admin web3j, Credentials credentials, String password) {
        this(web3j, credentials, password, 50, 5000l);
    }


    protected BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();

        return ethGetTransactionCount.getTransactionCount();
    }

    protected BigInteger getEstimatedGas(String to, String data) throws IOException {
        // Transaction tx= Transaction.createEthCallTransaction( getFromAddress(), to, data);
        // EthEstimateGas estimateGas= web3j.ethEstimateGas(tx).send();
        //return estimateGas.getAmountUsed();

        BigInteger gas = web3j.ethGetBlockByNumber(DefaultBlockParameterName.LATEST, false).send().getBlock().getGasLimit();
        return gas;
    }

    @Override
    public EthSendTransaction sendTransaction(
            BigInteger gasPrice, BigInteger gasLimit, String to,
            String data, BigInteger value) throws IOException {

        BigInteger nonce = getNonce();

        RawTransaction rawTransaction = RawTransaction.createTransaction(
                nonce,
                getEstimatedGas(to, data),
                gasLimit,
                to,
                value,
                data);

        return signAndSend(rawTransaction);
    }

    /*
     * @param rawTransaction a RawTransaction istance to be signed
     * @return The transaction signed and encoded without ever broadcasting it
     */
    public String sign(RawTransaction rawTransaction) {

        byte[] signedMessage;
        signedMessage = TransactionEncoder.signMessage(rawTransaction, credentials);

        return Numeric.toHexString(signedMessage);
    }

    public EthSendTransaction signAndSend(RawTransaction rawTransaction)
            throws IOException {

        String hexValue = sign(rawTransaction);
        EthSendTransaction ethSendTransaction = web3j.ethSendRawTransaction(hexValue).send();

        if (ethSendTransaction != null && !ethSendTransaction.hasError()) {
            String txHashLocal = Hash.sha3(hexValue);
            String txHashRemote = ethSendTransaction.getTransactionHash();
            if (!txHashVerifier.verify(txHashLocal, txHashRemote)) {
                throw new TxHashMismatchException(txHashLocal, txHashRemote);
            }
        }

        return ethSendTransaction;
    }
}


