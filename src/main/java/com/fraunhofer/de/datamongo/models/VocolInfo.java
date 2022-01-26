package com.fraunhofer.de.datamongo.models;

import lombok.Data;
import java.util.List;

@Data
public class VocolInfo {
    private List<String> csvFileList;
    private String rmlText;
    private String branchName;
    private String instanceName;
    private String username;
    private String secretKey;
}