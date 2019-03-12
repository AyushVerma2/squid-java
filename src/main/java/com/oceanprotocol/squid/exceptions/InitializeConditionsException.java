/*
 * Copyright BigchainDB GmbH and BigchainDB contributors
 * SPDX-License-Identifier: Apache-2.0
 */

package com.oceanprotocol.squid.exceptions;

/**
 * Business Exception related with issues during the Initialization of the Conditions of a Service
 */
public class InitializeConditionsException extends OceanException {

    public InitializeConditionsException(String message, Throwable e) {
        super(message, e);
    }

    public InitializeConditionsException(String message) {
        super(message);
    }
}
