package com.fraunhofer.de.datamongo.repositories;

import com.fraunhofer.de.datamongo.models.Mapping;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MappingMongoRepository extends MongoRepository<Mapping, String> {
}
