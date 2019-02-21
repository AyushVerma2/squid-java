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

/**
 * Class that represents the entry point to initialize and use the API
 */
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
    private Dispenser dispenser;
    private DIDRegistry didRegistryContract;
    private ServiceExecutionAgreement serviceExecutionAgreementContract;
    private PaymentConditions paymentConditionsContract;
    private AccessConditions accessConditionsContract;

    private AccountsAPI accountsAPI;
    private AssetsAPI assetsAPI;
    private SecretStoreAPI secretStoreAPI;

    private Account mainAccount;

    private static OceanAPI oceanAPI = null;


    /**
     * Private constructor
     * @param oceanConfig
     */
    private OceanAPI(OceanConfig oceanConfig){
        this.oceanConfig = oceanConfig;
    }

    /**
     * Transform a TypeSafe Config object into a Java's Properties
     * @param config
     * @return a Properties object with the configuration of the API
     */
    private static Properties toProperties(Config config) {
        Properties properties = new Properties();
        config.entrySet().forEach(e -> properties.setProperty(e.getKey(), config.getString(e.getKey())));
        return properties;
    }

    /**
     * Build an Instance of Ocean API from a Properties object
     * @param properties
     * @return an Initialized OceanAPI object
     * @throws InitializationException
     * @throws InvalidConfiguration
     */
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
            oceanAPI.serviceExecutionAgreementContract = oceanInitializationHelper.loadServiceExecutionAgreementContract(oceanAPI.keeperService);
            oceanAPI.paymentConditionsContract = oceanInitializationHelper.loadPaymentConditionsContract(oceanAPI.keeperService);
            oceanAPI.accessConditionsContract = oceanInitializationHelper.loadAccessConditionsContract(oceanAPI.keeperService);
            oceanAPI.dispenser = oceanInitializationHelper.loadDispenserContract(oceanAPI.keeperService);
            oceanAPI.tokenContract = oceanInitializationHelper.loadOceanTokenContract(oceanAPI.keeperService);

            oceanAPI.oceanManager = oceanInitializationHelper.getOceanManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.oceanManager.setSecretStoreManager(oceanAPI.secretStoreManager)
                    .setDidRegistryContract(oceanAPI.didRegistryContract)
                    .setServiceExecutionAgreementContract(oceanAPI.serviceExecutionAgreementContract)
                    .setPaymentConditionsContract(oceanAPI.paymentConditionsContract)
                    .setAccessConditionsContract(oceanAPI.accessConditionsContract)
                    .setMainAccount(oceanAPI.mainAccount)
                    .setEvmDto(oceanAPI.evmDto);

            oceanAPI.accountsManager = oceanInitializationHelper.getAccountsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.accountsManager.setTokenContract(oceanAPI.tokenContract);
            oceanAPI.accountsManager.setDispenserContract(oceanAPI.dispenser);
            oceanAPI.accountsManager .setMainAccount(oceanAPI.mainAccount);
            oceanAPI.assetsManager = oceanInitializationHelper.getAssetsManager(oceanAPI.keeperService, oceanAPI.aquariusService);
            oceanAPI.assetsManager.setMainAccount(oceanAPI.mainAccount);

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

    /**
     * Build an Instance of Ocean API from a TypeSafe Config object
     * @param config
     * @return an Initialized OceanAPI object
     * @throws InitializationException
     * @throws InvalidConfiguration
     */
    public static OceanAPI getInstance(Config config) throws InitializationException, InvalidConfiguration{
       return OceanAPI.getInstance(OceanAPI.toProperties(config));
    }

    /**
     * Gets the account used to initialized the API
     * @return the account used to initialized the API
     */
    public Account getMainAccount() {
        return this.mainAccount;
    }

    /**
     * Gets the AccountsAPI
     * @return an instance of an Implementation class of AccountsAPI
     */
    public AccountsAPI getAccountsAPI() {
        return this.accountsAPI;
    }

    /**
     * Gets the AssetsAPI
     * @return an instance of an Implementation class of AssetsAPI
     */
    public AssetsAPI getAssetsAPI() {
        return this.assetsAPI;
    }

    /**
     * Gets the SecretStoreAPI
     * @return an instance of an Implementation class of SecretStoreAPI
     */
    public SecretStoreAPI getSecretStoreAPI() {
        return this.secretStoreAPI;
    }

}
