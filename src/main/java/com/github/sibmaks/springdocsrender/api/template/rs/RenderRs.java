package com.github.sibmaks.springdocsrender.api.template.rs;

import lombok.*;

import java.time.LocalDateTime;

/**
 * @author sibmaks
 */
@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RenderRs {
    private String content;
    private String signature;
    private LocalDateTime createdAt;
}
