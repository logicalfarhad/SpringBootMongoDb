package com.fraunhofer.de.datamongo.controllers;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.RDFDataMgr;

@RestController
@RequestMapping("/api/ontology")
public class OntologyController {
    @GetMapping("/getDefault")
    public Object getOntology() {        
        final String uri = "https://lov.linkeddata.es/dataset/lov/api/v2/term/search?q=Person&type=class";
        RestTemplate restTemplate = new RestTemplate();
        Object result = restTemplate.getForObject(uri, Object.class);
        //System.out.println(result);
        Model model = RDFDataMgr.loadModel("/home/mrashikh/Workspace/rami.ttl") ;
        model.write(System.out, "RDF/JSON");
        return result;
    }
}
