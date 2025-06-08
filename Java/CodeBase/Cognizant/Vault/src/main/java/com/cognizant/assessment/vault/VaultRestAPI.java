package com.cognizant.assessment.vault;

import com.cognizant.assessment.vault.controller.api.VaultControllerInterface;
import com.cognizant.assessment.vault.model.DBObject;
import com.cognizant.assessment.vault.model.VaultData;
import com.cognizant.assessment.vault.exception.BadRequestException;
import com.cognizant.assessment.vault.model.VaultDataWithAllFields;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/service/vault")
public class VaultRestAPI {
    private static Logger LOGGER = LoggerFactory.getLogger(VaultRestAPI.class);

    @Autowired
    @Qualifier("VaultController")
    private VaultControllerInterface vaultControllerInterface;

    @Autowired
    private RestTemplate restTemplate;
    @PostMapping(value = "/secrets")
    public ResponseEntity<List<VaultData>> getAllVaultData(@RequestBody Map<String, Object> payload) {
        LOGGER.info("Service called : /getSecrets");
        if(!payload.containsKey("path")) {
            throw new BadRequestException("Path not present");
        }
        List<VaultData> vaultDataList = vaultControllerInterface.getAllSecrets(payload.get("path").toString());
        return new ResponseEntity<>(vaultDataList, HttpStatus.OK);
    }

    @PostMapping(value = "/secretValue")
    public ResponseEntity<VaultData> getAllVaultDataForKey(@RequestBody Map<String, Object> payload) {
        LOGGER.info("Service called : /getSecrets");
        if(!payload.containsKey("path")) {
            throw new BadRequestException("Path not present");
        }
        if(!payload.containsKey("key")) {
            throw new BadRequestException("Key not present");
        }
        VaultData vaultData = vaultControllerInterface.getKeyValueForSecret(payload.get("path").toString(), payload.get("key").toString());
        return new ResponseEntity<>(vaultData, HttpStatus.OK);
    }

    @PostMapping("/getConnectionNameList/{databaseType}")
    public ResponseEntity<?> getDatabaseListForType(@PathVariable String databaseType) {
        try {
            List<String> connectionNameList = new ArrayList();
            HttpHeaders headers = new HttpHeaders();
            headers.set("Content-Type", "application/json");

            String requestBody = "{\"path\": \"application\"}";
            HttpEntity<String> requestEntity = new HttpEntity<>(requestBody,headers);
            String url = "http://13.213.188.19:7777/service/vault/secrets";
            ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, requestEntity, String.class);
            try {
                ObjectMapper objectMapper = new ObjectMapper();
                List<VaultDataWithAllFields> list = objectMapper.readValue(
                        response.getBody(),
                        new TypeReference<>() {}
                );

                connectionNameList = list.stream().filter(vaultDataWithAllField -> vaultDataWithAllField.getValue().getDbtype().equals(databaseType)).map(vaultDataWithAllField -> vaultDataWithAllField.getKey()).collect(Collectors.toList());

            } catch (Exception e) {
                System.out.println("Tes" + e);
            }
            return new ResponseEntity(connectionNameList, HttpStatus.OK);
        } catch (Exception e) {
            LOGGER.info("Exception occured in getDatabaseTypeList Controller" + e.getMessage());
            return new ResponseEntity(null, HttpStatus.BAD_REQUEST);
        }
    }
}
