package com.fraunhofer.de.datamongo.models;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.util.HashMap;


@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mapping")
public class Mapping {
    @Id
    private String _id;
    private String entity;
    private String type;
    private String source;
    private String ID;
    private Option options;
    private HashMap<String,String> prolog;
    private HashMap<String,String> propertiesMap;
}
