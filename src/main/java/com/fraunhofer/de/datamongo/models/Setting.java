package com.fraunhofer.de.datamongo.models;

import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

import java.util.HashMap;

@Data
public class Setting {
    private String key;
    @Field("class")
    private String klass;
    private HashMap<String,String> prologList;
    private HashMap<String,String> propertiesList;
}
