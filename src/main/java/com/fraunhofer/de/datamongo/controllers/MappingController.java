package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.Repo;
import com.fraunhofer.de.datamongo.models.Schema;
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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

@RestController
@Api(value = "/api")
@RequestMapping("/api")
public class MappingController {

    private final MappingMongoRepository _mappingMongoRepository;

    private final Repo repo;

    @Autowired
    public MappingController(final MappingMongoRepository mappingMongoRepository) {
        this._mappingMongoRepository = mappingMongoRepository;
        this.repo = new Repo();
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
            var schema = new Schema();
            schema.setType(mapping.getType());
            schema.setEntity(mapping.getEntity());
            schema.setOptions(mapping.getOptions());
            schema.setClas(mapping.getClas());
            schema.setPropertiesMap(mapping.getPropertiesMap());
            schema.setProlog(mapping.getProlog());
            schema.setKey(mapping.getKey());
            schema.setSource(mapping.getSource());
            schema.set_id(mapping.get_id());

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

    @PostMapping(value = "/save", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Mapping> createMapping(@RequestBody Mapping mapping) {
        try {
            var _mapping = _mappingMongoRepository
                    .save(new Mapping(mapping.get_id(),
                            mapping.getEntity(),
                            mapping.getType(),
                            mapping.getSource(),
                            mapping.getKey(),
                            mapping.getOptions(),
                            mapping.getClas(),
                            mapping.getProlog(),
                            mapping.getPropertiesMap()));
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
            _mapping.setEntity(!Objects.equals(mapping.getEntity(), "") ? mapping.getEntity() : "");
            _mapping.setType(!Objects.equals(mapping.getType(), "") ? mapping.getType() : "");
            _mapping.setSource(!Objects.equals(mapping.getSource(), "") ? mapping.getSource() : "");
            _mapping.setKey(!Objects.equals(mapping.getKey(), "") ? mapping.getKey() : "");
            _mapping.setOptions(mapping.getOptions() == null ? null : mapping.getOptions());
            _mapping.setClas(mapping.getClas() == null ? null : mapping.getClas());
            _mapping.setProlog(mapping.getProlog() == null ? null : mapping.getProlog());
            _mapping.setPropertiesMap(mapping.getPropertiesMap() == null ? null : mapping.getPropertiesMap());

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
            repo.setBranchName(branchName);
            repo.setInstanceName(instanceName);
            return new ResponseEntity<>(true,HttpStatus.OK);
        }
        return new ResponseEntity<>(false,HttpStatus.NO_CONTENT);

    }
    @GetMapping(value = "/getMapping", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Repo> generateMapping() {

        final var rmltext = new StringBuilder();
        var mappingList = _mappingMongoRepository.findAll();

        AtomicBoolean isNull = new AtomicBoolean(false);

        mappingList.forEach(item -> {
            var propertiesMap = item.getPropertiesMap();
            if (propertiesMap == null) {
                isNull.set(true);
            }
        });
        if (isNull.get()) {
            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
        }
        final List<String> csvList = new ArrayList<>();

        AtomicInteger counter = new AtomicInteger();
        IntStream.range(0, mappingList.size()).forEachOrdered(i -> {
            rmltext.append("\n\n<#").append(mappingList.get(i).getEntity()).append("Mapping> a rr:TriplesMap;");
            rmltext.append("\n\trml:logicalSource [");
            rmltext.append("\n\t\trml:source \"").append(mappingList.get(i).getSource()).append("\";");
            rmltext.append("\n\t\trml:referenceFormulation ql:").append(mappingList.get(i).getType().toUpperCase());
            rmltext.append("\n\t];");
            rmltext.append("\n\trr:subjectMap [");
            rmltext.append("\n\t\trr:template \"http://example.com/{").append(mappingList.get(i).getKey()).append("}\";");
            if (!Objects.equals(mappingList.get(i).getClas(), "")) {
                rmltext.append("\n\t\trr:class ");
                rmltext.append(mappingList.get(i).getClas());
            } else {
                rmltext.append("\n\t\trr:class ()");
            }
            rmltext.append("\n\t];\n");
            mappingList.get(i).getPropertiesMap().forEach((key, value) -> {
                counter.getAndIncrement();
                rmltext.append("\n\trr:predicateObjectMap [");
                rmltext.append("\n\t\trr:predicate ").append(key.replace('.', '_')).append(";");
                if (counter.get() < mappingList.get(i).getPropertiesMap().size()) {
                    rmltext.append("\n\t\trr:objectMap [rml:reference ")
                            .append("\"")
                            .append(value)
                            .append("\"")
                            .append("]];");
                } else {
                    rmltext.append("\n\t\trr:objectMap [rml:reference ")
                            .append("\"")
                            .append(value)
                            .append("\"")
                            .append("]].");
                }
            });
            mappingList.get(i).getProlog()
                    .forEach((key, value)
                            -> rmltext.insert(0, "@prefix " + key + ": <" + value + ">.\n"));

            if (mappingList.get(i).getProlog().size() == 1) {
                rmltext.insert(0, "@base <http://example.com/ns#>.\n");
            } else if (i < mappingList.get(i).getProlog().size() - 1) {
                rmltext.insert(0, "@base <http://example.com/ns#>.\n");
            }
            csvList.add(mappingList.get(i).getSource());
        });
        repo.setCsvFileList(csvList);
        repo.setRmlText(rmltext.toString());
        return new ResponseEntity<>(repo, HttpStatus.CREATED);
    }
}
