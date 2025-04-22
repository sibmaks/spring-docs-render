package com.github.sibmaks.springdocsrender.controller;

import com.github.sibmaks.springdocsrender.api.template.rq.RenderRq;
import com.github.sibmaks.springdocsrender.api.template.rq.TemplateRq;
import com.github.sibmaks.springdocsrender.api.template.rs.RenderRs;
import com.github.sibmaks.springdocsrender.repository.entity.TemplateEntity;
import com.github.sibmaks.springdocsrender.service.TemplateService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;

@RestController
@RequestMapping("/api/template/")
@RequiredArgsConstructor(onConstructor = @__(@Autowired))
public class TemplateController {
    private final TemplateService templateService;
    private final Base64.Encoder base64Encoder;
    private final Base64.Decoder base64Decoder;

    @PostMapping(
            value = "/",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public long add(@RequestBody TemplateRq rq) {
        var template = base64Decoder.decode(rq.getTemplate());
        return templateService.add(
                rq.getCode(),
                template,
                rq.getEngine()
        );
    }

    @PatchMapping(
            value = "/{templateId}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public long update(
            @PathVariable("templateId") long templateId,
            @RequestBody TemplateRq rq
    ) {
        var template = base64Decoder.decode(rq.getTemplate());
        return templateService.update(
                templateId,
                rq.getCode(),
                template,
                rq.getEngine()
        );
    }

    @GetMapping(
            value = "/",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public List<TemplateEntity> getAll() {
        return templateService.getAll();
    }

    @GetMapping(
            value = "/{templateId}",
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public TemplateEntity getAll(
            @PathVariable("templateId") long templateId
    ) {
        return templateService.getById(templateId);
    }

    @DeleteMapping(
            value = "/{templateId}"
    )
    public void deleteById(
            @PathVariable("templateId") long templateId
    ) {
        templateService.deleteById(templateId);
    }

    @PostMapping(
            value = "/{templateId}/render",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE
    )
    public RenderRs render(
            @PathVariable("templateId") long templateId,
            @Validated @RequestBody RenderRq rq
    ) {
        var rendered = templateService.render(
                templateId,
                rq.getModel()
        );
        var content = rendered.getFirst();
        var signature = rendered.getSecond();
        return RenderRs.builder()
                .content(base64Encoder.encodeToString(content))
                .signature(signature)
                .createdAt(LocalDateTime.now())
                .build();
    }

}
