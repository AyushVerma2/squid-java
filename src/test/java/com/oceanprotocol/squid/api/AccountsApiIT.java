/*
 * Copyright 2018 Ocean Protocol Foundation
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.api;

import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.protocol.core.methods.response.TransactionReceipt;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class AccountsApiIT {

    private static final Logger log = LogManager.getLogger(AccountsApiIT.class);

    private static OceanAPI oceanAPI;

    @BeforeClass
    public static void setUp() throws Exception {


        Config config = ConfigFactory.load();
        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAccountsAPI());
        assertNotNull(oceanAPI.getMainAccount());

    }

    @Test
    public void list() throws Exception {

        List<Account> accounts = oceanAPI.getAccountsAPI().list();
        assertNotNull(accounts);
        assertTrue(!accounts.isEmpty());
    }

    @Test
    public void balance() throws Exception {

        Balance balance = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());
        assertNotNull(balance);
        assertEquals(1, balance.getEth().compareTo(BigInteger.ZERO));
        assertEquals(1, balance.getDrops().compareTo(BigInteger.ZERO));
    }

    @Test
    public void requestTokens() throws Exception {

        BigInteger tokens = BigInteger.ONE;

        Balance balanceBefore = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());
        log.debug("Balance before: " + balanceBefore.toString());

        TransactionReceipt receipt = oceanAPI.getAccountsAPI().requestTokens(tokens);

        assertTrue(receipt.isStatusOK());

        Balance balanceAfter = oceanAPI.getAccountsAPI().balance(oceanAPI.getMainAccount());

        log.debug("Balance after: " + balanceAfter.toString());

        BigDecimal before= balanceBefore.getOceanTokens();
        BigDecimal after= balanceAfter.getOceanTokens();
        assertEquals(0, after.compareTo(before.add(BigDecimal.ONE)));
    }

}
