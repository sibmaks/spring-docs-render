package com.github.sibmaks.springdocsrender.api.template.rq;

import lombok.*;

import java.util.Map;

/**
 * @author sibmaks
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenderRq {
    private Map<String, Object> model;
}
