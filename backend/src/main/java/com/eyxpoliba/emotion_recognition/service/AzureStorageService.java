package com.eyxpoliba.emotion_recognition.service;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class AzureStorageService {
    @Value("${azure.storage.connection-string}")
    private String connString;

    public String test() {
        BlobContainerClient container = new BlobContainerClientBuilder()
                .connectionString(connString)
                .containerName("images")
                .buildClient();

        System.out.println(container.toString());

        return "ok";
    }


    public List<String> getImagesForUserSession() {
        return new ArrayList<>(); // TODO
    }
}
