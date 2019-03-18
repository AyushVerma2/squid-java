package com.oceanprotocol.squid.api;

import com.fasterxml.jackson.core.type.TypeReference;
import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.squid.api.config.OceanConfig;
import com.oceanprotocol.squid.core.sla.ServiceAgreementHandler;
import com.oceanprotocol.squid.exceptions.*;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import com.oceanprotocol.squid.helpers.EncodingHelper;
import com.oceanprotocol.squid.helpers.EthereumHelper;
import com.oceanprotocol.squid.manager.ManagerHelper;
import com.oceanprotocol.squid.manager.OceanManager;
import com.oceanprotocol.squid.manager.SecretStoreManager;
import com.oceanprotocol.squid.models.Account;
import com.oceanprotocol.squid.models.Balance;
import com.oceanprotocol.squid.models.DDO;
import com.oceanprotocol.squid.models.DID;
import com.oceanprotocol.squid.models.asset.AssetMetadata;
import com.oceanprotocol.squid.models.asset.OrderResult;
import com.oceanprotocol.squid.models.service.ServiceEndpoints;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import io.reactivex.Flowable;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.BeforeClass;
import org.junit.Test;

import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class AgreementConditionsApiIT {


    public static class OceanManagerForTest extends OceanManager {

        protected OceanManagerForTest(KeeperService keeperService, AquariusService aquariusService) {
            super(keeperService, aquariusService);
        }

        public static OceanManagerForTest getInstance(KeeperService keeperService, AquariusService aquariusService) {
            return new OceanManagerForTest(keeperService, aquariusService);
        }


        @Override
        public Flowable<OrderResult> purchaseAsset(DID did, String serviceDefinitionId)
                throws OrderException {

            String serviceAgreementId = ServiceAgreementHandler.generateSlaId();
            DDO ddo;
            // Checking if DDO is already there and serviceDefinitionId is included
            try {

                ddo= resolveDID(did);
            } catch (DDOException |EthereumException e) {
                log.error("Error resolving did[" + did.getHash() + "]: " + e.getMessage());
                throw new OrderException("Error processing Order with DID " + did.getDid(), e);
            }

            try {

                return this.initializeServiceAgreement(did, ddo, serviceDefinitionId, serviceAgreementId)
                        .map(event ->new OrderResult(serviceAgreementId, false, false))
                        .timeout(60, TimeUnit.SECONDS)
                        .onErrorReturn(throwable -> {
                            String msg = "There was a problem executing the Service Agreement " + serviceAgreementId;
                            throw new ServiceAgreementException(serviceAgreementId, msg, throwable);
                        });

            }catch (DDOException|ServiceException |ServiceAgreementException e){
                String msg = "Error processing Order with DID " + did.getDid() + "and ServiceAgreementID " + serviceAgreementId;
                log.error(msg  + ": " + e.getMessage());
                throw new OrderException(msg, e);
            }
        }

    }

    private static final Logger log = LogManager.getLogger(AgreementConditionsApiIT.class);

    private static String METADATA_JSON_SAMPLE = "src/test/resources/examples/metadata.json";
    private static String METADATA_JSON_CONTENT;
    private static AssetMetadata metadataBase;
    private static ServiceEndpoints serviceEndpoints;
    private static OceanAPI oceanAPI;
    private static OceanAPI oceanAPIConsumer;

    private static Config config = ConfigFactory.load();

    private static OceanManagerForTest managerConsumer;
    private static KeeperService keeperConsumer;
    private static AquariusService aquarius;
    private static SecretStoreManager secretStore;

    private static DIDRegistry didRegistry;
    private static EscrowReward escrowReward;
    private static AccessSecretStoreCondition accessSecretStoreCondition;
    private static LockRewardCondition lockRewardCondition;
    private static EscrowAccessSecretStoreTemplate escrowAccessSecretStoreTemplate;

    private static Account consumerAccount;

    private static final String DID_REGISTRY_CONTRACT;
    static {
        DID_REGISTRY_CONTRACT = config.getString("contract.DIDRegistry.address");
    }

    private static final String ESCROW_REWARD_CONTRACT;
    static {
        ESCROW_REWARD_CONTRACT = config.getString("contract.EscrowReward.address");
    }

    private static final String LOCK_REWARD_CONTRACT;
    static {
        LOCK_REWARD_CONTRACT = config.getString("contract.LockRewardCondition.address");
    }


    private static final String ACCESS_SS_CONDITION_CONTRACT;
    static {
        ACCESS_SS_CONDITION_CONTRACT = config.getString("contract.AccessSecretStoreCondition.address");
    }

    private static final String ESCROW_ACCESS_CONTRACT;
    static {
        ESCROW_ACCESS_CONTRACT = config.getString("contract.EscrowAccessSecretStoreTemplate.address");
    }

    private static final String OCEAN_TOKEN_CONTRACT;
    static {
        OCEAN_TOKEN_CONTRACT = config.getString("contract.OceanToken.address");
    }

    @BeforeClass
    public static void setUp() throws Exception {

        METADATA_JSON_CONTENT =  new String(Files.readAllBytes(Paths.get(METADATA_JSON_SAMPLE)));
        metadataBase = DDO.fromJSON(new TypeReference<AssetMetadata>() {}, METADATA_JSON_CONTENT);

        String metadataUrl= "http://172.15.0.15:5000/api/v1/aquarius/assets/ddo/{did}";
        String consumeUrl= "http://localhost:8030/api/v1/brizo/services/consume?consumerAddress=${consumerAddress}&serviceAgreementId=${serviceAgreementId}&url=${url}";
        String purchaseEndpoint= "http://localhost:8030/api/v1/brizo/services/access/initialize";

        serviceEndpoints= new ServiceEndpoints(consumeUrl, purchaseEndpoint, metadataUrl);

        config = ConfigFactory.load();
        oceanAPI = OceanAPI.getInstance(config);

        assertNotNull(oceanAPI.getAssetsAPI());
        assertNotNull(oceanAPI.getMainAccount());

        Properties properties = new Properties();
        properties.put(OceanConfig.KEEPER_URL, config.getString("keeper.url"));
        properties.put(OceanConfig.KEEPER_GAS_LIMIT, config.getString("keeper.gasLimit"));
        properties.put(OceanConfig.KEEPER_GAS_PRICE, config.getString("keeper.gasPrice"));
        properties.put(OceanConfig.AQUARIUS_URL, config.getString("aquarius.url"));
        properties.put(OceanConfig.SECRETSTORE_URL, config.getString("secretstore.url"));
        properties.put(OceanConfig.CONSUME_BASE_PATH, config.getString("consume.basePath"));
        properties.put(OceanConfig.MAIN_ACCOUNT_ADDRESS, config.getString("account.parity.address2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_PASSWORD,  config.getString("account.parity.password2"));
        properties.put(OceanConfig.MAIN_ACCOUNT_CREDENTIALS_FILE, config.getString("account.parity.file2"));
        properties.put(OceanConfig.DID_REGISTRY_ADDRESS, config.getString("contract.DIDRegistry.address"));
        properties.put(OceanConfig.AGREEMENT_STORE_MANAGER_ADDRESS, config.getString("contract.AgreementStoreManager.address"));
        properties.put(OceanConfig.LOCKREWARD_CONDITIONS_ADDRESS,config.getString("contract.LockRewardCondition.address"));
        properties.put(OceanConfig.ESCROWREWARD_CONDITIONS_ADDRESS,config.getString("contract.EscrowReward.address"));
        properties.put(OceanConfig.ESCROW_ACCESS_SS_CONDITIONS_ADDRESS,config.getString("contract.EscrowAccessSecretStoreTemplate.address"));
        properties.put(OceanConfig.ACCESS_SS_CONDITIONS_ADDRESS, config.getString("contract.AccessSecretStoreCondition.address"));
        properties.put(OceanConfig.TOKEN_ADDRESS, config.getString("contract.OceanToken.address"));
        properties.put(OceanConfig.DISPENSER_ADDRESS, config.getString("contract.Dispenser.address"));

        oceanAPIConsumer = OceanAPI.getInstance(properties);
        oceanAPIConsumer.getAccountsAPI().requestTokens(BigInteger.valueOf(1000));
        Balance balance= oceanAPIConsumer.getAccountsAPI().balance(oceanAPIConsumer.getMainAccount());
        log.debug("Account " + oceanAPIConsumer.getMainAccount().address + " balance is: " + balance.toString());

        keeperConsumer = ManagerHelper.getKeeper(config, ManagerHelper.VmClient.parity, "2");
        didRegistry= ManagerHelper.loadDIDRegistryContract(keeperConsumer, DID_REGISTRY_CONTRACT);
        escrowReward= ManagerHelper.loadEscrowRewardContract(keeperConsumer, ESCROW_REWARD_CONTRACT);
        accessSecretStoreCondition= ManagerHelper.loadAccessSecretStoreConditionContract(keeperConsumer, ACCESS_SS_CONDITION_CONTRACT);
        lockRewardCondition= ManagerHelper.loadLockRewardCondition(keeperConsumer, LOCK_REWARD_CONTRACT);
        escrowAccessSecretStoreTemplate= ManagerHelper.loadEscrowAccessSecretStoreTemplate(keeperConsumer, ESCROW_ACCESS_CONTRACT);

        EvmDto evmDto = ManagerHelper.getEvmDto(config, ManagerHelper.VmClient.parity);
        consumerAccount = new Account(config.getString("account.parity.address2"), config.getString("account.parity.password2"));
        aquarius= ManagerHelper.getAquarius(config);
        secretStore= ManagerHelper.getSecretStoreController(config, evmDto);

        managerConsumer = OceanManagerForTest.getInstance(keeperConsumer, aquarius);
        managerConsumer.setSecretStoreManager(secretStore)
                .setDidRegistryContract(didRegistry)
                .setEscrowReward(escrowReward)
                .setAccessSecretStoreCondition(accessSecretStoreCondition)
                .setLockRewardCondition(lockRewardCondition)
                .setEscrowAccessSecretStoreTemplate(escrowAccessSecretStoreTemplate)
                .setMainAccount(consumerAccount)
                .setEvmDto(evmDto);

    }


    @Test
    public void testConditions() throws Exception {

        //We create an asset as a Publisher
        DDO ddo = oceanAPI.getAssetsAPI().create(metadataBase, serviceEndpoints);
        DID did= new DID(ddo.id);
        DDO resolvedDDO= oceanAPI.getAssetsAPI().resolve(did);
        assertEquals(ddo.id, resolvedDDO.id);

        // As a Consumer, we order the asset using the OceanManagerForTest class
        Flowable<OrderResult> response = managerConsumer.purchaseAsset(did, "1");
        String serviceAgreementId = response.blockingFirst().getServiceAgreementId();

        //Now we can test the conditions
        Boolean result = oceanAPIConsumer.getAgreementConditionsAPI().lockReward(serviceAgreementId,Integer.valueOf(ddo.metadata.base.price));
        assertEquals(true, result);

        /*
        IT DOESN'T WORK BECAUSE BRIZO FULFILL THESE CONDITIONS
        result = oceanAPI.getAgreementConditionsAPI().grantAccess(serviceAgreementId, did.getHash(), oceanAPIConsumer.getMainAccount().getAddress());
        assertEquals(true, result);

        result = oceanAPI.getAgreementConditionsAPI().releaseReward(serviceAgreementId, Integer.valueOf(ddo.metadata.base.price));
        assertEquals(true, result);
        */


    }



}
