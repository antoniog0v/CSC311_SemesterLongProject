package dao;

import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobClientBuilder;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;

public class StorageUploader {

    private String mediaFileUrl = ("https://villanicsc311storage.blob.core.windows.net/media-files/");
    private BlobContainerClient containerClient;
    private String ConnectionKey = ("DefaultEndpointsProtocol=https;AccountName=villanicsc311storage;AccountKey=X6ywaGmPMV9Lv7/DM1be5x7oIFQh8EY7v33xA9jykMgOsPekvKXh/zw407+5JA9KjiCPfEiqD+U8+AStxssyIQ==;EndpointSuffix=core.windows.net");

    public StorageUploader( ) {
        this.containerClient = new BlobContainerClientBuilder()
                .connectionString(ConnectionKey)
                .containerName("media-files")
                .buildClient();
    }

    public void uploadFile(String filePath, String blobName) {
        BlobClient blobClient = containerClient.getBlobClient(blobName);
        blobClient.uploadFromFile(filePath);
    }
    public String getImageURL(String s){
        String r = (mediaFileUrl+s);
        return r;
    }
    public BlobContainerClient getContainerClient(){
        return containerClient;

    }

}
