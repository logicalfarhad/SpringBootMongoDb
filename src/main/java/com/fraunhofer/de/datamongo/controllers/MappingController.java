package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.Schema;
import com.fraunhofer.de.datamongo.models.VocolInfo;
import com.fraunhofer.de.datamongo.services.IMappingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@Api(value = "/api")
@RequestMapping("/api")
public class MappingController {

    private final IMappingService mappingService;

    @Autowired
    public MappingController(final IMappingService mappingService) {
        this.mappingService = mappingService;
    }

    @ApiOperation(value = "getAll", notes = "Get all the mapping configuration", nickname = "getGreeting")
    @GetMapping(value = "/getAll")
    public ResponseEntity<List<Mapping>> getAllMapping() {
        var mappingList = mappingService.getAllMapping();
        return new ResponseEntity<>(mappingList, HttpStatus.OK);
    }

    @GetMapping(value = "/getById/{Id}")
    public ResponseEntity<Schema> getMappingById(@PathVariable("Id") String Id) {
        var schema = mappingService.getSchemaById(Id);
        if (schema != null)
            return new ResponseEntity<>(schema, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/save")
    public ResponseEntity<Mapping> createMapping(Mapping mapping) {
        try {
            var _mapping = mappingService.save(mapping);
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/editById/{Id}")
    public ResponseEntity<Mapping> updateMapping(@PathVariable("Id") String Id, @RequestBody Mapping mapping) {
        var _mapping = mappingService.update(Id, mapping);
        try {
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/deleteAll")
    public ResponseEntity<HttpStatus> deleteAllMapping() {
        try {
            mappingService.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/deleteById/{Id}")
    public ResponseEntity<HttpStatus> deleteMappingById(@PathVariable("Id") String Id) {
        try {
            mappingService.deleteById(Id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/setVocolInfo")
    public ResponseEntity<Boolean> setVocolBranch(@RequestBody VocolInfo vocolInfo) {
        mappingService.setVocolInfo(vocolInfo);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/getMapping")
    public ResponseEntity<VocolInfo> generateMapping() {
        var vocolInfo = mappingService.generateMapping();
        if (vocolInfo == null)
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(vocolInfo, HttpStatus.OK);
    }
}
