package com.cognizant.assessment.vault.controller.api;

import com.cognizant.assessment.vault.model.VaultData;

import java.util.List;

public interface VaultControllerInterface {
    List<VaultData> getAllSecrets(String path);

    VaultData getKeyValueForSecret(String path, String key);
}
