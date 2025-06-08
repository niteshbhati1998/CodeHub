package com.cognizant.assessment.vault.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
//@AllArgsConstructor
public class VaultDataWithAllFields {
    @JsonProperty(value= "key", required = true)
    public String key;
    @JsonProperty(value= "value", required = true)
    public DBObject value;
}
