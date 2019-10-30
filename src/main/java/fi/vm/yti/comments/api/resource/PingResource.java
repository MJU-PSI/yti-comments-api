package fi.vm.yti.comments.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("/ping")
@Tag(name = "Health")
public class PingResource {

    @GET
    @Operation(summary = "Ping pong health check API.")
    @ApiResponse(responseCode = "200", description = "Returns pong if service is this API is reachable.")
    @Produces("text/plain")
    public Response ping() {
        return Response.ok("pong").build();
    }
}
