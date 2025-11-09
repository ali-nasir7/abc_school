package com.backend.Abroad_School.service;

import com.backend.Abroad_School.model.Document;
import com.backend.Abroad_School.model.Student;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    Document storeFile(MultipartFile file, Student student , Document.DocumentType documentType);
}
