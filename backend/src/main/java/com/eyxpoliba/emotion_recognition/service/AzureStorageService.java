package com.eyxpoliba.emotion_recognition.service;


import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.azure.storage.blob.models.BlobItem;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AzureStorageService {

    @Value("${azure.storage.connection-string}")
    private String connectionString;

    @Value("${azure.storage.container-name}")
    private String containerName;

    @Value("${azure.storage.sas-token}")
    private String sasToken;

    private BlobContainerClient blobContainerClient;

    @PostConstruct
    public void init() {
        BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
                .endpoint(connectionString)
                .sasToken(sasToken)
                .buildClient();
        this.blobContainerClient = blobServiceClient.getBlobContainerClient(containerName);
        if (!blobContainerClient.exists()) {
            blobContainerClient.create(); // Crea il container se non esiste
        }
    }

    /**
     * Recupera i nomi di tutti i blob (immagini) nel container.
     *
     * @return Una lista di stringhe con i nomi dei blob.
     */
    private List<String> listAllBlobs() {
        return blobContainerClient.listBlobs().stream().map(BlobItem::getName).collect(Collectors.toList());
    }

    /**
     * Recupera un numero specificato di nomi di blob in modo casuale.
     *
     * @param count Il numero di immagini casuali da recuperare.
     * @return Una lista di stringhe con i nomi dei blob casuali.
     */
    public List<String> getRandomBlobNames(int count) {
        List<String> allBlobNames = listAllBlobs();
        if (allBlobNames.isEmpty()) {
            return Collections.emptyList();
        }

        // Se il numero di elementi richiesti è maggiore del totale, restituisci tutti gli elementi
        if (count >= allBlobNames.size()) {
            Collections.shuffle(allBlobNames); // Rimescola per ottenere una selezione casuale
            return allBlobNames;
        }

        // Rimescola la lista e prendi i primi 'count' elementi
        Collections.shuffle(allBlobNames);
        return allBlobNames.subList(0, count);
    }

    /**
     * Scarica il contenuto di un blob.
     * @param imageName Il nome del blob da scaricare.
     * @return Un array di byte contenente l'immagine.
     * @throws IOException Se si verifica un errore durante lo scaricamento.
     */
    public byte[] downloadImage(String imageName) throws IOException {
        BlobClient blobClient = blobContainerClient.getBlobClient(imageName);
        if (!blobClient.exists()) {
            throw new IOException("Blob non trovato: " + imageName);
        }
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        blobClient.downloadStream(outputStream);
        return outputStream.toByteArray();
    }
}