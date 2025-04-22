package com.github.sibmaks.springdocsrender.api.template.rq;

import com.github.sibmaks.springdocsrender.api.template.TemplateEngine;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
@AllArgsConstructor
public class TemplateRq {
    private String code;
    private String template;
    private TemplateEngine engine;
}
