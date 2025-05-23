package dev.getelements.elements.config;

import dev.getelements.elements.sdk.model.Constants;

import java.util.Properties;

import static dev.getelements.elements.sdk.model.Constants.*;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class CommonModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties();
        defaultProperties.setProperty(HTTP_BIND_ADDRESS, "0.0.0.0");
        defaultProperties.setProperty(SHORT_LINK_BASE, "http://localhost:8888/l");
        defaultProperties.setProperty(QUERY_MAX_RESULTS, "100");
        defaultProperties.setProperty(PASSWORD_DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(PASSWORD_ENCODING, "UTF-8");
        defaultProperties.setProperty(CORS_ALLOWED_ORIGINS, "http://localhost:8080,http://127.0.0.1:8080,http://localhost:4200,http://127.0.0.1:4200");
        defaultProperties.setProperty(ASYNC_TIMEOUT_LIMIT, Integer.toString(0));
        defaultProperties.setProperty(GENERATED_PASSWORD_LENGTH, "24");
        defaultProperties.setProperty(GLOBAL_SECRET, "");

        // The way the dependent code uses this is a little weird and should be reworked to eliminate some redundancy.
        defaultProperties.setProperty(APP_OUTSIDE_URL, "http://localhost:8080/");
        defaultProperties.setProperty(DOC_OUTSIDE_URL, "http://localhost:8080/doc");
        defaultProperties.setProperty(API_OUTSIDE_URL, "http://localhost:8080/api/rest");
        defaultProperties.setProperty(HTTP_TUNNEL_URL, "http://localhost:8080/app");
        defaultProperties.setProperty(CODE_SERVE_URL,  "http://localhost:8080/code");
        defaultProperties.setProperty(CDN_OUTSIDE_URL, "http://localhost:8080/cdn");

        return defaultProperties;
    }

}
