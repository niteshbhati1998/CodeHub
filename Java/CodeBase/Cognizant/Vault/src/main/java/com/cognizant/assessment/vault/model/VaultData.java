package com.cognizant.assessment.vault.model;

import lombok.*;

@Data
@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class VaultData {
    @NonNull
    public String key;

    @NonNull
    public String value;
}
