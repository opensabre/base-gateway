package io.github.opensabre.gateway.entity;


import lombok.*;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode()
public class Resource {
    private String code;
    private String type;
    private String url;
    private String method;
    private String name;
    private String description;
}
