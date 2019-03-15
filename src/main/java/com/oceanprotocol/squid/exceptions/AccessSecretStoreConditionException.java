package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with AccessSecretStore Condition issues
 */
public class AccessSecretStoreConditionException extends OceanException{

    public AccessSecretStoreConditionException(String message, Throwable e) {
        super(message, e);
    }

    public AccessSecretStoreConditionException(String message) {
        super(message);
    }
}
