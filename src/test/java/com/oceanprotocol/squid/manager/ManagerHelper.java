package com.oceanprotocol.squid.manager;

import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import com.typesafe.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.web3j.crypto.CipherException;
import java.io.IOException;
import java.math.BigInteger;

public abstract class ManagerHelper {

    private static final Logger log = LogManager.getLogger(ManagerHelper.class);

    public enum VmClient { ganache, parity}

    public static KeeperService getKeeper(Config config) throws IOException, CipherException {
        return getKeeper(config, VmClient.ganache);
    }

    public static KeeperService getKeeper(Config config, VmClient client) throws IOException, CipherException {
        KeeperService keeper= KeeperService.getInstance(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address"),
                config.getString("account." + client.toString() + ".password"),
                config.getString("account." + client.toString() + ".file")
        );

        keeper.setGasLimit(BigInteger.valueOf(config.getLong("keeper.gasLimit")))
              .setGasPrice(BigInteger.valueOf(config.getLong("keeper.gasPrice")));

        return keeper;
    }


    public static KeeperService getKeeper(Config config, VmClient client, String nAddress) throws IOException, CipherException {
        KeeperService keeper= KeeperService.getInstance(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address" + nAddress),
                config.getString("account." + client.toString() + ".password" + nAddress),
                config.getString("account." + client.toString() + ".file" + nAddress)
        );

        keeper.setGasLimit(BigInteger.valueOf(config.getLong("keeper.gasLimit")))
                .setGasPrice(BigInteger.valueOf(config.getLong("keeper.gasPrice")));

        return keeper;
    }

    public static AquariusService getAquarius(Config config) {
        return AquariusService.getInstance(config.getString("aquarius.url"));
    }

    public static SecretStoreDto getSecretStoreDto(Config config) {
        return SecretStoreDto.builder(config.getString("secretstore.url"));
    }

    public static EvmDto getEvmDto(Config config, VmClient client) {
        return EvmDto.builder(
                config.getString("keeper.url"),
                config.getString("account." + client.toString() + ".address"),
                config.getString("account." + client.toString() + ".password")
        );
    }

    public static SecretStoreManager getSecretStoreController(Config config, VmClient client) {
        return SecretStoreManager.getInstance(getSecretStoreDto(config),getEvmDto(config, client));
    }


    public static OceanToken loadOceanTokenContract(KeeperService keeper, String address) throws Exception, IOException, CipherException {
        return OceanToken.load(
                address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider());
    }


    public static Dispenser loadDispenserContract(KeeperService keeper, String address)
            throws Exception {
        return Dispenser.load(
                address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider()
        );
    }


    public static DIDRegistry loadDIDRegistryContract(KeeperService keeper, String address)
            throws Exception {

        return DIDRegistry.load(
                address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider()
        );
    }


    public static ServiceExecutionAgreement loadServiceExecutionAgreementContract(KeeperService keeper, String address) throws Exception, IOException, CipherException {
        return ServiceExecutionAgreement.load(
                address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider());
    }



    public static PaymentConditions loadPaymentConditionsContract(KeeperService keeper, String address) throws Exception, IOException, CipherException {
        return PaymentConditions.load(
                address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider()
        );
    }



    public static AccessConditions loadAccessConditionsContract(KeeperService keeper, String address) throws Exception, IOException, CipherException {
        return AccessConditions.load(address,
                keeper.getWeb3(),
                keeper.getCredentials(),
                keeper.getContractGasProvider()
                );
    }


}
