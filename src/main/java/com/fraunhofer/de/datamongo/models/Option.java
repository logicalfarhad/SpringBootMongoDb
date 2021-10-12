package com.fraunhofer.de.datamongo.models;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class Option {
    private String delimiter;
    private String header;
    private String mode;
}
