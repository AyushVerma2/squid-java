package com.oceanprotocol.squid.api;

import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.api.config.OceanConfigFactory;
import com.oceanprotocol.squid.api.helper.OceanInitializationHelper;
import com.oceanprotocol.squid.api.impl.AccountsImpl;
import com.oceanprotocol.squid.api.impl.AssetsImpl;
import com.oceanprotocol.squid.api.impl.SecretStoreImpl;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.exceptions.InitializationException;
import com.oceanprotocol.squid.exceptions.InvalidConfiguration;
import com.oceanprotocol.squid.manager.AccountsManager;
import com.oceanprotocol.squid.manager.AssetsManager;
import com.oceanprotocol.squid.manager.OceanManager;
import com.oceanprotocol.squid.manager.SecretStoreManager;
import com.oceanprotocol.squid.models.Account;
import com.typesafe.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Properties;

public class OceanAPI {

    static final Logger log= LogManager.getLogger(OceanAPI.class);

    private OceanConfig oceanConfig;

    private KeeperService keeperService;
    private AquariusService aquariusService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;

    private SecretStoreManager secretStoreManager;
    private OceanManager oceanManager;
    private AssetsManager assetsManager;
    private AccountsManager accountsManager;

    private OceanToken tokenContract;
    private OceanMarket oceanMarketContract;
    private DIDRegistry didRegistryContract;
    private ServiceAgreement serviceAgreementContract;
    private PaymentConditions paymentConditionsContract;
    private AccessConditions accessConditionsContract;

    private AccountsAPI accountsAPI;
    private AssetsAPI assetsAPI;
    private SecretStoreAPI secretStoreAPI;

    private Account mainAccount;

    private static OceanAPI oceanAPI = null;


    private OceanAPI(OceanConfig oceanConfig){
        this.oceanConfig = oceanConfig;
    }

    private static Properties toProperties(Config config) {
        Properties properties = new Properties();
        config.entrySet().forEach(e -> properties.setProperty(e.getKey(), config.getString(e.getKey())));
        return properties;
    }

    public static OceanAPI getInstance(Properties properties) throws InitializationException, InvalidConfiguration {

        if (oceanAPI != null)
            return oceanAPI;

        OceanConfig oceanConfig = OceanConfigFactory.getOceanConfig(properties);
        OceanConfig.OceanConfigValidation validation = OceanConfig.validate(oceanConfig);

        if (!validation.isValid()) {
            String msg= "Error Initializing Ocean API. Configuration not valid " + validation.errorsToString();
            log.error(msg);
            throw new InvalidConfiguration(msg);
        }

        oceanAPI = new OceanAPI(oceanConfig);

        oceanAPI.mainAccount = new Account(oceanConfig.getMainAccountAddress(), oceanConfig.getMainAccountPassword());

        OceanInitializationHelper oceanInitializationHelper = new OceanInitializationHelper(oceanConfig);

        try {
            oceanAPI.oceanConfig = oceanConfig;
            oceanAPI.aquariusService = oceanInitializationHelper.getAquarius();
            oceanAPI.keeperService = oceanInitializationHelper.getKeeper();
            oceanAPI.secretStoreDto = oceanInitializationHelper.getSecretStoreDto();
            oceanAPI.evmDto = oceanInitializationHelper.getEvmDto();
            oceanAPI.secretStoreManager = oceanInitializationHelper.getSecretStoreManager(oceanAPI.secretStoreDto, oceanAPI.evmDto);

            oceanAPI.didRegistryContract = oceanInitializationHelper.loadDIDRegistryContract(oceanAPI.keeperService);
            oceanAPI.serviceAgreementContract = oceanInitializationHelper.loadServiceAgreementContract(oceanAPI.keeperService);
            oceanAPI.paymentConditionsContract = oceanInitializationHelper.loadPaymentConditionsContract(oceanAPI.keeperService);
            oceanAPI.accessConditionsContract = oceanInitializationHelper.loadAccessConditionsContract(oceanAPI.keeperService);
            oceanAPI.oceanMarketContract = oceanInitializationHelper.loadOceanMarketContract(oceanAPI.keeperService);
            oceanAPI.tokenContract = oceanInitializationHelper.loadOceanTokenContract(oceanAPI.keeperService);

            oceanAPI.oceanManager = oceanInitializationHelper.getOceanManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.oceanManager.setSecretStoreManager(oceanAPI.secretStoreManager)
                    .setDidRegistryContract(oceanAPI.didRegistryContract)
                    .setServiceAgreementContract(oceanAPI.serviceAgreementContract)
                    .setPaymentConditionsContract(oceanAPI.paymentConditionsContract)
                    .setAccessConditionsContract(oceanAPI.accessConditionsContract);

            oceanAPI.accountsManager = oceanInitializationHelper.getAccountsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.accountsManager.setTokenContract(oceanAPI.tokenContract);
            oceanAPI.accountsManager.setOceanMarketContract(oceanAPI.oceanMarketContract);
            oceanAPI.assetsManager = oceanInitializationHelper.getAssetsManager(oceanAPI.keeperService, oceanAPI.aquariusService);

            oceanAPI.accountsAPI = new AccountsImpl(oceanAPI.accountsManager);
            oceanAPI.secretStoreAPI = new SecretStoreImpl(oceanAPI.secretStoreManager);
            oceanAPI.assetsAPI = new AssetsImpl(oceanAPI.oceanManager, oceanAPI.assetsManager);

            return oceanAPI;
        }catch (Exception e){
            String msg= "Error Initializing Ocean API";
            log.error(msg + ": " + e.getMessage());
            throw new InitializationException(msg, e);
        }
    }

    public static OceanAPI getInstance(Config config) throws InitializationException, InvalidConfiguration{
       return OceanAPI.getInstance(OceanAPI.toProperties(config));
    }

    public Account getMainAccount() {
        return this.mainAccount;
    }

    public AccountsAPI getAccountsAPI() {
        return this.accountsAPI;
    }

    public AssetsAPI getAssetsAPI() {
        return this.assetsAPI;
    }

    public SecretStoreAPI getSecretStoreAPI() {
        return this.secretStoreAPI;
    }

}
