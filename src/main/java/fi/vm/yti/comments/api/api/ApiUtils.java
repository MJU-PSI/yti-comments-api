package fi.vm.yti.comments.api.api;

import javax.inject.Inject;

import org.springframework.stereotype.Component;

import fi.vm.yti.comments.api.configuration.CodelistProperties;
import fi.vm.yti.comments.api.configuration.CommentsApiConfiguration;
import fi.vm.yti.comments.api.configuration.DatamodelProperties;
import fi.vm.yti.comments.api.configuration.FrontendConfiguration;
import fi.vm.yti.comments.api.configuration.GroupManagementProperties;
import fi.vm.yti.comments.api.configuration.MessagingProperties;
import fi.vm.yti.comments.api.configuration.TerminologyProperties;
import fi.vm.yti.comments.api.configuration.UriProperties;

import static fi.vm.yti.comments.api.constants.ApiConstants.*;

@Component
public class ApiUtils {

    private static final String URI_COMMENT_ROUND_CONTEXT_PATH = "/comments/round/";
    private static final String PATH_THREAD = "/thread";
    private static final String PATH_COMMENT = "/comment";

    private final CommentsApiConfiguration commentsApiConfiguration;
    private final FrontendConfiguration frontendConfiguration;
    private final GroupManagementProperties groupManagementProperties;
    private final TerminologyProperties terminologyProperties;
    private final DatamodelProperties dataModelProperties;
    private final CodelistProperties codelistProperties;
    private final MessagingProperties messagingProperties;
    private final UriProperties uriProperties;

    @Inject
    public ApiUtils(final CommentsApiConfiguration commentsApiConfiguration,
                    final FrontendConfiguration frontendConfiguration,
                    final GroupManagementProperties groupManagementProperties,
                    final TerminologyProperties terminologyProperties,
                    final DatamodelProperties dataModelProperties,
                    final CodelistProperties codelistProperties,
                    final MessagingProperties messagingProperties,
                    final UriProperties uriProperties) {
        this.commentsApiConfiguration = commentsApiConfiguration;
        this.frontendConfiguration = frontendConfiguration;
        this.groupManagementProperties = groupManagementProperties;
        this.terminologyProperties = terminologyProperties;
        this.dataModelProperties = dataModelProperties;
        this.codelistProperties = codelistProperties;
        this.messagingProperties = messagingProperties;
        this.uriProperties = uriProperties;
    }

    public String getEnv() {
        return commentsApiConfiguration.getEnv();
    }

    public String getGroupmanagementPublicUrl() {
        return groupManagementProperties.getPublicUrl();
    }

    public String getTerminologyPublicUrl() {
        return terminologyProperties.getPublicUrl();
    }

    public String getDataModelPublicUrl() {
        return dataModelProperties.getPublicUrl();
    }

    public String getCodelistPublicUrl() {
        return codelistProperties.getPublicUrl();
    }

    public boolean getMessagingEnabled() {
        return messagingProperties.getEnabled();
    }

    public String createCommentRoundUrl(final Integer commentRoundSequenceId) {
        return createResourceUrl(API_PATH_COMMENTROUNDS, commentRoundSequenceId.toString());
    }

    public String createCommentRoundWebUrl(final Integer commentRoundSequenceId) {
        return createFrontendBaseUrl() + "/round;round=" + commentRoundSequenceId.toString();
    }

    public String createCommentThreadUrl(final Integer commentRoundSequenceId,
                                         final Integer commentThreadSequenceId) {
        return createResourceUrl(API_PATH_COMMENTROUNDS + "/" + commentRoundSequenceId.toString() + API_PATH_COMMENTTHREADS, commentRoundSequenceId.toString());
    }

    public String createCommentThreadWebUrl(final Integer commentRoundSequenceId,
                                            final Integer commentThreadSequenceId) {
        return createFrontendBaseUrl() + "/round;round=" + commentRoundSequenceId.toString() + ";thread=" + commentThreadSequenceId.toString();
    }

    public String createCommentUrl(final Integer commentRoundSequenceId,
                                   final Integer commentThreadSequenceId,
                                   final Integer commentSequenceId) {
        return createResourceUrl(API_PATH_COMMENTROUNDS + "/" + commentRoundSequenceId.toString() + API_PATH_COMMENTTHREADS + "/" + commentThreadSequenceId.toString(), commentSequenceId.toString());
    }

    public String createCommentWebUrl(final Integer commentRoundSequenceId,
                                      final Integer commentThreadSequenceId,
                                      final Integer commentSequenceId) {
        return createFrontendBaseUrl() + "/round;round=" + commentRoundSequenceId.toString() + ";thread=" + commentThreadSequenceId.toString() + ";comment=" + commentSequenceId.toString();
    }

    private String createResourceUrl(final String apiPath,
                                     final String resourceId) {
        final StringBuilder builder = new StringBuilder();
        builder.append(createBaseUrl());
        builder.append(commentsApiConfiguration.getContextPath());
        builder.append(API_BASE_PATH);
        builder.append("/");
        builder.append(API_VERSION_V1);
        builder.append(apiPath);
        builder.append("/");
        if (resourceId != null && !resourceId.isEmpty()) {
            builder.append(resourceId);
        }
        return builder.toString();
    }

    private String createBaseUrl() {
        final String port = commentsApiConfiguration.getPort();
        final StringBuilder builder = new StringBuilder();
        builder.append(commentsApiConfiguration.getScheme());
        builder.append("://");
        builder.append(commentsApiConfiguration.getHost());
        if (port != null && port.length() > 0) {
            builder.append(":");
            builder.append(port);
        }
        return builder.toString();
    }

    private String createFrontendBaseUrl() {
        final String port = frontendConfiguration.getPort();
        final StringBuilder builder = new StringBuilder();
        builder.append(frontendConfiguration.getScheme());
        builder.append("://");
        builder.append(frontendConfiguration.getHost());
        if (port != null && port.length() > 0) {
            builder.append(":");
            builder.append(port);
        }
        return builder.toString();
    }

    public String createCommentRoundUri(final Integer commentRoundSequenceId) {
        return uriProperties.getUriAddress() + URI_COMMENT_ROUND_CONTEXT_PATH + commentRoundSequenceId.toString();
    }

    public String createCommentThreadUri(final Integer commentRoundSequenceId,
                                         final Integer commentThreadSequenceId) {
        return uriProperties.getUriAddress() + URI_COMMENT_ROUND_CONTEXT_PATH + commentRoundSequenceId.toString() + PATH_THREAD + "/" + commentThreadSequenceId.toString();
    }

    public String createCommentUri(final Integer commentRoundSequenceId,
                                   final Integer commentThreadSequenceId,
                                   final Integer commentSequenceId) {
        return uriProperties.getUriAddress() + URI_COMMENT_ROUND_CONTEXT_PATH + commentRoundSequenceId.toString() + PATH_THREAD + "/" + commentThreadSequenceId.toString() + PATH_COMMENT + "/" + commentSequenceId.toString();
    }
}
