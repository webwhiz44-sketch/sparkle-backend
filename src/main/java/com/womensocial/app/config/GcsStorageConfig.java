package com.womensocial.app.config;

import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class GcsStorageConfig {

    @Bean
    public Storage gcsStorage() {
        // Uses Application Default Credentials automatically on Cloud Run (service account)
        // For local dev: set GOOGLE_APPLICATION_CREDENTIALS env var pointing to a service account JSON
        return StorageOptions.getDefaultInstance().getService();
    }
}
