package com.fraunhofer.de.datamongo.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(collection = "mapping")
@Builder
public class Mapping {
    @Id
    private String _id;
    private String entity;
    private String type;
    private String source;
    private Option options;
    private List<Setting> settingList;
}
