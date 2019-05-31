package com.oceanprotocol.squid.external.parity;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthGetTransactionReceipt;
import org.web3j.protocol.core.methods.response.Log;
import org.web3j.protocol.core.methods.response.TransactionReceipt;
import org.web3j.protocol.exceptions.TransactionException;
import org.web3j.tx.response.TransactionReceiptProcessor;

import java.io.IOException;
import java.util.Optional;

public class SquidTransactionReceiptProcessor extends TransactionReceiptProcessor {

    private static final Logger log = LogManager.getLogger(SquidTransactionReceiptProcessor.class);

    private final long sleepDuration;
    private final int attempts;
    private final Web3j web3j;


    public SquidTransactionReceiptProcessor(Web3j web3j, long sleepDuration, int attempts) {
        super(web3j);
        this.sleepDuration = sleepDuration;
        this.attempts = attempts;
        this.web3j = web3j;
    }


    @Override
    public TransactionReceipt waitForTransactionReceipt(
            String transactionHash)
            throws IOException, TransactionException {

        return getTransactionReceipt(transactionHash, sleepDuration, attempts);
    }

    Optional<TransactionReceipt> sendTransactionReceiptRequest(
            String transactionHash) throws IOException, TransactionException {
        EthGetTransactionReceipt transactionReceipt =
                web3j.ethGetTransactionReceipt(transactionHash).send();
        if (transactionReceipt.hasError()) {
            throw new TransactionException("Error processing request: "
                    + transactionReceipt.getError().getMessage());
        }

        return transactionReceipt.getTransactionReceipt();
    }


    private Boolean keepWaiting(Optional<TransactionReceipt> receiptOptional) {

        if (!receiptOptional.isPresent())
            return true;

        TransactionReceipt receipt = receiptOptional.get();
        Optional<Log> optionalLog = receipt.getLogs().stream().filter(log -> !log.getType().equalsIgnoreCase("mined")).findFirst();
        if (optionalLog.isPresent()) {
            log.debug("Not mined transaction receipt. Waiting until transaction get mined...");
            return true;
        }

        return false;
    }

    private TransactionReceipt getTransactionReceipt(
            String transactionHash, long sleepDuration, int attempts)
            throws IOException, TransactionException {

        Optional<TransactionReceipt> receiptOptional = sendTransactionReceiptRequest(transactionHash);

        for (int i = 0; i < attempts; i++) {

            if (keepWaiting(receiptOptional)) {
                try {
                    Thread.sleep(sleepDuration);
                } catch (InterruptedException e) {
                    throw new TransactionException(e);
                }
                receiptOptional = sendTransactionReceiptRequest(transactionHash);
            } else {
                return receiptOptional.get();
            }
        }

        throw new TransactionException("Transaction receipt was not generated after "
                + ((sleepDuration * attempts) / 1000
                + " seconds for transaction: " + transactionHash), transactionHash);
    }


}
