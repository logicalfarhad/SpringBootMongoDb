package com.fraunhofer.de.datamongo.services;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.Schema;
import com.fraunhofer.de.datamongo.models.VocolInfo;

import java.util.List;

public interface IMappingService {
    List<Mapping> getAllMapping();

    void deleteById(String Id);

    Mapping update(String Id, Mapping mapping);

    Mapping save(Mapping mapping);

    Mapping getMappingById(String Id);

    Schema getSchemaById(String Id);

    void deleteAll();

    VocolInfo generateMapping();

    void setVocolInfo(String branchName, String instanceName);
}
