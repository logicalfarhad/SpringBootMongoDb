package com.fraunhofer.de.datamongo.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.MvcUriComponentsBuilder;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import com.fraunhofer.de.datamongo.models.OntologyFileData;
import com.fraunhofer.de.datamongo.services.FileService;

import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.riot.RDFDataMgr;

@RestController
@RequestMapping("/api/ontology")
public class OntologyController {
    @Autowired
    FileService fileService;

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
    public ResponseEntity<List<OntClass>>  getOntology() {
        final String uri = "https://lov.linkeddata.es/dataset/lov/api/v2/term/search?q=Person&type=class";
        RestTemplate restTemplate = new RestTemplate();
        Object result = restTemplate.getForObject(uri, Object.class);

        String path = "/home/mrashikh/Workspace/rami.ttl";
        OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_DL_MEM);
        model.read(path);
        // model.write(System.out);
        List<OntClass> object = model.listClasses().toList();

        return ResponseEntity.status(HttpStatus.OK).body(object);
    }

    @PostMapping("/uploadFile")
    public String uploadFile(@RequestParam("file") MultipartFile file, RedirectAttributes redirectAttributes) {

        fileService.uploadFile(file);

        redirectAttributes.addFlashAttribute("message",
                "You successfully uploaded " + file.getOriginalFilename() + "!");

        return "redirect:/";
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
