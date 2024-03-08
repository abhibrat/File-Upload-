package org.example.file_upload.service;

import org.example.file_upload.config.AmazonS3Config;
import org.example.file_upload.models.FileMetadata;
import org.example.file_upload.persistence.FileMetadataRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectResponse;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectResponse;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class FileServiceImpl implements FileService {
    @Autowired
    private FileMetadataRepository fileMetadataRepository;

    @Autowired
    private AmazonS3Config amazonS3Config;

    @Autowired
    private S3Client s3Client;

    @Override
    public String uploadFile(byte[] fileData, String fileName, String fileType) {
        String fileId = UUID.randomUUID().toString();
        LocalDateTime createdAt = LocalDateTime.now();
        long size = fileData.length;

        FileMetadata fileMetadata = new FileMetadata(fileId, fileName, createdAt, size, fileType);

        // Save file metadata to H2
        fileMetadataRepository.save(fileMetadata);

        // Save file data to S3
        String bucketName = amazonS3Config.getBucketName();
        String s3Key = fileId;

        PutObjectResponse putObjectResponse = s3Client.putObject(builder ->
                builder.bucket(bucketName)
                        .key(s3Key)
                        .build(), RequestBody.fromBytes(fileData));

        // Ensure successful S3 upload
        if (putObjectResponse.sdkHttpResponse().isSuccessful()) {
            return fileId;
        } else {
            // Handle upload failure
            throw new RuntimeException("Failed to upload file to S3");
        }
    }

    @Override
    public byte[] readFile(String fileId) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
//            FileData fileData = optionalFileMetadata.get().getFileData();

//            if (fileData != null) {
                String bucketName = amazonS3Config.getBucketName();
                String s3Key = fileId;

                // Retrieve file data from S3
                ResponseInputStream<GetObjectResponse> s3Object = s3Client.getObject(builder ->
                        builder.bucket(bucketName)
                                .key(s3Key)
                                .build());

                try {
                    return s3Object.readAllBytes();
                } catch (IOException e) {
                    // Handle exception
                    throw new RuntimeException("Failed to read file data from S3", e);
                }
//            }
        }

        return null; // File not found
    }

    @Override
    public FileMetadata updateFile(String fileId, byte[] newFileData, FileMetadata newMetadata) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
            FileMetadata existingMetadata = optionalFileMetadata.get();

            // Update metadata fields if newMetadata is not null
            if (newMetadata != null) {
                existingMetadata.setFileName(newMetadata.getFileName());
                existingMetadata.setFileType(newMetadata.getFileType());
                // Update other metadata fields as needed
            }

            fileMetadataRepository.save(existingMetadata); // Save updated metadata to H2

            // Save new file binary data to S3 if provided
            if (newFileData != null && newFileData.length > 0) {
                String bucketName = amazonS3Config.getBucketName();
                String s3Key = fileId;

                PutObjectResponse putObjectResponse = s3Client.putObject(builder ->
                        builder.bucket(bucketName)
                                .key(s3Key)
                                .build(), RequestBody.fromBytes(newFileData));

                // Ensure successful S3 upload
                if (!putObjectResponse.sdkHttpResponse().isSuccessful()) {
                    // Handle upload failure
                    throw new RuntimeException("Failed to upload updated file data to S3");
                }
            }

            return existingMetadata;
        }

        return null; // File not found
    }

    @Override
    public String deleteFile(String fileId) {
        Optional<FileMetadata> optionalFileMetadata = fileMetadataRepository.findById(fileId);

        if (optionalFileMetadata.isPresent()) {
            FileMetadata deletedFile = optionalFileMetadata.get();
            fileMetadataRepository.delete(deletedFile); // Delete from H2

            // Delete file data from S3
            String bucketName = amazonS3Config.getBucketName();
            String s3Key = fileId;

            DeleteObjectResponse deleteObjectResponse = s3Client.deleteObject(builder ->
                    builder.bucket(bucketName)
                            .key(s3Key)
                            .build());

            if (!deleteObjectResponse.sdkHttpResponse().isSuccessful()) {
                // Handle delete failure
                throw new RuntimeException("Failed to delete file data in S3");
            }
            return "File deleted successfully";
        }

        return "File not found";
    }

    @Override
    public List<FileMetadata> listFiles() {
        return fileMetadataRepository.findAll();
    }
}
