package fi.vm.yti.comments.api.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("")
@Component
@Validated
public class CommentsApiProperties {

    private String defaultLanguage;

    public String getDefaultLanguage() {
        return this.defaultLanguage != null ? this.defaultLanguage : "en";
    }

    public void setDefaultLanguage(final String defaultLanguage) {
        this.defaultLanguage = defaultLanguage;
    }
}
