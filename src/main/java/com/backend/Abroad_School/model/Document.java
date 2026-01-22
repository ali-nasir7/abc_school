package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "documents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String fileName;

    
    @Column(nullable = false)
    private String filePath;

    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

   
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

 
    public enum DocumentType {
        STUDENT_PHOTO,
        CNIC_FATHER,
        CNIC_MOTHER,
        CERTIFICATE
    }
}
