package fi.vm.yti.comments.api.configuration;

import javax.validation.constraints.NotNull;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@ConfigurationProperties("uri")
@Component
@Validated
public class UriProperties {

    @NotNull
    private String host;

    @NotNull
    private String scheme;

    public String getHost() {
        return host;
    }

    public void setHost(final String host) {
        this.host = host;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(final String scheme) {
        this.scheme = scheme;
    }

    public String getUriAddress() {
        return scheme + "://" + host;
    }
}

