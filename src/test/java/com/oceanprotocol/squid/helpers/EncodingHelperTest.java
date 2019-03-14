/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import org.apache.logging.log4j.core.net.MimeMessageBuilder;
import org.junit.Test;
import org.web3j.abi.datatypes.generated.Bytes32;

import java.io.UnsupportedEncodingException;

import static org.junit.Assert.*;

public class EncodingHelperTest {

    public static final String input= "hi there";


    @Test
    public void stringToBytes32() {
        Bytes32 bytes32= EncodingHelper.stringToBytes32(input);
        assertTrue(new String(bytes32.getValue()).contains(input));
    }

    @Test
    public void byteArrayToByteArray32() {
        byte[] bytes= EncodingHelper.byteArrayToByteArray32(input.getBytes());
        assertEquals(32, bytes.length);
    }

    @Test
    public void encodeToHex() throws UnsupportedEncodingException {
        String hex= EncodingHelper.encodeToHex(input);
        assertTrue(EncodingHelper.isHexString(hex));
    }

    @Test
    public void hexStringToBytes() throws UnsupportedEncodingException {
        String hexInput= "9348243298abcd";
        byte[] bytes= EncodingHelper.hexStringToBytes(hexInput);
        assertEquals(7, bytes.length);
    }

    @Test
    public void stringToBytes()  throws UnsupportedEncodingException  {
        String nohexInput= "fsasadascdv";
        byte[] bytes= EncodingHelper.stringToBytes(nohexInput);
        String output= new String(bytes);
        assertTrue(output.equals(nohexInput));
    }

    @Test
    public void padRightWithZero() {
        String output= EncodingHelper.padRightWithZero(input, 12);
        assertEquals(input+ "0000", output);
    }

    @Test
    public void toHexString() {
        String hex= EncodingHelper.toHexString(input.getBytes()).replaceFirst("0x", "");
        assertTrue(EncodingHelper.isHexString(hex));
    }

    @Test
    public void hexEncodeAbiType() throws UnsupportedEncodingException {
        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("bool", true)));

        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("uint", 3)));

        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("address", "0xB36A521Fb8DaBC1e515dC5f3b8EF928995495c0c")));

        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("bytes", input)));

        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("string", input)));

        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("xxxx", input)));

        String[] multiple= {"hi", "there"};
        assertTrue(EncodingHelper.isHexString(
                EncodingHelper.hexEncodeAbiType("string[]", multiple)));
    }
}