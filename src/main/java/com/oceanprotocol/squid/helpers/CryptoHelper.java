/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.helpers;

import com.google.common.base.Preconditions;
import org.apache.commons.codec.digest.DigestUtils;
import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Uint;
import org.web3j.abi.datatypes.generated.Uint256;
import org.web3j.abi.datatypes.generated.Uint64;
import org.web3j.crypto.Hash;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.apache.commons.codec.digest.MessageDigestAlgorithms.SHA3_256;

/**
 * Helper abstract class with crypto utility methods
 */
public abstract class CryptoHelper {

    /**
     * The Constant DIGITS.
     */
    private static final char[] DIGITS =
            {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};

    private static final BigInteger MASK_256 = BigInteger.ONE.shiftLeft(256).subtract(BigInteger.ONE);


    /**
     * Given an input string return the result of sha3256
     *
     * @param input string
     * @return hashed message
     */
    public static String sha3256(String input) {
        return new DigestUtils(SHA3_256).digestAsHex(input);
    }

    /**
     * Given N objects, return the SHA3 of those objects
     *
     * @param data objects
     * @return sha3
     */
    public static byte[] soliditySha3(Object... data) {
        if (data.length == 1) {
            return Hash.sha3(toBytes(data[0]));
        }
        List<byte[]> arrays = Stream.of(data).map(CryptoHelper::toBytes).collect(Collectors.toList());
        ByteBuffer buffer = ByteBuffer.allocate(arrays.stream().mapToInt(a -> a.length).sum());
        for (byte[] a : arrays) {
            buffer.put(a);
        }
        byte[] array = buffer.array();
        assert buffer.position() == array.length;
        return Hash.sha3(array);
    }

    /**
     * Gets the hex.
     *
     * @param data the data
     * @return the hex
     */
    public static String getHex(byte[] data) {
        final int l = data.length;
        final char[] outData = new char[l << 1];
        for (int i = 0, j = 0; i < l; i++) {
            outData[j++] = DIGITS[(0xF0 & data[i]) >>> 4];
            outData[j++] = DIGITS[0x0F & data[i]];
        }

        return new String(outData);
    }


    /**
     * Given an object (byte[], BigInteger, Address, Uint or Number) gets the byte[] with the proper length
     *
     * @param obj the object
     * @return byte[]
     */
    public static byte[] toBytes(Object obj) {
        if (obj instanceof byte[]) {
            int length = ((byte[]) obj).length;
            Preconditions.checkArgument(length <= 32);
            if (length < 32) {
                return Arrays.copyOf((byte[]) obj, 32);
            }
            return (byte[]) obj;
        } else if (obj instanceof BigInteger) {
            BigInteger value = (BigInteger) obj;
            if (value.signum() < 0) {
                value = MASK_256.and(value);
            }
            return Numeric.toBytesPadded(value, 32);
        } else if (obj instanceof Address) {
            Uint uint = ((Address) obj).toUint160();
            return Numeric.toBytesPadded(uint.getValue(), 20);
        } else if (obj instanceof Uint256) {
            Uint uint = (Uint) obj;
            return Numeric.toBytesPadded(uint.getValue(), 32);
        } else if (obj instanceof Uint64) {
            Uint uint = (Uint) obj;
            return Numeric.toBytesPadded(uint.getValue(), 8);
        } else if (obj instanceof Number) {
            long l = ((Number) obj).longValue();
            return toBytes(BigInteger.valueOf(l));
        }
        throw new IllegalArgumentException(obj.getClass().getName());
    }

}
