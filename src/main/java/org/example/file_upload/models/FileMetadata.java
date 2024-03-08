package org.example.file_upload.models;


import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Data
public class FileMetadata {
    @Id
    private String fileId;
    private String fileName;
    private LocalDateTime createdAt;
    private long size;
    private String fileType;

//    @OneToOne(mappedBy = "fileMetadata")
//    private FileData fileData;

    // Constructors, getters, and setters
}
