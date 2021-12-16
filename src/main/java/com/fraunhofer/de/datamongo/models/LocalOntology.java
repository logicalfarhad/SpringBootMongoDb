package com.fraunhofer.de.datamongo.models;

import java.util.ArrayList;
import java.util.List;

public class LocalOntology {
    private String name;
    private String uri;
    private List<LocalOntology> properties = new ArrayList<LocalOntology>();

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public List<LocalOntology> getProperties() {
        return this.properties;
    }

    public void addProperties(LocalOntology ontology) {
        this.properties.add(ontology);
    }
}
