package com.github.sibmaks.springdocsrender.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sibmaks.springdocsrender.api.template.TemplateEngine;
import com.github.sibmaks.springdocsrender.repository.TemplateRepository;
import com.github.sibmaks.springdocsrender.repository.entity.TemplateEntity;
import lombok.RequiredArgsConstructor;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.data.JsonDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.util.Pair;
import org.springframework.stereotype.Service;

import java.io.*;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateService {
    private final TemplateRepository templateRepository;
    private final ObjectMapper objectMapper;

    private static byte[] compileTemplate(byte[] template) {
        var report = new ByteArrayInputStream(template);
        JasperReport jasperReport;
        try {
            jasperReport = JasperCompileManager.compileReport(report);
        } catch (JRException e) {
            throw new RuntimeException(e);
        }
        var baos = new ByteArrayOutputStream();
        try (var oos = new ObjectOutputStream(baos)) {
            oos.writeObject(jasperReport);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }

    private static BigInteger getSHA512(byte[] input) throws NoSuchAlgorithmException {
        var md = MessageDigest.getInstance("SHA-512");
        var messageDigest = md.digest(input);
        return new BigInteger(1, messageDigest);
    }

    private static String toSignature(BigInteger no) {
        var hashText = new StringBuilder(no.toString(16));

        while (hashText.length() < 128) {
            hashText.insert(0, "0");
        }

        return hashText.toString();
    }

    public long add(String code, byte[] template, TemplateEngine engine) {
        var compiledTemplate = compileTemplate(template);

        var entity = TemplateEntity.builder()
                .code(code)
                .template(compiledTemplate)
                .engine(engine)
                .createdAt(LocalDateTime.now())
                .modifiedAt(LocalDateTime.now())
                .build();

        entity = templateRepository.save(entity);

        return entity.getId();
    }

    public long update(long id, String code, byte[] template, TemplateEngine engine) {
        var compiledTemplate = compileTemplate(template);

        var entity = getById(id);

        entity.setCode(code);
        entity.setTemplate(compiledTemplate);
        entity.setEngine(engine);
        entity.setModifiedAt(LocalDateTime.now());

        entity = templateRepository.save(entity);

        return entity.getId();
    }

    public TemplateEntity getByCode(String code) {
        return templateRepository.findByCode(code)
                .orElseThrow(() -> new IllegalArgumentException("Template \"%s\" not found".formatted(code)));
    }

    public TemplateEntity getById(long id) {
        return templateRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Template \"%s\" not found".formatted(id)));
    }

    public List<TemplateEntity> getAll() {
        return templateRepository.findAll();
    }

    public void deleteById(long id) {
        templateRepository.deleteById(id);
    }

    public Pair<byte[], String> render(long templateId, Map<String, Object> model) {
        var entity = getById(templateId);

        try {
            var serialized = objectMapper.writeValueAsBytes(model);

            var jsonStream = new ByteArrayInputStream(serialized);

            var dataSource = new JsonDataSource(jsonStream);

            JasperReport jasperReport;
            try (var ois = new ObjectInputStream(new ByteArrayInputStream(entity.getTemplate()))) {
                jasperReport = (JasperReport) ois.readObject();
            }

            var jasperPrint = JasperFillManager.fillReport(jasperReport, null, dataSource);

            var pdf = JasperExportManager.exportReportToPdf(jasperPrint);

            var modelsHash = getSHA512(serialized);
            var pdfHash = getSHA512(pdf);
            var signature = toSignature(modelsHash.xor(pdfHash));

            return Pair.of(pdf, signature);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
