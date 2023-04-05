package fi.vm.yti.comments.api.resource;

import java.net.URI;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.HeaderParam;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import fi.vm.yti.comments.api.api.ApiUtils;
import fi.vm.yti.comments.api.configuration.UriProperties;
import fi.vm.yti.comments.api.dao.CommentDao;
import fi.vm.yti.comments.api.dao.CommentRoundDao;
import fi.vm.yti.comments.api.dao.CommentThreadDao;
import fi.vm.yti.comments.api.entity.Comment;
import fi.vm.yti.comments.api.entity.CommentRound;
import fi.vm.yti.comments.api.entity.CommentThread;
import fi.vm.yti.comments.api.error.ErrorModel;
import fi.vm.yti.comments.api.exception.NotFoundException;
import fi.vm.yti.comments.api.exception.YtiCommentsException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

@Component
@Path("/v1/uris")
@Produces({ MediaType.APPLICATION_JSON + ";charset=UTF-8", "application/xlsx", "application/csv" })
@Tag(name = "Resolver")
public class UriResolverResource implements AbstractBaseResource {

    private static final Logger LOG = LoggerFactory.getLogger(UriResolverResource.class);
    private static final String API_PATH_COMMENTS = "/comments";
    private static final String PATH_COMMENTROUND = "round";
    private static final String PATH_THREAD = "thread";
    private static final String PATH_COMMENT = "comment";
    private static final String HEADER_YTITOKEN = "YTITOKEN";

    private final CommentRoundDao commentRoundDao;
    private final CommentThreadDao commentThreadDao;
    private final CommentDao commentDao;
    private final ApiUtils apiUtils;

    @Autowired
    UriProperties uriProperties;

    @Inject
    public UriResolverResource(final CommentRoundDao commentRoundDao,
                               final CommentThreadDao commentThreadDao,
                               final CommentDao commentDao,
                               final ApiUtils apiUtils) {
        this.commentRoundDao = commentRoundDao;
        this.commentThreadDao = commentThreadDao;
        this.commentDao = commentDao;
        this.apiUtils = apiUtils;
    }

    @GET
    @Path("resolve")
    @Operation(description = "Resolve URI resource.")
    @ApiResponse(responseCode = "200", description = "Resolves the API url for the given comments resource URI.")
    @Produces({ MediaType.APPLICATION_JSON + ";charset=UTF-8", MediaType.TEXT_PLAIN })
    public Response resolveUri(@Parameter(description = "Resource URI.", required = true, in = ParameterIn.QUERY) @QueryParam("uri") final String uri) {
        final URI resolveUri = parseUriFromString(uri);
        ensureUriHost(uri, uriProperties.getUriHostAddress());
        final String uriPath = resolveUri.getPath();
        final ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        objectMapper.registerModule(new JavaTimeModule());
        final ObjectNode json = objectMapper.createObjectNode();
        json.put("uri", uri);
        checkResourceValidity(uriPath);
        final String resourcePath = uriPath.substring(API_PATH_COMMENTS.length() + 1);
        final List<String> resourceCodeValues = Arrays.asList(resourcePath.split("/"));
        json.put("url", resolveResourceApiUrl(resourceCodeValues));
        return Response.ok().entity(json).build();
    }

