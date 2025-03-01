package com.fraunhofer.de.datamongo.services;


import com.fraunhofer.de.datamongo.models.*;
import com.fraunhofer.de.datamongo.repositories.MappingRepository;
import lombok.SneakyThrows;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class MappingService implements IMappingService {

    private final MappingRepository mappingRepository;

    @Value("${source.path}")
    private String sourcePath;

    private final VocolInfo vocolInfo;
    private final FileService fileService;
    @Autowired
    public MappingService(final MappingRepository mappingRepository,final FileService _fileService) {
        this.mappingRepository = mappingRepository;
        this.fileService = _fileService;
        this.vocolInfo = new VocolInfo();
    }

    @Override
    public List<Mapping> getAllMappingByInstanceName(String instanceName) {
        return mappingRepository.findByInstanceName(instanceName);
    }

    @Override
    public void deleteById(String Id){
        var _mapping = getMappingById(Id);
        if(_mapping!=null){
            mappingRepository.deleteById(Id);
        }
    }

    @Override
    public Mapping update(String Id, MappingDTO mappingDTO) {
        var _mapping = getMappingById(Id);
        _mapping.setEntity(mappingDTO.getEntity());
        _mapping.setType(mappingDTO.getType());
        _mapping.setInstanceName(mappingDTO.getInstanceName());
        _mapping.setSettingList(null);
        _mapping.setOptions(Option.builder()
                .delimiter(mappingDTO.getDelimiter())
                .header(mappingDTO.getHeader())
                .build());
        String fileName = mappingDTO.getFile().getOriginalFilename();
        _mapping.setSource(fileName);
        fileService.uploadSourceFile(mappingDTO.getFile());
        return mappingRepository.save(_mapping);
    }

    @Override
    public Mapping save(Mapping mapping) {
        return mappingRepository
                .save(Mapping.builder().
                        _id(mapping.get_id())
                        .entity(mapping.getEntity())
                        .type(mapping.getType())
                        .instanceName(mapping.getInstanceName())
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


    @SneakyThrows
    @Override
    public Schema getSchemaById(String Id) {
        var mapping = getMappingById(Id);

        var fileName = mapping.getSource();
        var schema = Schema.SchemaBuilder()
                ._id(mapping.get_id())
                .entity(mapping.getEntity())
                .type(mapping.getType())
                .instanceName(mapping.getInstanceName())
                .source(mapping.getSource())
                .options(mapping.getOptions())
                .settingList(mapping.getSettingList())
                .build();

        try (InputStream inputStream = fileService.getFileByName(fileName).getInputStream()) {

            try(BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {

                List<String> lines = new ArrayList<>();
                String line;
                while ((line = bufferedReader.readLine()) != null)
                    lines.add(line);
                bufferedReader.close();
                schema.setColumns(lines.get(0));
            }
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
        var mappingList = getAllMappingByInstanceName(vocolInfo.getInstanceName());

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
            String changedSource = sourcePath +File.separator+mapping.getSource();
            for (Setting setting : settingList) {

                totalCount++;
                rmltext.append("\n\n<#").append("Mapping").append(totalCount).append("> a rr:TriplesMap;");
                rmltext.append("\n\trml:logicalSource [");
                rmltext.append("\n\t\trml:source \"").append(changedSource).append("\";");
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
    public void setVocolInfo(VocolInfo _vocolInfo) {
        if (_vocolInfo.getBranchName() != null)
            vocolInfo.setBranchName(_vocolInfo.getBranchName());
        if (_vocolInfo.getInstanceName() != null)
            vocolInfo.setInstanceName(_vocolInfo.getInstanceName());
        if (_vocolInfo.getCsvFileList() != null)
            vocolInfo.setCsvFileList(_vocolInfo.getCsvFileList());
        if (_vocolInfo.getUsername() != null)
            vocolInfo.setUsername(_vocolInfo.getUsername());
        if (_vocolInfo.getSecretKey() != null)
            vocolInfo.setSecretKey(_vocolInfo.getSecretKey());
    }

    @Override
    public VocolInfo getVocolInfo() {
        return vocolInfo;
    }

    @Override
    public Mapping setMappingSettings(Mapping mapping) {
        var _mapping = getMappingById(mapping.get_id());
        _mapping.setSettingList(mapping.getSettingList());
       return mappingRepository.save(_mapping);
    }
}