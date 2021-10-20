package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.repositories.MappingMongoRepository;
import io.swagger.annotations.Api;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@Api(value = "Swagger2DemoRestController", description = "REST APIs related to Student Entity!!!!")
@RequestMapping("/api")
public class MappingController {

    private final MappingMongoRepository _mappingMongoRepository;

    @Autowired
    public MappingController(final MappingMongoRepository mappingMongoRepository) {
        this._mappingMongoRepository = mappingMongoRepository;
    }

    @GetMapping("/getAll")
    public ResponseEntity<List<Mapping>> getAllMapping() {
        var mappingList = _mappingMongoRepository.findAll();

        return new ResponseEntity<>(mappingList, HttpStatus.OK);
    }

    @GetMapping("/getById/{Id}")
    public ResponseEntity<Mapping> getMappingById(@PathVariable("Id") String Id) {
        var mapping = _mappingMongoRepository.findById(Id);
        return mapping.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping("/save")
    public ResponseEntity<Mapping> createMapping(@RequestBody Mapping mapping) {
        try {
            var _mapping = _mappingMongoRepository
                    .save(new Mapping(mapping.get_id(),
                            mapping.getEntity(),
                            mapping.getType(),
                            mapping.getSource(),
                            mapping.getKey(),
                            mapping.getOptions(),
                            mapping.getProlog(),
                            mapping.getPropertiesMap()));
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/editById/{Id}")
    public ResponseEntity<Mapping> updateMapping(@PathVariable("Id") String Id, @RequestBody Mapping mapping) {
        var mappingData = _mappingMongoRepository.findById(Id);

        if (mappingData.isPresent()) {
            var _mapping = mappingData.get();
            _mapping.setEntity(!Objects.equals(mapping.getEntity(), "") ? mapping.getEntity() : "");
            _mapping.setType(!Objects.equals(mapping.getType(), "") ? mapping.getType() : "");
            _mapping.setSource(!Objects.equals(mapping.getSource(), "") ? mapping.getSource() : "");
            _mapping.setKey(!Objects.equals(mapping.getKey(), "") ? mapping.getKey() : "");
            _mapping.setOptions(mapping.getOptions() == null ? null : mapping.getOptions());
            _mapping.setProlog(mapping.getProlog() == null ? null : mapping.getProlog());
            _mapping.setPropertiesMap(mapping.getPropertiesMap() == null ? null : mapping.getPropertiesMap());

            return new ResponseEntity<>(_mappingMongoRepository.save(_mapping), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/deleteAll")
    public ResponseEntity<HttpStatus> deleteAllMapping() {
        try {
            _mappingMongoRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/deleteById/{Id}")
    public ResponseEntity<HttpStatus> deleteMappingById(@PathVariable("Id") String Id) {
        try {
            var mapping = _mappingMongoRepository.findById(Id);
            if (mapping.isPresent()) {
                _mappingMongoRepository.deleteById(Id);
                return new ResponseEntity<>(HttpStatus.OK);
            }
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
