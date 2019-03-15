package com.oceanprotocol.squid.models;

public class AgreementData {

    String agreementId;
    byte[] agreementIdBytes;

    String accessSecretStoreConditionId;
    byte[]accessSecretStoreConditionIdBytes;

    String lockRewardConditionId;
    byte[] lockRewardConditionIdBytes;

    String consumerAdress;
    String providerAdress;

    public String getAgreementId() {
        return agreementId;
    }

    public void setAgreementId(String agreementId) {
        this.agreementId = agreementId;
    }

    public byte[] getAgreementIdBytes() {
        return agreementIdBytes;
    }

    public void setAgreementIdBytes(byte[] agreementIdBytes) {
        this.agreementIdBytes = agreementIdBytes;
    }

    public String getAccessSecretStoreConditionId() {
        return accessSecretStoreConditionId;
    }

    public void setAccessSecretStoreConditionId(String accessSecretStoreConditionId) {
        this.accessSecretStoreConditionId = accessSecretStoreConditionId;
    }

    public byte[] getAccessSecretStoreConditionIdBytes() {
        return accessSecretStoreConditionIdBytes;
    }

    public void setAccessSecretStoreConditionIdBytes(byte[] accessSecretStoreConditionIdBytes) {
        this.accessSecretStoreConditionIdBytes = accessSecretStoreConditionIdBytes;
    }

    public String getLockRewardConditionId() {
        return lockRewardConditionId;
    }

    public void setLockRewardConditionId(String lockRewardConditionId) {
        this.lockRewardConditionId = lockRewardConditionId;
    }

    public byte[] getLockRewardConditionIdBytes() {
        return lockRewardConditionIdBytes;
    }

    public void setLockRewardConditionIdBytes(byte[] lockRewardConditionIdBytes) {
        this.lockRewardConditionIdBytes = lockRewardConditionIdBytes;
    }

    public String getConsumerAdress() {
        return consumerAdress;
    }

    public void setConsumerAdress(String consumerAdress) {
        this.consumerAdress = consumerAdress;
    }

    public String getProviderAdress() {
        return providerAdress;
    }

    public void setProviderAdress(String providerAdress) {
        this.providerAdress = providerAdress;
    }
}
