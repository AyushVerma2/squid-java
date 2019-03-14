/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import com.oceanprotocol.squid.exceptions.EncodingException;
import org.apache.commons.codec.binary.Hex;
import org.web3j.abi.TypeEncoder;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Bool;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Bytes32;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.nio.ByteBuffer;

public abstract class EncodingHelper {

    /**
     * Given a String return a Bytes32
     * @param input input string
     * @return Bytes32 output
     */
    public static Bytes32 stringToBytes32(String input) {
        byte[] byteValue = input.getBytes();
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return new Bytes32(byteValueLen32);
    }

    /**
     * Given a byte array returns the first 32 bytes in a new byte array
     * @param byteValue byte[]
     * @return byte[]
     */
    public static byte[] byteArrayToByteArray32(byte[] byteValue) {
        byte[] byteValueLen32 = new byte[32];
        System.arraycopy(byteValue, 0, byteValueLen32, 0, byteValue.length);
        return byteValueLen32;
    }

    /**
     * Encodes a String in Hex
     * @param input string to encode
     * @return Hex string
     * @throws UnsupportedEncodingException Error encoding to Hex
     */
    public static String encodeToHex(String input) throws UnsupportedEncodingException {
        return Hex.encodeHexString(input.getBytes("UTF-8"));
    }

    /**
     * Encodes a Hex String in a byte array
     * @param input hex string to encode
     * @return byte[]
     * @throws UnsupportedEncodingException Error encoding to byte array
     */
    public static byte[] hexStringToBytes(String input) throws UnsupportedEncodingException {
        return Numeric.hexStringToByteArray(input);
    }

    /**
     * Convert a string to hex and after to a byte array
     * @param input string to encode
     * @return byte[]
     * @throws UnsupportedEncodingException Error encoding to byte array
     */
    public static byte[] stringToBytes(String input) throws UnsupportedEncodingException {
        return hexStringToBytes(encodeToHex(input));
    }

    /**
     * Pad a string with zero given a specific length
     * @param input string
     * @param len length of the output string
     * @return string
     */
    public static String padRightWithZero(String input, int len) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            sb.append('0');
        }
        return input + sb.substring(input.length());

    }

    /**
     * Convert a byte[] to a hex string with 0x appended
     * @param input byte[]
     * @return hex string
     */
    public static String toHexString(byte[] input)  {
        return Numeric.toHexString(input);
    }

    /**
     * Return true or false if a input string is in hex format
     * @param input the input string
     * @return bool
     */
    public static boolean isHexString(String input) {
        return input.matches("-?[0-9a-fA-F]+");
    }

    /**
     * Given a type and an object, convert to the proper web3j abi type
     * @param type thw type
     * @param value the object
     * @return String in hex format
     * @throws UnsupportedEncodingException UnsupportedEncodingException
     */
    public static String hexEncodeAbiType(String type, Object value) throws UnsupportedEncodingException {

        if (type.contains("[")) {
            String subType= type.replaceAll("\\[\\]", "");
            Object[] items= (String[]) value;
            String result= "";
            for (Object item: items)    {
                result= result + hexEncodeAbiType(subType, item);
            }
            return result;
        } else if (type.contains("bool"))
            return TypeEncoder.encode(new Bool((boolean) value));
        else if (type.contains("uint"))
            return TypeEncoder.encode(new Uint(BigInteger.valueOf((Integer) value)));
        else if (type.contains("address"))
            return TypeEncoder.encode(new Address((String) value));
        else if (type.contains("bytes"))
            return encodeToHex((String) value);
        else if (type.equals("string"))
            return encodeToHex((String) value);

        return encodeToHex((String) value);

    }

    public static String signatureToString(Sign.SignatureData signatureData)    {
        return EthereumHelper.remove0x(
                Integer.toHexString(signatureData.getV())
                + Numeric.toHexString(signatureData.getR())
                + Numeric.toHexString(signatureData.getS())
        );
    }

    public static Sign.SignatureData stringToSignature(String signatureString) throws EncodingException {
        if (signatureString.length() != 130)
            throw new EncodingException(
                    "Error deserializing string to SignatureData, invalid length:" + signatureString.length());

        byte[] v= Numeric.hexStringToByteArray(signatureString.substring(0, 2));
        byte[] r= Numeric.hexStringToByteArray(signatureString.substring(2, 66));
        byte[] s= Numeric.hexStringToByteArray(signatureString.substring(66, 130));

        return new Sign.SignatureData(v[0], r, s);
    }
}
