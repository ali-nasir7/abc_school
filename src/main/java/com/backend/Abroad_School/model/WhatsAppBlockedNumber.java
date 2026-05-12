package com.backend.Abroad_School.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "whatsapp_blocked_numbers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class WhatsAppBlockedNumber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 20)
    private String phoneNumber;

    @Column(length = 500)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime blockedAt;

    @PrePersist
    public void prePersist() {
        if (this.blockedAt == null) {
            this.blockedAt = LocalDateTime.now();
        }
    }
}