package com.github.sibmaks.springdocsrender.repository;

import com.github.sibmaks.springdocsrender.repository.entity.TemplateEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface TemplateRepository extends JpaRepository<TemplateEntity, Long> {

    Optional<TemplateEntity> findByCode(String code);

}
