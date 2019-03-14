/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import com.oceanprotocol.squid.exceptions.EncodingException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Test;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.util.Arrays;

import static org.junit.Assert.*;

public class EthereumHelperTest {

    static final Logger log= LogManager.getLogger(EthereumHelper.class);

    public static final String PRIVATE_KEY_STRING = "a392604efc2fad9c0b3da43b5f698a2e3f270f170d859912be0d54742275c5f6";
    public static final String PUBLIC_KEY_STRING = "0x506bc1dc099358e5137292f4efdd57e400f29ba5132aa5d12b18dac1c1f6aab"
            + "a645c0b7b58158babbfa6c6cd5a48aa7340a8749176b120e8516216787a13dc76";
    public static final String ADDRESS = "0xef678007d18427e6022059dbc264f27507cd1ffc";

    public static final BigInteger PRIVATE_KEY = Numeric.toBigInt(PRIVATE_KEY_STRING);
    public static final BigInteger PUBLIC_KEY = Numeric.toBigInt(PUBLIC_KEY_STRING);

    static final ECKeyPair KEY_PAIR = new ECKeyPair(PRIVATE_KEY, PUBLIC_KEY);

    @Test
    public void signMessage() {
        String message = "Hi there";
        Sign.SignatureData signatureData = EthereumHelper.signMessage(message, KEY_PAIR);

        assertTrue(signatureData.getR().length == 32);
        assertTrue(signatureData.getS().length == 32);

    }

    @Test
    public void signAndValidateMessage() {
        String message = "Hi dude";
        byte[] hashMessage= EthereumHelper.getEthereumMessageHash(message);

        log.debug("String message to sign and validate: " + message);
        Sign.SignatureData signatureData = EthereumHelper.signMessage(message, KEY_PAIR);

        assertTrue(EthereumHelper.wasSignedByAddress(ADDRESS, signatureData, hashMessage));

    }

    @Test
    public void checkingSignatureString() throws EncodingException {
        String message = CryptoHelper.sha3("Hi there");

        Sign.SignatureData signatureSource = EthereumHelper.signMessage(message, KEY_PAIR);

        String signatureString= EncodingHelper.signatureToString(signatureSource);

        log.debug("Signature String " + signatureString);

        Sign.SignatureData signature= EncodingHelper.stringToSignature(signatureString);

        assertTrue(signatureSource.getR().length == 32);
        assertTrue(signatureSource.getS().length == 32);

        assertTrue(signature.getR().length == 32);
        assertTrue(signature.getS().length == 32);


        assertTrue(Arrays.equals(signatureSource.getR(), signature.getR()));
        assertTrue(Arrays.equals(signatureSource.getS(), signature.getS()));
        assertEquals(signatureSource.getV(), signature.getV());

    }

    @Test
    public void remove0x() {
        assertEquals("1234", EthereumHelper.remove0x("0x1234"));
    }

    @Test
    public void add0x() {
        assertEquals("0x1234", EthereumHelper.add0x("1234"));
        assertEquals("0x1234", EthereumHelper.add0x("0x1234"));
    }

    @Test
    public void getFunctionSelector() {
        String functionDefinition= "lockPayment(bytes32,bytes32,uint256)";
        assertEquals("0x668453f0", EthereumHelper.getFunctionSelector(functionDefinition));
    }

    @Test
    public void isValidAddress() {
        assertTrue(EthereumHelper.isValidAddress("0xEEC7C1607081af8F84224D2971c031c1940ca1B4"));
        assertTrue(EthereumHelper.isValidAddress("EEC7C1607081af8F84224D2971c031c1940ca1B4"));
        assertTrue(EthereumHelper.isValidAddress("0xeec4832948320849230480239584035890349583"));

        assertFalse(EthereumHelper.isValidAddress("0xeec48"));
        assertFalse(EthereumHelper.isValidAddress("0xmvdlkmsldmxclmvlxmvlsdkmvkdsm cvksdmfksd"));
        assertFalse(EthereumHelper.isValidAddress("0XEEC7C1607081af8F84224D2971c031c1940ca1B4"));
    }
}