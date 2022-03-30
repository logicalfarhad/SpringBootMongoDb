package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.*;
import com.fraunhofer.de.datamongo.services.FileService;
import com.fraunhofer.de.datamongo.services.IMappingService;
import org.apache.http.entity.ContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
//@Api(value = "/api")
@RequestMapping("/api")
public class MappingController {

    private final IMappingService mappingService;
    private final FileService fileService;

    @Autowired
    public MappingController(final IMappingService mappingService, final FileService _fileService) {
        this.mappingService = mappingService;
        this.fileService = _fileService;
    }

    //@ApiOperation(value = "getAll", notes = "Get all the mapping configuration", nickname = "getGreeting")
    @GetMapping(value = "/getAllMappingByInstanceName")
    public ResponseEntity<List<Mapping>> getAllMapping(@RequestParam String instanceName) {
        var mappingList = mappingService.getAllMappingByInstanceName
                (instanceName);
        return new ResponseEntity<>(mappingList, HttpStatus.OK);
    }

    @GetMapping(value = "/getById")
    public ResponseEntity<Schema> getMappingById(@RequestParam("Id") String Id) {
        var schema = mappingService.getSchemaById(Id);
        if (schema != null)
            return new ResponseEntity<>(schema, HttpStatus.OK);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/saveSource", consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Mapping> createMapping(@ModelAttribute MappingDTO mappingDTO
    ) {
        try {
            var _mapping = new Mapping();
            _mapping.setEntity(mappingDTO.getEntity());
            _mapping.setType(mappingDTO.getType());
            _mapping.setInstanceName(mappingDTO.getInstanceName());
            var option = new Option();

            option.setDelimiter(mappingDTO.getDelimiter());
            option.setHeader(mappingDTO.getHeader());

            _mapping.setOptions(option);
            String fileName = mappingDTO.getFile().getOriginalFilename();
            _mapping.setSource(fileName);

            fileService.uploadSourceFile(mappingDTO.getFile());
            mappingService.save(_mapping);
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/updateSource",consumes = { MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Mapping> updateSource(@RequestParam String Id, @ModelAttribute MappingDTO mappingDTO) {
        try {
          var _mapping =   mappingService.update(Id,mappingDTO);
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/deleteAll")
    public ResponseEntity<HttpStatus> deleteAllMapping() {
        try {
            mappingService.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/deleteById")
    public ResponseEntity<HttpStatus> deleteMappingById(@RequestParam("Id") String Id) {
        try {
            mappingService.deleteById(Id);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/setVocolInfo")
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

    @PostMapping(value = "/setMappingSettings")
    public ResponseEntity<Boolean> setMappingSettings(@RequestBody Mapping mapping) {
        mappingService.setMappingSettings(mapping);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }

    @GetMapping("/getVocolInfo")
    public ResponseEntity<VocolInfo> getVocolInfo() {
        var vocolInfo = mappingService.getVocolInfo();
        if (vocolInfo.getInstanceName().isEmpty())
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(vocolInfo, HttpStatus.OK);
    }
}
