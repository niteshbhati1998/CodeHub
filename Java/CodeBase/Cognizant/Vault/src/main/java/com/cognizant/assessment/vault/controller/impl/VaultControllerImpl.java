package com.cognizant.assessment.vault.controller.impl;

import com.cognizant.assessment.vault.controller.api.VaultControllerInterface;
import com.cognizant.assessment.vault.model.VaultData;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.vault.VaultException;
import org.springframework.vault.core.VaultKeyValueOperationsSupport;
import org.springframework.vault.core.VaultOperations;
import org.springframework.vault.support.VaultResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service("VaultController")
public class VaultControllerImpl implements VaultControllerInterface {

    private static Logger LOGGER = LoggerFactory.getLogger(VaultControllerImpl.class);
    @Autowired
    VaultOperations vaultOperations;

    //environment.getProperty("test.dbuser")
    @Value("${lineage.fetch.key}")
    private String fetchKey;

    @Value("${lineage.fetch.engine}")
    private String fetchEngine;

    @Override
    public List<VaultData> getAllSecrets(String path) {
        LOGGER.info("Execution of getAllSecret started.");
        List<VaultData> vaultDataList = new ArrayList<>();
        try {
            VaultResponse vaultResponse = vaultOperations.opsForKeyValue(fetchEngine, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(path);
            if (null == vaultResponse) {
                throw new com.cognizant.assessment.vault.exception.VaultException("Secrets not found for the given path");
            }
            Map<String, Object> vaultSecrectMap = vaultResponse.getData();
            if (null == vaultSecrectMap || vaultSecrectMap.isEmpty()) {
                return vaultDataList;
            }
            vaultSecrectMap.forEach((k, v) -> {
                VaultData vaultData = new VaultData();
                vaultData.setKey(k);
                vaultData.setValue(vaultOperations.opsForTransit().decrypt(fetchKey, v.toString()));
                vaultDataList.add(vaultData);
            });
        } catch (VaultException e) {
            LOGGER.info("Execution of getAllSecret received exception : ", e);
            throw new com.cognizant.assessment.vault.exception.VaultException(e);
        }
        LOGGER.info("Execution of getAllSecret ended.");
        return vaultDataList;
    }

    @Override
    public VaultData getKeyValueForSecret(String path, String key) {
        VaultData vaultData = null;
        LOGGER.info("Execution of getKeyValueForSecret started.");
        try {
            VaultResponse vaultResponse = vaultOperations.opsForKeyValue(fetchEngine, VaultKeyValueOperationsSupport.KeyValueBackend.KV_2).get(path);
            if (null == vaultResponse) {
                throw new com.cognizant.assessment.vault.exception.VaultException("Secrets not found for the given path");
            }
            Map<String, Object> vaultSecrectMap = vaultResponse.getData();
            if (null == vaultSecrectMap || vaultSecrectMap.isEmpty()) {
                throw new com.cognizant.assessment.vault.exception.VaultException("No value present for the given path");
            }

            if (!vaultSecrectMap.containsKey(key)) {
                throw new com.cognizant.assessment.vault.exception.VaultException("No value present for given key");
            }

            String vaultKeyValue = vaultSecrectMap.get(key).toString();

            String decryptedVaultValue = vaultOperations.opsForTransit().decrypt(fetchKey, vaultKeyValue);
            vaultData = new VaultData(key, decryptedVaultValue);

        } catch (VaultException ex) {
            LOGGER.info("Execution of getKeyValueForSecret received exception : ", ex);
            throw new com.cognizant.assessment.vault.exception.VaultException(ex);
        }
        LOGGER.info("Execution of getAllSecret ended.");
        return vaultData;
    }
}
