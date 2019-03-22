/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.external.web3;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.admin.Admin;
import org.web3j.protocol.core.DefaultBlockParameterName;
import org.web3j.protocol.core.methods.request.Transaction;
import org.web3j.protocol.core.methods.response.EthGetTransactionCount;
import org.web3j.protocol.core.methods.response.EthSendTransaction;
import org.web3j.tx.TransactionManager;

import java.io.IOException;
import java.math.BigInteger;

public class PersonalTransactionManager extends TransactionManager {

    private static final Logger log = LogManager.getLogger(PersonalTransactionManager.class);

    private final Admin web3j;
    private final Credentials credentials;
    private final String password;

    public PersonalTransactionManager(Admin web3j, Credentials credentials, String password)    {
        super(web3j, credentials.getAddress());
        this.web3j= web3j;
        this.credentials= credentials;
        this.password= password;
    }



    protected BigInteger getNonce() throws IOException {
        EthGetTransactionCount ethGetTransactionCount = web3j.ethGetTransactionCount(
                credentials.getAddress(), DefaultBlockParameterName.PENDING).send();

        return ethGetTransactionCount.getTransactionCount();
    }

    @Override
    public EthSendTransaction sendTransaction(BigInteger gasPrice, BigInteger gasLimit, String to, String data, BigInteger value) throws IOException {

        Transaction transaction = new Transaction(
                getFromAddress(), getNonce(), gasPrice, gasLimit, to, value, data);

        log.debug("Sending Personal Transaction " + transaction.getNonce());
        EthSendTransaction ethSendTransaction = web3j.personalSendTransaction(transaction, password).send();

        return ethSendTransaction;
    }
}
