package com.oceanprotocol.squid.external.parity.methods.response;

import org.web3j.protocol.core.Response;

public class ParitySquidPersonalSign extends Response<String> {

    public String getSign(){
        return getResult();
    }

}
