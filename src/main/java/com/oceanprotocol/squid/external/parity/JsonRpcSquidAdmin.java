package com.oceanprotocol.squid.external.parity;

import com.oceanprotocol.squid.external.parity.methods.response.ParitySquidPersonalSign;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.admin.JsonRpc2_0Admin;
import org.web3j.protocol.core.Request;

import java.util.Arrays;

public class JsonRpcSquidAdmin extends JsonRpc2_0Admin {

    public JsonRpcSquidAdmin(Web3jService web3jService) {
        super(web3jService);
    }

    /**
     * Invoke the personal sign method
     *
     * @param data     data to sign
     * @param address  ethereum address
     * @param password password
     * @return signed hash
     */
    public Request<?, ParitySquidPersonalSign> parityPersonalSign(
            String data, String address, String password) {

        return new Request<>(
                "personal_sign",
                Arrays.asList(data, address, password),
                web3jService,
                ParitySquidPersonalSign.class);
    }

}
