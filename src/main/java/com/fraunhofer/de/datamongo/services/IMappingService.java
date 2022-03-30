package com.fraunhofer.de.datamongo.services;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.MappingDTO;
import com.fraunhofer.de.datamongo.models.Schema;
import com.fraunhofer.de.datamongo.models.VocolInfo;

import java.util.List;

public interface IMappingService {
    List<Mapping> getAllMappingByInstanceName(String instanceName);
    void deleteById(String Id);

    Mapping update(String Id, MappingDTO mappingDTO);

    Mapping save(Mapping mapping);

    Mapping getMappingById(String Id);

    Schema getSchemaById(String Id);

    void deleteAll();

    VocolInfo generateMapping();

    void setVocolInfo(VocolInfo vocolInfo);

    VocolInfo getVocolInfo();

    Mapping setMappingSettings(Mapping mapping);
}