    @GET
    @Path("redirect")
    @Operation(description = "Redirect URI resource.")
    @Consumes({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
    @Produces({ MediaType.APPLICATION_JSON, MediaType.TEXT_HTML })
    @ApiResponse(responseCode = "303", description = "Does a redirect from comments resource URI to comments API.")
    @ApiResponse(responseCode = "406", description = "Resource not found.")
    @ApiResponse(responseCode = "406", description = "Cannot redirect to given URI.")
    public Response redirectUri(@HeaderParam("Accept") String accept,
                                @Parameter(description = "Format for returning content.", in = ParameterIn.QUERY) @QueryParam("format") final String format,
                                @Parameter(description = "Filter string (csl) for expanding specific child resources.", in = ParameterIn.QUERY) @QueryParam("expand") final String expand,
                                @Parameter(description = "Filter string (csl) for expanding specific child resources.", in = ParameterIn.QUERY) @QueryParam("token") final String token,
                                @Parameter(description = "Resource URI.", required = true, in = ParameterIn.QUERY) @QueryParam("uri") final String uri) {
        ensureUriHost(uri, uriProperties.getUriHostAddress());
        final String uriPath = uri.substring((uriProperties.getUriHostAddress()).length());
        checkResourceValidity(uriPath);
        final String resourcePath = uriPath.substring(API_PATH_COMMENTS.length() + 1);
        final List<String> resourcePathParams = parseResourcePathIdentifiers(resourcePath);
        final List<String> acceptHeaders = parseAcceptHeaderValues(accept);
        final URI redirectUri;
        if (format != null && !format.isEmpty()) {
            redirectUri = constructUri(resolveResourceApiUrl(resourcePathParams), format, expand);
        } else if (acceptHeaders.contains(MediaType.APPLICATION_JSON)) {
            redirectUri = URI.create(resolveResourceApiUrl(resourcePathParams));
        } else {
            redirectUri = URI.create(resolveResourceWebUrl(resourcePathParams));
        }
        if (token != null && !token.isEmpty()) {
            return Response.seeOther(redirectUri).header("Set-Cookie", HEADER_YTITOKEN + "=" + token + ";path=/;HttpOnly;").build();
        } else {
            return Response.seeOther(redirectUri).build();
        }
    }

    private URI constructUri(final String uri,
                             final String format,
                             final String expand) {
        final StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(uri);
        boolean firstParameter = true;
        if (format != null && !format.isEmpty()) {
            stringBuffer.append("?format=");
            stringBuffer.append(format);
            firstParameter = false;
        }
        if (expand != null && !expand.isEmpty()) {
            if (firstParameter) {
                stringBuffer.append("?");
            } else {
                stringBuffer.append("&");
            }
            stringBuffer.append("expand=");
            stringBuffer.append(expand);
        }
        return URI.create(stringBuffer.toString());
    }

    private List<String> parseResourcePathIdentifiers(final String resourcePath) {
        if (resourcePath.contains("?")) {
            return Arrays.asList(resourcePath.substring(0, resourcePath.indexOf("?")).split("/"));
        }
        return Arrays.asList(resourcePath.split("/"));
    }

    private List<String> parseAcceptHeaderValues(final String accept) {
        final List<String> acceptHeaders = new ArrayList<>();
        for (final String acceptValue : accept.split("\\s*,\\s*")) {
            if (acceptValue.contains(";q=")) {
                acceptHeaders.add(acceptValue.substring(0, acceptValue.indexOf(";q=")));
            } else {
                acceptHeaders.add(acceptValue);
            }
        }
        return acceptHeaders;
    }

    private String resolveResourceApiUrl(final List<String> resourceCodeValues) {
        return resolveResourceUrl(resourceCodeValues, false);
    }

    private String resolveResourceWebUrl(final List<String> resourceCodeValues) {
        return resolveResourceUrl(resourceCodeValues, true);
    }

    private String resolveResourceUrl(final List<String> resourceCodeValues,
                                      final boolean returnWebUrl) {
        final String url;
        switch (resourceCodeValues.size()) {
            // ROUND
            case 2: {
                final String pathIdentifier = checkNotEmpty(resourceCodeValues.get(0));
                if (PATH_COMMENTROUND.equalsIgnoreCase(pathIdentifier)) {
                    final Integer commentRoundSequenceId = checkNotEmptyInteger(resourceCodeValues.get(1));
                    checkCommentRoundExists(commentRoundSequenceId);
                    if (returnWebUrl) {
                        url = apiUtils.createCommentRoundWebUrl(commentRoundSequenceId);
                    } else {
                        url = apiUtils.createCommentRoundUrl(commentRoundSequenceId);
                    }
                    break;
                } else {
                    throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable!"));
                }
            }
            // THREAD
            case 4: {
                final String commentRoundPathIdentifier = checkNotEmpty(resourceCodeValues.get(0));
                final String commentThreadPathIdentifier = checkNotEmpty(resourceCodeValues.get(2));
                if (PATH_COMMENTROUND.equalsIgnoreCase(commentRoundPathIdentifier) && PATH_THREAD.equalsIgnoreCase(commentThreadPathIdentifier)) {
                    final Integer commentRoundSequenceId = checkNotEmptyInteger(resourceCodeValues.get(1));
                    final Integer commentThreadSequenceId = checkNotEmptyInteger(resourceCodeValues.get(3));
                    checkCommentThreadExists(commentRoundSequenceId, commentThreadSequenceId);
                    if (returnWebUrl) {
                        url = apiUtils.createCommentThreadWebUrl(commentRoundSequenceId, commentThreadSequenceId);
                    } else {
                        url = apiUtils.createCommentThreadUrl(commentRoundSequenceId, commentThreadSequenceId);
                    }
                    break;
                } else {
                    throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable!"));
                }
            }
            // COMMENT
            case 6: {
                final String commentRoundPathIdentifier = checkNotEmpty(resourceCodeValues.get(0));
                final String commentThreadPathIdentifier = checkNotEmpty(resourceCodeValues.get(2));
                final String commentPathIdentifier = checkNotEmpty(resourceCodeValues.get(4));
                if (PATH_COMMENTROUND.equalsIgnoreCase(commentRoundPathIdentifier) && PATH_THREAD.equalsIgnoreCase(commentThreadPathIdentifier) && PATH_COMMENT.equalsIgnoreCase(commentPathIdentifier)) {
                    final Integer commentRoundSequenceId = checkNotEmptyInteger(resourceCodeValues.get(1));
                    final Integer commentThreadSequenceId = checkNotEmptyInteger(resourceCodeValues.get(3));
                    final Integer commentSequenceId = checkNotEmptyInteger(resourceCodeValues.get(5));
                    checkCommentExists(commentRoundSequenceId, commentThreadSequenceId, commentSequenceId);
                    if (returnWebUrl) {
                        url = apiUtils.createCommentWebUrl(commentRoundSequenceId, commentThreadSequenceId, commentSequenceId);
                    } else {
                        url = apiUtils.createCommentUrl(commentRoundSequenceId, commentThreadSequenceId, commentSequenceId);
                    }
                    break;
                } else {
                    throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable!"));
                }
            }
            default:
                throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable!"));
        }
        return url;
    }

    private void checkResourceValidity(final String uriPath) {
        final String resourcePath = uriPath.substring(API_PATH_COMMENTS.length() + 1);
        final List<String> resourceCodeValues = Arrays.asList(resourcePath.split("/"));
        if (!uriPath.toLowerCase().startsWith(API_PATH_COMMENTS)) {
            LOG.error("Comments resource URI not resolvable, wrong context path!");
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable, wrong context path!"));
        } else if (resourceCodeValues.isEmpty()) {
            LOG.error("Comments resource URI not resolvable, empty resource path!");
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Comments resource URI not resolvable, empty resource path!"));
        }
    }

    private Integer checkNotEmptyInteger(final String string) {
        if (string != null && !string.isEmpty()) {
            try {
                return Integer.parseInt(string);
            } catch (final NumberFormatException e) {
                LOG.error("Resource hook not valid due to invalid resource ID.");
                throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Resource hook not valid due to invalid resource ID."));
            }
        } else {
            LOG.error("Resource hook not valid due to empty resource ID.");
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Resource hook not valid due to empty resource ID."));
        }
    }

    private String checkNotEmpty(final String string) {
        if (string != null && !string.isEmpty()) {
            return string;
        } else {
            LOG.error("Resource hook not valid due to empty resource ID.");
            throw new YtiCommentsException(new ErrorModel(HttpStatus.NOT_ACCEPTABLE.value(), "Resource hook not valid due to empty resource ID."));
        }
    }

    private void checkCommentRoundExists(final Integer commentRoundSequenceId) {
        final CommentRound commentRound = commentRoundDao.findBySequenceId(commentRoundSequenceId);
        if (commentRound == null) {
            throw new NotFoundException();
        }
    }

    private void checkCommentThreadExists(final Integer commentRoundSequenceId,
                                          final Integer commentThreadSequenceId) {
        final CommentRound commentRound = commentRoundDao.findBySequenceId(commentRoundSequenceId);
        if (commentRound != null) {
            final CommentThread commentThread = commentThreadDao.findByCommentRoundIdAndSequenceId(commentRound.getId(), commentThreadSequenceId);
            if (commentThread == null) {
                throw new NotFoundException();
            }
        } else {
            throw new NotFoundException();
        }
    }

    private void checkCommentExists(final Integer commentRoundSequenceId,
                                    final Integer commentThreadSequenceId,
                                    final Integer commentSequenceId) {
        final CommentRound commentRound = commentRoundDao.findBySequenceId(commentRoundSequenceId);
        if (commentRound != null) {
            final CommentThread commentThread = commentThreadDao.findByCommentRoundIdAndSequenceId(commentRound.getId(), commentThreadSequenceId);
            if (commentThread != null) {
                final Comment comment = commentDao.findByCommentThreadIdAndSequenceId(commentThread.getId(), commentSequenceId);
                if (comment == null) {
                    throw new NotFoundException();
                }
            } else {
                throw new NotFoundException();
            }
        } else {
            throw new NotFoundException();
        }
    }
}
