package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.Schema;
import com.fraunhofer.de.datamongo.models.Setting;
import com.fraunhofer.de.datamongo.models.VocolInfo;
import com.fraunhofer.de.datamongo.repositories.MappingMongoRepository;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@RestController
@Api(value = "/api")
@RequestMapping("/api")
public class MappingController {

    private final MappingMongoRepository _mappingMongoRepository;

    private final VocolInfo vocolInfo;

    @Autowired
    public MappingController(final MappingMongoRepository mappingMongoRepository) {
        this._mappingMongoRepository = mappingMongoRepository;
        this.vocolInfo = new VocolInfo();
    }

    @ApiOperation(value = "getAll", notes = "Get all the mapping configuration", nickname = "getGreeting")
    @GetMapping(value = "/getAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Mapping>> getAllMapping() {
        var mappingList = _mappingMongoRepository.findAll();

        return new ResponseEntity<>(mappingList, HttpStatus.OK);
    }

    @GetMapping(value = "/getById/{Id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Schema> getMappingById(@PathVariable("Id") String Id) {
        var _mapping = _mappingMongoRepository.findById(Id);

        if (_mapping.isPresent()) {
            var mapping = _mapping.get();
            var schema = Schema.SchemaBuilder()
                    ._id(mapping.get_id())
                    .entity(mapping.getEntity())
                    .type(mapping.getType())
                    .source(mapping.getSource())
                    .options(mapping.getOptions())
                    .settingList(mapping.getSettingList())
                    .build();
            BufferedReader reader;
            try {
                reader = new BufferedReader(new FileReader(mapping.getSource()));
                List<String> lines = new ArrayList<>();
                String line;
                while ((line = reader.readLine()) != null)
                    lines.add(line);
                reader.close();
                schema.setColumns(lines.get(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            return new ResponseEntity<>(schema, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/save")
    public ResponseEntity<Mapping> createMapping(@RequestBody Mapping mapping) {
        try {

            var _mapping = _mappingMongoRepository
                    .save(Mapping.builder().
                            _id(mapping.get_id())
                            .entity(mapping.getEntity())
                            .type(mapping.getType())
                            .source(mapping.getSource())
                            .options(mapping.getOptions())
                            .settingList(mapping.getSettingList())
                            .build());
            return new ResponseEntity<>(_mapping, HttpStatus.CREATED);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/editById/{Id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mapping> updateMapping(@PathVariable("Id") String Id, @RequestBody Mapping mapping) {
        var mappingData = _mappingMongoRepository.findById(Id);
        if (mappingData.isPresent()) {
            var _mapping = mappingData.get();
            _mapping.setEntity(mapping.getEntity());
            _mapping.setType(mapping.getType());
            _mapping.setSource(mapping.getSource());
            _mapping.setOptions(mapping.getOptions());
            _mapping.setSettingList(mapping.getSettingList());
            return new ResponseEntity<>(_mappingMongoRepository.save(_mapping), HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PostMapping(value = "/deleteAll", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<HttpStatus> deleteAllMapping() {
        try {
            _mappingMongoRepository.deleteAll();
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping(value = "/deleteById/{Id}", produces = MediaType.APPLICATION_JSON_VALUE)
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


    @GetMapping(value = "/saveMapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Boolean> setVocolBranch(@RequestParam("branchName") String branchName,
                                                  @RequestParam("instanceName") String instanceName) {
        if (!branchName.isEmpty() && !instanceName.isEmpty()) {
            vocolInfo.setBranchName(branchName);
            vocolInfo.setInstanceName(instanceName);
            return new ResponseEntity<>(true, HttpStatus.OK);
        }
        return new ResponseEntity<>(false, HttpStatus.NO_CONTENT);

    }

    @GetMapping(value = "/getMapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<VocolInfo> generateMapping() {

        var prologMap = new HashMap<>();
        final var rmltext = new StringBuilder();
        var mappingList = _mappingMongoRepository.findAll();

        AtomicBoolean isNull = new AtomicBoolean(false);

        mappingList.forEach(item -> {
            var settingList = item.getSettingList();
            if (settingList == null) {
                isNull.set(true);
            }
        });
        if (isNull.get()) {
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
        final List<String> csvList = new ArrayList<>();
        AtomicInteger counter = new AtomicInteger();

        int totalCount = 0;
        for (Mapping mapping : mappingList) {
            var settingList = mapping.getSettingList();
            for (Setting setting : settingList) {
                totalCount++;
                rmltext.append("\n\n<#").append("Mapping").append(totalCount).append("> a rr:TriplesMap;");
                rmltext.append("\n\trml:logicalSource [");
                rmltext.append("\n\t\trml:source \"").append(mapping.getSource()).append("\";");
                rmltext.append("\n\t\trml:referenceFormulation ql:").append(mapping.getType().toUpperCase());
                rmltext.append("\n\t];");
                rmltext.append("\n\trr:subjectMap [");
                rmltext.append("\n\t\trr:template \"http://example.com/{").append(setting.getKey()).append("}\";");
                rmltext.append("\n\t\trr:class ").append(setting.getKlass());
                rmltext.append("\n\t];\n");
                setting.getPropertiesList().forEach((key, value) -> {
                    counter.getAndIncrement();
                    rmltext.append("\n\trr:predicateObjectMap [");
                    rmltext.append("\n\t\trr:predicate ").append(key.replace('.', '_')).append(";");

                    rmltext.append("\n\t\trr:objectMap [rml:reference ")
                            .append("\"")
                            .append(value)
                            .append("\"")
                            .append("]");
                    rmltext.append(counter.get() < setting.getPropertiesList().size() ? "\n\t];\n" : "\n\t].\n");


                });
                counter.set(0);
                prologMap.putAll(setting.getPrologList());
                prologMap.put("base", "http://example.com/ns#");
            }
            csvList.add(mapping.getSource());
        }


        prologMap.forEach((key, val) -> {
            if (key == "base")
                rmltext.insert(0, "@" + key + " <" + val + ">.\n");
            else {
                rmltext.insert(0, "@prefix " + key + ": <" + val + ">.\n");
            }
        });
        vocolInfo.setCsvFileList(csvList);
        vocolInfo.setRmlText(rmltext.toString());
        return new ResponseEntity<>(vocolInfo, HttpStatus.CREATED);
    }
}
