package org.example.file_upload.service;

import org.example.file_upload.models.FileMetadata;

import java.util.List;

public interface FileService {
    String uploadFile(byte[] fileData, String fileName, String fileType);

    byte[] readFile(String fileId);

    FileMetadata updateFile(String fileId, byte[] newFileData, FileMetadata newMetadata);

    String deleteFile(String fileId);

    List<FileMetadata> listFiles();
}