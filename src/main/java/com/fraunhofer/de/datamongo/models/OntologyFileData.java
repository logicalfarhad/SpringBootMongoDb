package com.fraunhofer.de.datamongo.models;


import lombok.Data;

@Data
public class OntologyFileData {
    private String filename;
    private String url;
    private Long size;
}
