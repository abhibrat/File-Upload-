package org.example.file_upload.persistence;

import org.example.file_upload.models.FileMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FileMetadataRepository extends JpaRepository<FileMetadata, String> {
}