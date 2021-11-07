package com.fraunhofer.de.datamongo.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

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
    @Field("class")
    private String clas;
    private HashMap<String,String> prolog;
    private HashMap<String,String> propertiesMap;
}
