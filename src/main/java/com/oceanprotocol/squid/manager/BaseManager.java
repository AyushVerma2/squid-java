package com.oceanprotocol.squid.manager;

import com.oceanprotocol.keeper.contracts.*;
import com.oceanprotocol.secretstore.core.EvmDto;
import com.oceanprotocol.secretstore.core.SecretStoreDto;
import com.oceanprotocol.squid.external.AquariusService;
import com.oceanprotocol.squid.external.KeeperService;
import org.web3j.crypto.CipherException;
import java.io.IOException;

/**
 * Abstract class for the Managers
 */
public abstract class BaseManager {

    private KeeperService keeperService;
    private AquariusService aquariusService;
    private SecretStoreDto secretStoreDto;
    private EvmDto evmDto;
    private SecretStoreManager secretStoreManager;
    protected OceanToken tokenContract;
    protected Dispenser dispenser;
    protected DIDRegistry didRegistry;
    protected ServiceExecutionAgreement serviceExecutionAgreement;
    protected PaymentConditions paymentConditions;
    protected AccessConditions accessConditions;
    protected ContractAddresses contractAddresses  = new ContractAddresses();

    public static class ContractAddresses {

        private String paymentConditionsAddress;
        private String accessConditionsAddres;

        public ContractAddresses(){}

        public String getPaymentConditionsAddress() {
            return paymentConditionsAddress;
        }

        public void setPaymentConditionsAddress(String paymentConditionsAddress) {
            this.paymentConditionsAddress = paymentConditionsAddress;
        }

        public String getAccessConditionsAddres() {
            return accessConditionsAddres;
        }

        public void setAccessConditionsAddres(String accessConditionsAddres) {
            this.accessConditionsAddres = accessConditionsAddres;
        }
    }

    /**
     * Constructor
     * @param keeperService KeeperService
     * @param aquariusService AquariusService
     * @throws IOException
     * @throws CipherException
     */
    public BaseManager(KeeperService keeperService, AquariusService aquariusService) throws IOException, CipherException {
        this.keeperService = keeperService;
        this.aquariusService = aquariusService;
    }


    public ContractAddresses getContractAddresses() {
        return contractAddresses;
    }

    /**
     * Get the KeeperService
     * @return KeeperService
     */
    public KeeperService getKeeperService() {
        return keeperService;
    }

    /**
     * Set the KeeperService
     * @param keeperService KeeperService
     * @return this
     */
    public BaseManager setKeeperService(KeeperService keeperService) {
        this.keeperService = keeperService;
        return this;
    }

    /**
     * Get the AquariusService
     * @return AquariusService
     */
    public AquariusService getAquariusService() {
        return aquariusService;
    }

    /**
     * Set the AquariusService
     * @param aquariusService AquariusService
     * @return this
     */
    public BaseManager setAquariusService(AquariusService aquariusService) {
        this.aquariusService = aquariusService;
        return this;
    }

    /**
     * Get the SecretStoreDto
     * @return SecretStoreDto
     */
    public SecretStoreDto getSecretStoreDto() {
        return secretStoreDto;
    }

    /**
     * Set the SecretStoreDto
     * @param secretStoreDto SecretStoreDto
     * @return this
     */
    public BaseManager setSecretStoreDto(SecretStoreDto secretStoreDto) {
        this.secretStoreDto = secretStoreDto;
        return this;
    }

    /**
     * Get the SecretStoreManager
     * @return SecretStoreDto
     */
    public SecretStoreManager getSecretStoreManager() {
        return secretStoreManager;
    }

    /**
     * Set the SecretStoreManager
     * @param secretStoreManager SecretStoreDto
     * @return this
     */
    public BaseManager setSecretStoreManager(SecretStoreManager secretStoreManager) {
        this.secretStoreManager = secretStoreManager;
        return this;
    }

    /**
     * Get the EvmDto
     * @return EvmDto
     */
    public EvmDto getEvmDto() {
        return evmDto;
    }

    /**
     * Set the EvmDto necessary to stablish the encryption/decryption flow necessary by Secret Store
     * @param evmDto EvmDto
     * @return this
     */
    public BaseManager setEvmDto(EvmDto evmDto) {
        this.evmDto = evmDto;
        return this;
    }

    /**
     * Initialize the OceanToken object using the address given as parameter to point to the deployed contract
     * @param address OceanToken contract address
     * @return AccountsManager instance
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public BaseManager setTokenContract(String address) throws IOException, CipherException {
        this.tokenContract= OceanToken.load(address,
                getKeeperService().getWeb3(),
                getKeeperService().getCredentials(),
                getKeeperService().getContractGasProvider());
        return this;
    }

    /**
     * It sets the OceanToken stub instance
     * @param contract OceanToken instance
     * @return BaseManager instance
     */
    public BaseManager setTokenContract(OceanToken contract)   {
        this.tokenContract= contract;
        return this;
    }


    /**
     * It sets the Dispenser stub instance
     * @param contract Dispenser instance
     * @return BaseManager instance
     */
    public BaseManager setDispenserContract(Dispenser contract)    {
        this.dispenser= contract;
        return this;
    }

    /**
     * Initialize the DIDRegistry object using the address given as parameter to point to the deployed contract
     * @param address DIDRegistry contract address
     * @return BaseManager instance
     * @throws IOException IOException
     * @throws CipherException CipherException
     */
    public BaseManager setDidRegistryContract(String address) throws IOException, CipherException {
        this.didRegistry= DIDRegistry.load(address,
                getKeeperService().getWeb3(),
                getKeeperService().getCredentials(),
                getKeeperService().getContractGasProvider());
        return this;
    }

    /**
     * It sets the ServiceExecutionAgreement stub instance
     * @param contract ServiceExecutionAgreement instance
     * @return BaseManager instance
     */
    public BaseManager setServiceExecutionAgreementContract(ServiceExecutionAgreement contract)    {
        this.serviceExecutionAgreement= contract;
        return this;
    }

    /**
     * It sets the PaymentConditions stub instance
     * @param contract PaymentConditions instance
     * @return BaseManager instance
     */
    public BaseManager setPaymentConditionsContract(PaymentConditions contract)    {
        this.paymentConditions= contract;
        this.contractAddresses.setPaymentConditionsAddress(this.paymentConditions.getContractAddress());
        return this;
    }

    /**
     * It sets the AccessConditions stub instance
     * @param contract AccessConditions instance
     * @return BaseManager instance
     */
    public BaseManager setAccessConditionsContract(AccessConditions contract)    {
        this.accessConditions= contract;
        this.contractAddresses.setAccessConditionsAddres(this.accessConditions.getContractAddress());
        return this;
    }

    /**
     * It sets the DIDRegistry stub instance
     * @param contract DIDRegistry instance
     * @return BaseManager instance
     */
    public BaseManager setDidRegistryContract(DIDRegistry contract)    {
        this.didRegistry= contract;
        return this;
    }

    @Override
    public String toString() {
        return "BaseManager{" +
                "keeperService=" + keeperService +
                ", aquariusService=" + aquariusService +
                '}';
    }
}
