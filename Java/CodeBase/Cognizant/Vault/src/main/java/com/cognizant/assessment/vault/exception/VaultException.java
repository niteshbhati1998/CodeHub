package com.cognizant.assessment.vault.exception;

public class VaultException extends RuntimeException {

    public VaultException(Exception e) {
        super(e);
    }

    public VaultException() {
        super();
    }

    public VaultException(String message) {
        super(message);
    }
}
