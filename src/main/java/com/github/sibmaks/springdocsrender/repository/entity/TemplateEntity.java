package com.github.sibmaks.springdocsrender.repository.entity;

import com.github.sibmaks.springdocsrender.api.template.TemplateEngine;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "template")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class TemplateEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String code;
    @Lob
    @Column(nullable = false)
    private byte[] template;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TemplateEngine engine;
    @Column(nullable = false)
    private LocalDateTime createdAt;
    @Column(nullable = false)
    private LocalDateTime modifiedAt;
}
