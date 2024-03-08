package org.example.file_upload.controller;


import org.example.file_upload.models.FileMetadata;
import org.example.file_upload.service.FileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/files")
public class FileController {
    @Autowired
    private FileService fileService;

    @PostMapping("/upload")
    public ResponseEntity<String> uploadFile(@RequestParam("file") MultipartFile file,
                                             @RequestParam("fileName") String fileName,
                                             @RequestParam(value = "fileType", required = false) String fileType) {
        try {
            String fileId = fileService.uploadFile(file.getBytes(), fileName, fileType);
            return ResponseEntity.ok(fileId);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error uploading file");
        }
    }

    @GetMapping("/{fileId}")
    public ResponseEntity<byte[]> readFile(@PathVariable String fileId) {
        byte[] fileData = fileService.readFile(fileId);

        if (fileData != null) {
            return ResponseEntity.ok().body(fileData);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    @PutMapping("/{fileId}")
    public ResponseEntity<?> updateFile(@PathVariable String fileId,
                                        @RequestParam(value = "file", required = false) MultipartFile newFile,
                                        @RequestBody(required = false) FileMetadata newMetadata) {
        try {
            byte[] newFileData = (newFile != null) ? newFile.getBytes() : null;
            FileMetadata updatedMetadata = fileService.updateFile(fileId, newFileData, newMetadata);
            if(newMetadata == null ) {
                return ResponseEntity.ok("File Updated Successfully");
            }
            if (updatedMetadata != null) {
                return ResponseEntity.ok(updatedMetadata);
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("File not found");
            }
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error updating file");
        }
    }

    @DeleteMapping("/{fileId}")
    public ResponseEntity<String> deleteFile(@PathVariable String fileId) {
        String result = fileService.deleteFile(fileId);
        if (result.equals("File deleted successfully")) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(result);
        }
    }

    @GetMapping
    public ResponseEntity<List<FileMetadata>> listFiles() {
        List<FileMetadata> files = fileService.listFiles();
        return ResponseEntity.ok(files);
    }
}

