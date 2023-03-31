package fi.vm.yti.comments.api.configuration;

import javax.ws.rs.ApplicationPath;

import org.glassfish.jersey.jackson.internal.jackson.jaxrs.json.JacksonJaxbJsonProvider;
import org.glassfish.jersey.message.DeflateEncoder;
import org.glassfish.jersey.message.GZipEncoder;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.filter.EncodingFilter;
import org.springframework.stereotype.Component;

import fi.vm.yti.comments.api.exception.exceptionmapping.UncaughtExceptionMapper;
import fi.vm.yti.comments.api.exception.exceptionmapping.YtiCommentsExceptionMapper;
import fi.vm.yti.comments.api.filter.CacheFilter;
import fi.vm.yti.comments.api.filter.CharsetResponseFilter;
import fi.vm.yti.comments.api.filter.RequestLoggingFilter;
import fi.vm.yti.comments.api.resource.AuthenticatedUserResource;
import fi.vm.yti.comments.api.resource.CommentResource;
import fi.vm.yti.comments.api.resource.CommentRoundResource;
import fi.vm.yti.comments.api.resource.CommentThreadResource;
import fi.vm.yti.comments.api.resource.ImpersonateUserResource;
import fi.vm.yti.comments.api.resource.IntegrationResource;
import fi.vm.yti.comments.api.resource.OrganizationResource;
import fi.vm.yti.comments.api.resource.PingResource;
import fi.vm.yti.comments.api.resource.SourceResource;
import fi.vm.yti.comments.api.resource.SystemResource;
import fi.vm.yti.comments.api.resource.UriResolverResource;
import fi.vm.yti.comments.api.resource.externalresources.CodelistProxyResource;
import fi.vm.yti.comments.api.resource.externalresources.DatamodelProxyResource;
import fi.vm.yti.comments.api.resource.externalresources.GroupManagementProxyResource;
import fi.vm.yti.comments.api.resource.externalresources.TerminologyProxyResource;
import io.swagger.v3.jaxrs2.integration.resources.OpenApiResource;
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@Component
@OpenAPIDefinition(
    info = @Info(
        description = "YTI Comments - Comments API - Spring Boot microservice.",
        version = "v1",
        title = "YTI Comments - Comments API Service",
        termsOfService = "https://opensource.org/licenses/EUPL-1.1",
        contact = @Contact(
            name = "Content commenting Service by the Digital and Population Data Services Agency"
        ),
        license = @License(
            name = "EUPL-1.2",
            url = "https://opensource.org/licenses/EUPL-1.1"
        )
    ),
    servers = {
        @Server(
            description = "Comments API",
            url = "/comments-api")
    }
)
@ApplicationPath("/api")
public class JerseyConfig extends ResourceConfig {

    public JerseyConfig() {
        final JacksonJaxbJsonProvider provider = new JacksonJaxbJsonProvider();
        provider.setMapper(new CustomObjectMapper());

        // Charset filter
        register(CharsetResponseFilter.class);

        // Cache control headers to no cache.
        register(CacheFilter.class);

        // Gzip
        register(EncodingFilter.class);
        register(GZipEncoder.class);
        register(DeflateEncoder.class);

        // ExceptionMappers
        register(YtiCommentsExceptionMapper.class);
        register(UncaughtExceptionMapper.class);

        // Logging
        register(RequestLoggingFilter.class);

        // System
        register(SystemResource.class);

        // Ping test API
        register(PingResource.class);

        // Swagger
        register(OpenApiResource.class);

        // GroupManagement
        register(GroupManagementProxyResource.class);

        // Authentication
        register(AuthenticatedUserResource.class);
        register(ImpersonateUserResource.class);

        // Integrations
        register(CodelistProxyResource.class);
        register(DatamodelProxyResource.class);
        register(TerminologyProxyResource.class);

        // API: Integration API
        register(IntegrationResource.class);

        // APIs
        register(SourceResource.class);
        register(CommentResource.class);
        register(CommentRoundResource.class);
        register(CommentThreadResource.class);
        register(OrganizationResource.class);

        // API: URI Resolver
        register(UriResolverResource.class);
    }
}
