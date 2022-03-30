package com.fraunhofer.de.datamongo.models;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Builder
public class Option {
    private String delimiter;
    private String header;
}
