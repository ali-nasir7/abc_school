package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Document;
// import com.backend.Abroad_School.model.Document.DocumentType;
import com.backend.Abroad_School.model.Student;
import com.backend.Abroad_School.repository.DocumentRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class FileStorageServiceImpl implements FileStorageService {

    private final DocumentRepository documentRepository;
    private final Path fileStorageLocation;

    public FileStorageServiceImpl(DocumentRepository documentRepository,
                                  @Value("${file.upload-dir}") String uploadDir) {
        this.documentRepository = documentRepository;
        this.fileStorageLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.fileStorageLocation);
        } catch (Exception ex) {
            throw new RuntimeException("Could not create upload directory", ex);
        }
    }

    @Override
    public Document storeFile(MultipartFile file, Student student , Document.DocumentType documentType) {
        String original = StringUtils.cleanPath(file.getOriginalFilename());
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String fileName = timestamp + "_" + original;

        try {
            if (original.contains("..")) {
                throw new RuntimeException("Invalid path sequence in file name: " + original);
            }

            Path target = this.fileStorageLocation.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            Document document = Document.builder()
                    .fileName(original)
                    .filePath(target.toString())
                    .documentType(documentType)
                    .student(student)
                    .build();

            return documentRepository.save(document);

        } catch (IOException e) {
            throw new RuntimeException("Could not store file " + original, e);
        }
    }

  
}
