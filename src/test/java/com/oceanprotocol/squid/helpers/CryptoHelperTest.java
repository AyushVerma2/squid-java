/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import org.junit.Test;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;

import java.math.BigInteger;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class CryptoHelperTest {

    @Test
    public void soliditySha3() throws Exception {
        assertTrue(CryptoHelper.soliditySha3(1).length == 32);
        assertTrue(CryptoHelper.soliditySha3(1, 2, 3).length == 32);
    }

    @Test
    public void getHex() {
        String input= "hey";
        String hex= CryptoHelper.getHex(input.getBytes());
        assertTrue(hex.matches("-?[0-9a-fA-F]+"));
    }

    @Test
    public void toBytes() {

        assertEquals(32, CryptoHelper.toBytes("hey".getBytes()).length);
        assertEquals(32, CryptoHelper.toBytes("00a329c0648769A73afAc7F9381E08FB".getBytes()).length);
        assertEquals(32, CryptoHelper.toBytes(BigInteger.TEN).length);
        assertEquals(32, CryptoHelper.toBytes(BigInteger.valueOf(-1)).length);

        assertEquals(20, CryptoHelper.toBytes(
                new Address("00a329c0648769A73afAc7F9381E08FB43dBEA72")).length);

        assertEquals(32, CryptoHelper.toBytes(
                new Uint256(BigInteger.TEN)).length);

        assertEquals(8, CryptoHelper.toBytes(
                new Uint64(BigInteger.ZERO)).length);

        assertEquals(32, CryptoHelper.toBytes(
                7).length);
    }

    @Test(expected = IllegalArgumentException.class)
    public void toBytesException() {
        assertEquals(32, CryptoHelper.toBytes(new ArrayList<String>()).length);
    }

    }