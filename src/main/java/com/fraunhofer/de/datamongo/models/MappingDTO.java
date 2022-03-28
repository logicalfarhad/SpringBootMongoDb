package com.fraunhofer.de.datamongo.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MappingDTO {

    private String entity;
    private String type;

    private String delimiter;
    private String header;
    private MultipartFile file;
}
