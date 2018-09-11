package fi.vm.yti.comments.api.resource;

import java.io.File;
import java.io.IOException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponse;
import static fi.vm.yti.comments.api.ServiceInitializer.LOCAL_SWAGGER_DATA_DIR;

@Component
@Path("/swagger.json")
@Api(value = "swagger.json")
public class SwaggerResource {

    @GET
    @ApiOperation(value = "Get Swagger JSON", response = String.class)
    @ApiResponse(code = 200, message = "Returns the swagger.json description for this service.")
    @Produces("text/plain")
    @SuppressFBWarnings("DMI_HARDCODED_ABSOLUTE_FILENAME")
    public String getSwaggerJson() throws IOException {
        final File file = new File(LOCAL_SWAGGER_DATA_DIR + "swagger.json");
        return FileUtils.readFileToString(file, "UTF-8");
    }
}
