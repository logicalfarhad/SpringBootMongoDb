package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.repositories.MappingMongoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(value = "/")
public class MappingController {

    private final MappingMongoRepository _mappingMongoRepository;

    @Autowired
    MappingController(MappingMongoRepository mappingMongoRepository) {
        this._mappingMongoRepository = mappingMongoRepository;
    }

    @GetMapping(value = "/getall")
    public ResponseEntity<List<Mapping>> getAllMapping() {
        var mappingList = _mappingMongoRepository.findAll();
        return new ResponseEntity<>(mappingList, HttpStatus.OK);
    }

    @GetMapping(value = "/{Id}")
    public ResponseEntity<Mapping> getMappingById(@PathVariable("Id") String Id) {
        var mapping = _mappingMongoRepository.findById(Id);
        return mapping.map(value -> new ResponseEntity<>(value, HttpStatus.OK))
                .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
    }

    @PostMapping(value = "/save")
    public ResponseEntity<Mapping> createMapping(@RequestBody Mapping mapping) {
        try {
            var _mapping = _mappingMongoRepository
                    .save(new Mapping(mapping.get_id(),
                            mapping.getEntity(),
                            mapping.getType(),
                            mapping.getSource(),
                            mapping.getID(),
                            mapping.getOptions(),
                            mapping.getProlog(),
                            mapping.getPropertiesMap()));
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping(value = "/{Id}")
    public ResponseEntity<Mapping> updateMapping(@PathVariable("Id") String Id, @RequestBody Mapping mapping) {
        var mappingData = _mappingMongoRepository.findById(Id);

        if (mappingData.isPresent()) {
            var _mapping = mappingData.get();
            _mapping.setEntity(mapping.getEntity());
            _mapping.setType(mapping.getType());
            _mapping.setSource(mapping.getSource());
            _mapping.setID(mapping.getID());
            _mapping.setOptions(mapping.getOptions());
            _mapping.setProlog(mapping.getProlog());
            _mapping.setPropertiesMap(mapping.getPropertiesMap());
            return new ResponseEntity<>(_mappingMongoRepository.save(_mapping), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<HttpStatus> deleteAllMapping() {
        try {
            _mappingMongoRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{Id}")
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
