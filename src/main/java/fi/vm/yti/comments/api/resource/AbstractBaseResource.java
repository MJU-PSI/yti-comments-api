package fi.vm.yti.comments.api.resource;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;

import org.apache.poi.ss.usermodel.Workbook;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.EndpointConfigBase;
import org.glassfish.jersey.jackson.internal.jackson.jaxrs.cfg.ObjectWriterModifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;

import fi.vm.yti.comments.api.api.ResponseWrapper;
import fi.vm.yti.comments.api.dto.ResourceDTO;
import fi.vm.yti.comments.api.error.ErrorModel;
import fi.vm.yti.comments.api.error.Meta;
import fi.vm.yti.comments.api.exception.YtiCommentsException;
import fi.vm.yti.comments.api.model.Status;
import static fi.vm.yti.comments.api.constants.ApiConstants.*;

public interface AbstractBaseResource {

    String MESSAGE_TYPE_ADDED_OR_MODIFIED = "MESSAGE_TYPE_ADDED_OR_MODIFIED";

    String MESSAGE_TYPE_GET_RESOURCES = "MESSAGE_TYPE_GET_RESOURCES";

    String UTF_8_ENCODING = "UTF-8";

    default SimpleFilterProvider createSimpleFilterProviderWithSingleFilter(final String baseFilter,
                                                                            final String expand) {
        final List<String> baseFilters = new ArrayList<>();
        baseFilters.add(baseFilter);
        return createSimpleFilterProvider(baseFilters, expand);
    }

    default SimpleFilterProvider createBaseFilterProvider() {
        final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
        filterProvider.addFilter(FILTER_NAME_COMMENT, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        filterProvider.addFilter(FILTER_NAME_SOURCE, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        filterProvider.addFilter(FILTER_NAME_COMMENTROUND, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        filterProvider.addFilter(FILTER_NAME_COMMENTTHREAD, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        filterProvider.addFilter(FILTER_NAME_ORGANIZATION, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        filterProvider.addFilter(FILTER_NAME_COMMENTROUNDORGANIZATION, SimpleBeanPropertyFilter.filterOutAllExcept(FIELD_NAME_ID));
        return filterProvider;
    }

    default SimpleFilterProvider createSimpleFilterProvider(final List<String> baseFilters,
                                                            final String expand) {
        final SimpleFilterProvider filterProvider = createBaseFilterProvider();
        filterProvider.setFailOnUnknownId(false);
        if (baseFilters != null) {
            for (final String baseFilter : baseFilters) {
                filterProvider.removeFilter(baseFilter.trim());
            }
        }
        if (expand != null && !expand.isEmpty()) {
            final String[] filterOptions = expand.split(",");
            for (final String filter : filterOptions) {
                filterProvider.removeFilter(filter.trim());
            }
        }
        return filterProvider;
    }

    default Response streamExcelOutput(final Workbook workbook,
                                       final String filename) {
        final StreamingOutput stream = output -> {
            try {
                workbook.write(output);
            } catch (final Exception e) {
                throw new YtiCommentsException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Excel output generation failed!"));
            }
        };
        try {
            return Response.ok(stream, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").header(HEADER_CONTENT_DISPOSITION, "attachment; filename = " + URLEncoder.encode(filename.replace(" ", "_"), UTF_8_ENCODING)).build();
        } catch (final UnsupportedEncodingException e) {
            throw new YtiCommentsException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Excel output generation failed due to charset issue!"));
        }
    }

    default Response createDeleteResponse(final String objectType) {
        final Meta meta = new Meta();
        meta.setCode(200);
        meta.setMessage(objectType + " deleted.");
        final ResponseWrapper responseWrapper = new ResponseWrapper(meta);
        return Response.ok(responseWrapper).build();
    }

    default <T> Response createResponse(final String objectType,
                                        final String messageType,
                                        final Set<T> set) {
        final Meta meta = new Meta();
        final ResponseWrapper<T> responseWrapper = new ResponseWrapper<>(meta);
        final String message;
        switch (messageType) {
            case MESSAGE_TYPE_ADDED_OR_MODIFIED: {
                message = " added or modified: ";
                break;
            }
            case MESSAGE_TYPE_GET_RESOURCES: {
                message = " found: ";
                break;
            }
            default: {
                throw new YtiCommentsException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Response message type unknown!"));
            }
        }
        meta.setMessage(objectType + message + set.size());
        meta.setCode(200);
        responseWrapper.setResults(set);
        return Response.ok(responseWrapper).build();
    }

    default Set<ResourceDTO> parseResourcesFromResponse(final ResponseEntity response) {
        final Object responseBody = response.getBody();
        if (responseBody != null) {
            try {
                final ObjectMapper mapper = new ObjectMapper();
                mapper.setFilterProvider(new SimpleFilterProvider().setFailOnUnknownId(false));
                final String data = responseBody.toString();
                final JsonNode jsonNode = mapper.readTree(data);
                final String dataString;
                if (!jsonNode.isArray() && jsonNode.has("results")) {
                    dataString = jsonNode.get("results").toString();
                } else {
                    dataString = "[]";
                }
                return mapper.readValue(dataString, new TypeReference<Set<ResourceDTO>>() {
                });
            } catch (final IOException e) {
                throw new YtiCommentsException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to parse integration resources!"));
            }
        } else {
            throw new YtiCommentsException(new ErrorModel(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Failed to parse integration resources!"));
        }
    }

    class FilterModifier extends ObjectWriterModifier {

        private final FilterProvider provider;

        FilterModifier(final FilterProvider provider) {
            this.provider = provider;
        }

        @Override
        public ObjectWriter modify(final EndpointConfigBase<?> endpoint,
                                   final MultivaluedMap<String, Object> responseHeaders,
                                   final Object valueToWrite,
                                   final ObjectWriter w,
                                   final JsonGenerator g) {
            return w.with(provider);
        }
    }

    default Set<String> parseStatusCsl(final String statusCsl) {
        final Set<String> statuses = new HashSet<>();
        if (statusCsl != null) {
            for (final String s : statusCsl.split(",")) {
                try {
                    final Status status = Status.valueOf(s.toUpperCase().trim());
                    statuses.add(status.toString());
                } catch (final Exception e) {
                    throw new YtiCommentsException(new ErrorModel(HttpStatus.BAD_REQUEST.value(), "Status code is not valid: " + s));
                }
            }
        }
        return statuses;
    }

    default URI parseUriFromString(final String uriString) {
        if (!uriString.isEmpty()) {
            return URI.create(uriString.replace(" ", "%20"));
        } else {
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "URI string was not valid!"));
        }
    }

    default void ensureUriHost(final String host, final String uriHost) {
        if (!host.startsWith(uriHost)) {
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "This URI is not resolvable as a comments resource."));
        }
    }
}
