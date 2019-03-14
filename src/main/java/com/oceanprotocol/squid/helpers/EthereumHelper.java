/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import org.web3j.crypto.*;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.methods.response.EthSign;
import org.web3j.utils.Numeric;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

public abstract class EthereumHelper {

    static final String MESSAGE_PREFIX = "\u0019Ethereum Signed Message:\n";


    /**
     * Given a String message, return the prefixed message hashed
     * @param messageString message to hash
     * @return byte[]
     */
    public static byte[] getEthereumMessageHash(String messageString) {
        String prefix = MESSAGE_PREFIX + messageString.length();
        return Hash.sha3((prefix + messageString).getBytes());
    }

    /**
     * Given a string and a ECKeyPair sign the ethereum prefixed message
     * @param message message to sign
     * @param keyPair keypair
     * @return signature data
     */
    public static Sign.SignatureData signMessage(String message, ECKeyPair keyPair)   {
        return Sign.signPrefixedMessage(message.getBytes(), keyPair);
    }

    /**
     * Given a string and a ECKeyPair sign the ethereum prefixed message
     * @param message message to sign
     * @param credentials user credentials
     * @return signature data
     */
    public static Sign.SignatureData signMessage(String message, Credentials credentials)   {
        return Sign.signPrefixedMessage(message.getBytes(), credentials.getEcKeyPair());
    }

    /**
     * Given a signature data and the hashed message, recover and return the public
     * key that generated the signature
     * @param signatureData signature data
     * @param hashMessage hashed message
     * @return address
     */
    public static List<String> recoverAddressFromSignature(Sign.SignatureData signatureData, byte[] hashMessage) {
        List<String> address= new ArrayList<>();
        ECDSASignature ecdsaSignature = new ECDSASignature(
                new BigInteger(1, signatureData.getR()),
                new BigInteger(1, signatureData.getS()));

        // Iterate for each possible key to recover
        for (int i = 0; i < 4; i++) {
            BigInteger publicKey = Sign.recoverFromSignature(
                    (byte) i, ecdsaSignature, hashMessage);
            if (publicKey != null) {
                address.add("0x" + Keys.getAddress(publicKey));
            }
        }
        return address;
    }

    /**
     * Given an address, signature data and the hashed message return true or false if the address
     * is the one used to sign the message
     * @param address address
     * @param signatureData the signature
     * @param hashMessage the hashed message
     * @return boolean
     */
    public static boolean wasSignedByAddress(String address, Sign.SignatureData signatureData, byte[] hashMessage)  {
        List<String> addresses= recoverAddressFromSignature(signatureData, hashMessage);
        return addresses.contains(address);
    }

    /**
     * Execute a web3 eth-sign message in the keeper
     * @param web3  web3j
     * @param message message to sign
     * @param address address to use for signing
     * @return signed message
     * @throws IOException IOException
     */
    public static String ethSignMessage(Web3j web3, String message, String address) throws IOException {
        EthSign ethSign= web3.ethSign(address, message).send();
        return ethSign.getResult();
    }

    /**
     * Hashing a message and signing using web3 eth-sign
     * @param web3 web3j
     * @param message message to hash and sign
     * @param address address to use for signing
     * @return signed message
     * @throws IOException IOException
     */
    public static String ethEncodeAndSignMessage(Web3j web3, String message, String address) throws IOException {
        String hash= Hash.sha3(EncodingHelper.encodeToHex(message));
        return ethSignMessage(web3, hash, address);
    }

    /**
     * Removes all the "0x"
     * @param input string
     * @return string
     */
    public static String remove0x(String input) {
        return input.replaceAll("0x", "");
    }

    /**
     * If a string doesn't start by 0x, prepend 0x to it
     * @param input string
     * @return string
     */
    public static String add0x(String input) {
        if (!input.startsWith("0x"))
            return "0x" + input;
        return input;
    }

    /**
     * Given an input string validates if it's a valid ethereum address
     * @param input the input string
     * @return boolean
     */
    public static boolean isValidAddress(String input) {
        String hash= remove0x(input).toLowerCase();
        if (hash.length() == 40 && EncodingHelper.isHexString(hash))
            return true;
        return false;
    }


    /**
     * Given a function defition return the ethereum function selector
     * @param functionDefinition the definition of a function
     * @return a String with the ethereum function selector
     */
    public static String getFunctionSelector(String functionDefinition)    {
        return Hash.sha3String(functionDefinition)
                .substring(0, 10);
    }

    public static String encodeParameterValue(String type, Object value) throws UnsupportedEncodingException {

        if (type.equals("string"))
           return EthereumHelper.remove0x((String) value);
        else if (type.contains("bytes32"))
            return EthereumHelper.remove0x((String) value);
        else if (type.contains("int"))
           if (value instanceof String)
              return EthereumHelper.remove0x(EncodingHelper.hexEncodeAbiType("uint", Integer.parseInt((String) value)));
            else
                return EthereumHelper.remove0x(EncodingHelper.hexEncodeAbiType("uint", value));
        else if (type.equals("address"))
            return EthereumHelper.remove0x((String)value);

        return "";

    }


}
