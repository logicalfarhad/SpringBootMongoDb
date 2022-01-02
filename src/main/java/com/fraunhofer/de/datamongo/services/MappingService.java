package com.fraunhofer.de.datamongo.services;


import com.fraunhofer.de.datamongo.models.Mapping;
import com.fraunhofer.de.datamongo.models.Schema;
import com.fraunhofer.de.datamongo.models.Setting;
import com.fraunhofer.de.datamongo.models.VocolInfo;
import com.fraunhofer.de.datamongo.repositories.MappingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MappingService implements IMappingService {

    private final MappingRepository mappingRepository;

    private final VocolInfo vocolInfo;

    @Autowired
    public MappingService(final MappingRepository mappingRepository) {
        this.mappingRepository = mappingRepository;
        this.vocolInfo = new VocolInfo();
    }

    @Override
    public List<Mapping> getAllMapping() {
        return mappingRepository.findAll();
    }

    @Override
    public void deleteById(String Id){
        var _mapping = getMappingById(Id);
        if(_mapping!=null){
            mappingRepository.deleteById(Id);
        }
    }

    @Override
    public Mapping update(String Id, Mapping mapping) {
        var _mapping = getMappingById(Id);
        _mapping.setEntity(mapping.getEntity());
        _mapping.setType(mapping.getType());
        _mapping.setSource(mapping.getSource());
        _mapping.setOptions(mapping.getOptions());
        _mapping.setSettingList(mapping.getSettingList());
        return save(_mapping);
    }

    @Override
    public Mapping save(Mapping mapping) {
        return mappingRepository
                .save(Mapping.builder().
                        _id(mapping.get_id())
                        .entity(mapping.getEntity())
                        .type(mapping.getType())
                        .source(mapping.getSource())
                        .options(mapping.getOptions())
                        .settingList(mapping.getSettingList())
                        .build());
    }

    @Override
    public Mapping getMappingById(String Id) {
        var _mapping = this.mappingRepository.findById(Id);
        return _mapping.orElse(null);
    }


    @Override
    public Schema getSchemaById(String Id) {
        var mapping = getMappingById(Id);
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
        return schema;
    }

    @Override
    public void deleteAll() {
        mappingRepository.deleteAll();
    }

    @Override
    public VocolInfo generateMapping() {
        var prologMap = new HashMap<>();
        final var rmltext = new StringBuilder();
        var mappingList = getAllMapping();

        AtomicBoolean isNull = new AtomicBoolean(false);

        mappingList.forEach(item -> {
            var settingList = item.getSettingList();
            if (settingList == null) {
                isNull.set(true);
            }
        });
        if(isNull.get()){
            return null;
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
        return vocolInfo;
    }

    @Override
    public void setVocolInfo(String branchName, String instanceName) {
        vocolInfo.setBranchName(branchName);
        vocolInfo.setInstanceName(instanceName);
    }
}