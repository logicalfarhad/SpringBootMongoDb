package com.fraunhofer.de.datamongo.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import com.fraunhofer.de.datamongo.exceptionHandlers.FileStorageException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileService {

    @Value("${upload.path}")
    private String uploadPath;

    @PostConstruct
    public void init() {
        try {
            Files.createDirectories(Paths.get(uploadPath));
        } catch (IOException e) {
            throw new RuntimeException("Could not create upload folder!");
        }
    }

    public void uploadFile(MultipartFile file) {
        try {
            Path root = Paths.get(uploadPath);
            if (!Files.exists(root)) {
                init();
            }
            Path copyLocation = Paths
                    .get(uploadPath + File.separator + StringUtils.cleanPath(file.getOriginalFilename()));
            Files.copy(file.getInputStream(), copyLocation, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException(
                    "Could not store file " + file.getOriginalFilename() + ". Please try again!");
        }
    }

    public void deleteFile(String fileName) {
        try {
            Path copyLocation = Paths
                    .get(uploadPath + File.separator + StringUtils.cleanPath(fileName));
            Files.delete(copyLocation);
        } catch (Exception e) {
            e.printStackTrace();
            throw new FileStorageException(
                    "Could not delete file " + fileName + ". Please try again!");
        }
    }

    public void deleteAllExistingFiles(File dir) {
        for (File file : dir.listFiles())
            if (!file.isDirectory())
                file.delete();
    }

    public List<Path> loadAll() {
        try {
            Path root = Paths.get(uploadPath);
            if (Files.exists(root)) {
                return Files.walk(root, 1).filter(path -> !path.equals(root)).collect(Collectors.toList());
            }

            return Collections.emptyList();
        } catch (IOException e) {
            throw new RuntimeException("Could not list the files!");
        }
    }

    public Resource load(String filename) {
        try {
            System.out.println(filename);
            Path file = Paths.get(uploadPath).resolve(filename);
            Resource resource = new UrlResource(file.toUri());

            if (resource.exists() || resource.isReadable()) {
                return resource;
            } else {
                throw new RuntimeException("Could not read the file!");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Error: " + e.getMessage());
        }
    }
}