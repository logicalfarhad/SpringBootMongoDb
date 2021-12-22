package com.fraunhofer.de.datamongo.controllers;

import com.fraunhofer.de.datamongo.models.LocalOntology;
import com.fraunhofer.de.datamongo.models.OntologyFileData;
import com.fraunhofer.de.datamongo.services.FileService;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/ontology")
public class OntologyController {
    private final FileService fileService;

    @Value("${upload.path}")
    private String uploadPath;

    @Autowired
    public OntologyController(FileService fileService) {
        this.fileService = fileService;
    }

    /*
     * @GetMapping("/getDefault") public Object getOntology() { final String uri =
     * "https://lov.linkeddata.es/dataset/lov/api/v2/term/search?q=Person&type=class";
     * RestTemplate restTemplate = new RestTemplate(); Object result =
     * restTemplate.getForObject(uri, Object.class); //System.out.println(result);
     * Model model = RDFDataMgr.loadModel("/home/mrashikh/Workspace/rami.ttl") ;
     * Object objects = model.listObjects(); for (Object object :
     * model.listObjects().toList()) { System.out.println(object); }
     * //model.write(System.out, "RDF/JSON");
     * 
     * return result; }
     */

    @GetMapping("/getDefault")
    public ResponseEntity<List<String>> getDefault() {
        List<String> prefixedName = new ArrayList<String>();
        File folder = new File(uploadPath);
        String fileName = folder.listFiles()[0].getName();
        Path resouceLocation = Paths.get(uploadPath + File.separator + fileName);
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(resouceLocation.toString());
        List<OntClass> classes = model.listClasses().toList();
        for (OntClass item : classes) {
            prefixedName.add(item.getLocalName());
        }

        return ResponseEntity.status(HttpStatus.OK).body(prefixedName);
    }

    @GetMapping("/search")
    public ResponseEntity<List<LocalOntology>> search(@RequestParam String query, @RequestParam String objectType) {
        List<LocalOntology> localOntologiesData = new ArrayList<LocalOntology>();
        File folder = new File(uploadPath);
        for (int i = 0; i < folder.listFiles().length; i++) {
            String fileName = folder.listFiles()[i].getName();
            Path resouceLocation = Paths.get(uploadPath + File.separator + fileName);
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
            model.read(resouceLocation.toString());
            if (Objects.equals("class", objectType)) {
                var classes = model.listClasses().toList();
                for (var item : classes) {
                    if (item.getLocalName() != null && item.getLocalName().contains(query)) {
                        var ontology = new LocalOntology();
                        ontology.setName(item.getLocalName());
                        ontology.setUri(item.getURI());
                        boolean alreadyExists = localOntologiesData.stream()
                                .anyMatch(x -> x.getName().equals(item.getLocalName()));
                        if (!alreadyExists) {
                            localOntologiesData.add(ontology);
                        }
                    }
                }
            } else {
                var properties = model.listOntProperties().toList();
                for (var item : properties) {
                    if (item.getLocalName().contains(query)) {
                        var ontology = new LocalOntology();
                        ontology.setName(item.getLocalName());
                        ontology.setUri(item.getURI());
                        boolean alreadyExists = localOntologiesData.stream()
                                .anyMatch(x -> x.getName().equals(item.getLocalName()));
                        if (!alreadyExists) {
                            localOntologiesData.add(ontology);
                        }
                    }
                }
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(localOntologiesData);
    }

    @GetMapping("/getOntology")
    public ResponseEntity<List<LocalOntology>> getOntology() {
        List<LocalOntology> localOntologiesData = new ArrayList<LocalOntology>();
        File folder = new File(uploadPath);
        for (int i = 0; i < folder.listFiles().length; i++) {
            String fileName = folder.listFiles()[i].getName();
            Path resouceLocation = Paths.get(uploadPath + File.separator + fileName);
            OntModel model = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_RDFS_INF);
            model.read(resouceLocation.toString());
            var classes = model.listClasses().toList();
            for (var item : classes) {
                var ontology = new LocalOntology();
                ontology.setName(item.getLocalName());
                ontology.setUri(item.getURI());
                for(var prop: item.listDeclaredProperties().toList()){
                    var property = new LocalOntology();
                    property.setName(prop.getLocalName());
                    property.setUri(prop.getURI());
                    ontology.addProperties(property);             
                }
                boolean alreadyExists = localOntologiesData.stream()
                                .anyMatch(x -> x.getName().equals(item.getLocalName()));                
                if (!alreadyExists){
                    localOntologiesData.add(ontology);
                }
                
            }
        }

        return ResponseEntity.status(HttpStatus.OK).body(localOntologiesData);
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {
        fileService.uploadFile(file);
        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");
        return "redirect:/";
    }

    @PostMapping("/deleteFile")
    public ResponseEntity<String> deleteFile(@RequestBody String fileName) {
        fileService.deleteFile(fileName);
        return ResponseEntity.status(HttpStatus.OK).body(fileName + " deleted successfully.");
    }

    @GetMapping("/getListFiles")
    public ResponseEntity<List<OntologyFileData>> getListFiles() {
        List<OntologyFileData> fileInfos = fileService.loadAll().stream().map(this::pathToFileData)
                .collect(Collectors.toList());
        return ResponseEntity.status(HttpStatus.OK).body(fileInfos);
    }

    private OntologyFileData pathToFileData(Path path) {
        OntologyFileData fileData = new OntologyFileData();
        String filename = path.getFileName().toString();
        fileData.setFilename(filename);
        fileData.setUrl(MvcUriComponentsBuilder.fromMethodName(OntologyController.class, "getFile", filename).build()
                .toString());
        try {
            fileData.setSize(Files.size(path));
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Error: " + e.getMessage());
        }
        return fileData;
    }

    @GetMapping("{filename:.+}")
    @ResponseBody
    public ResponseEntity<Resource> getFile(@PathVariable String filename) {
        Resource file = fileService.load(filename);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + file.getFilename() + "\"")
                .body(file);
    }
}
