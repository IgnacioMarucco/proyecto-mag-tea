package com.utn.magtea.storage;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "minio")
@Getter
@Setter
public class MinioProperties {
    private String url = "http://minio:9000";
    private String publicUrl;
    private String accessKey = "minioadmin";
    private String secretKey = "minioadmin";
    private int presignedExpiryMinutes = 5;
}
