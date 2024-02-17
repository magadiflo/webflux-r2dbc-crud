package dev.magadiflo.r2dbc.app.configuration;

import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.config.CorsRegistry;
import org.springframework.web.reactive.config.WebFluxConfigurer;

import java.util.Arrays;

@Slf4j
@Configuration
public class AppConfig implements WebFluxConfigurer {

    @Value("${app.cors.pathPattern:/**}")
    private String pathPattern;
    @Value("${app.cors.allowedOrigins:*}")
    private String[] allowedOrigins;
    @Value("${app.cors.allowedHeaders:*}")
    private String[] allowedHeaders;
    @Value("${app.cors.allowedMethods:*}")
    private String[] allowedMethods;
    @Value("${app.cors.maxAge:1800}")
    private long maxAge;


    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }


    @Override
    public void addCorsMappings(CorsRegistry corsRegistry) {
        log.info("pathPattern: {}", pathPattern);
        log.info("allowedOrigins: {}", Arrays.toString(allowedOrigins));
        log.info("allowedMethods: {}", Arrays.toString(allowedMethods));
        log.info("maxAge: {}", maxAge);

        corsRegistry.addMapping(pathPattern)
                .allowedHeaders(allowedHeaders)
                .allowedOrigins(allowedOrigins)
                .allowedMethods(allowedMethods)
                .maxAge(maxAge);
    }
}
