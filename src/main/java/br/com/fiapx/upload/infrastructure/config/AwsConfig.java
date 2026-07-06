package br.com.fiapx.upload.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;

import java.net.URI;

@Configuration
public class AwsConfig {

    @Value("${aws.region}") private String region;
    @Value("${aws.s3.endpoint}") private String endpoint;
    @Value("${aws.s3.path-style-access:false}") private boolean pathStyleAccess;

    @Bean
    public S3Client s3Client() {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(DefaultCredentialsProvider.create());

        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint));
        }
        if (pathStyleAccess) {
            builder.forcePathStyle(true);
        }
        builder.serviceConfiguration(S3Configuration.builder()
                .checksumValidationEnabled(false)
                .build());
        return builder.build();
    }
}
