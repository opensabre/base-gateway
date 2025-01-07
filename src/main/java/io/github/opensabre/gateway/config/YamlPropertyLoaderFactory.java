package io.github.opensabre.gateway.config;

import lombok.NonNull;
import org.springframework.boot.env.YamlPropertySourceLoader;
import org.springframework.core.io.support.DefaultPropertySourceFactory;
import org.springframework.core.io.support.EncodedResource;

import java.io.IOException;

/**
 * yaml加载
 */
public class YamlPropertyLoaderFactory extends DefaultPropertySourceFactory {

    @Override
    public @NonNull org.springframework.core.env.PropertySource<?> createPropertySource(String name, EncodedResource resource) throws IOException {
        return new YamlPropertySourceLoader().load(resource.getResource().getFilename(), resource.getResource()).get(0);
    }
}