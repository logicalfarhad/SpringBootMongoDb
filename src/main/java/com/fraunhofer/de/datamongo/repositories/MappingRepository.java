package com.fraunhofer.de.datamongo.repositories;

import com.fraunhofer.de.datamongo.models.Mapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MappingRepository extends MongoRepository<Mapping, String> {

    List<Mapping> findByInstanceName(String instancename);
}
