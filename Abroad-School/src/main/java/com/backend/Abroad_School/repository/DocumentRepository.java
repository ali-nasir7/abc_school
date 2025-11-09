package com.backend.Abroad_School.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.backend.Abroad_School.model.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

    
} 
