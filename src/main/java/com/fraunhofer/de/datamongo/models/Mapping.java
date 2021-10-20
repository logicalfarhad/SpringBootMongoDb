package com.fraunhofer.de.datamongo.models;

import lombok.*;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.FieldType;
import org.springframework.data.mongodb.core.mapping.MongoId;

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
    private String key;
    private Option options;
    private HashMap<String,String> prolog;
    private HashMap<String,String> propertiesMap;
}
