package com.oceanprotocol.squid.helpers;

import com.oceanprotocol.squid.dto.KeeperDto;
import com.oceanprotocol.squid.manager.ManagerHelper;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.junit.BeforeClass;
import org.junit.Test;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Sign;

import java.io.IOException;

import static org.junit.Assert.*;

public class EthereumHelperTestIT {

    private static final Config config = ConfigFactory.load();
    private static KeeperDto keeper;


    @BeforeClass
    public static void setUp() throws Exception {
        keeper = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity);
    }

    @Test
    public void getFunctionSelector() {

        assertEquals("0xc48d6d5e",
                EthereumHelper.getFunctionSelector("sendMessage(string,address)"));

        assertEquals("0x668453f0",
                EthereumHelper.getFunctionSelector("lockPayment(bytes32,bytes32,uint256)"));
    }


    @Test
    public void signMessage() throws IOException, CipherException {
        String message = "Hi there";
        Sign.SignatureData signedMessage = EthereumHelper.signMessage(message, keeper.getCredentials());

        assertTrue(signedMessage.getR().length == 32);
        assertTrue(signedMessage.getS().length == 32);

    }

    @Test
    public void ethSignMessage() throws IOException, CipherException {
        String message = "Hi there";

        String signedMaessage= EthereumHelper.ethEncodeAndSignMessage(keeper.getWeb3(), message, keeper.getCredentials().getAddress());

        assertTrue( signedMaessage.length() == 132);
    }


}