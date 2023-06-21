package fi.vm.yti.comments.api.service.impl;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.inject.Inject;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.comments.api.configuration.CommentsApiProperties;
import fi.vm.yti.comments.api.dao.CommentDao;
import fi.vm.yti.comments.api.dao.CommentThreadDao;
import fi.vm.yti.comments.api.dto.CommentThreadResultDTO;
import fi.vm.yti.comments.api.entity.Comment;
import fi.vm.yti.comments.api.entity.CommentThread;
import fi.vm.yti.comments.api.service.ResultService;
import static fi.vm.yti.comments.api.utils.StatusUtils.localizeResourceStatusToDefaultLanguage;

@Component
public class ResultServiceImpl implements ResultService {

    private final CommentThreadDao commentThreadDao;
    private final CommentDao commentDao;
    private final CommentsApiProperties commentsApiProperties;
    
    @Autowired
    private MessageSource messageSource;

    @Inject
    public ResultServiceImpl(@Lazy final CommentThreadDao commentThreadDao,
                             @Lazy final CommentDao commentDao,
                             final CommentsApiProperties commentsApiProperties) {
        this.commentThreadDao = commentThreadDao;
        this.commentDao = commentDao;
        this.commentsApiProperties = commentsApiProperties;
    }

    @Transactional
    public Set<CommentThreadResultDTO> getResultsForCommentThread(final UUID commentThreadId) {
        final Map<String, Integer> commentThreadResultsMap = new HashMap<>();
        final CommentThread commentThread = commentThreadDao.findById(commentThreadId);
        if (commentThread != null) {
            final Set<Comment> comments = commentDao.findByCommentThreadIdAndParentCommentIsNull(commentThreadId);
            comments.forEach(comment -> {
                final String endStatus = comment.getEndStatus();
                if (endStatus != null && !endStatus.isEmpty() && !"NOSTATUS".equalsIgnoreCase(endStatus)) {
                    if (commentThreadResultsMap.get(endStatus) != null) {
                        commentThreadResultsMap.put(endStatus, commentThreadResultsMap.get(endStatus) + 1);
                    } else {
                        commentThreadResultsMap.put(endStatus, 1);
                    }
                }
            });
        }
        final int totalCount = commentThreadResultsMap.values().stream().mapToInt(i -> i).sum();
        final Set<CommentThreadResultDTO> commentThreadResults = new HashSet<>();
        commentThreadResultsMap.forEach((status, count) -> {
            final CommentThreadResultDTO commentThreadResult = new CommentThreadResultDTO();
            commentThreadResult.setStatus(status);
            commentThreadResult.setCount(count);
            final Float percentage = count * 100f / totalCount;
            commentThreadResult.setPercentage(String.format("%.1f", percentage));
            commentThreadResults.add(commentThreadResult);
        });
        return commentThreadResults;
    }

    @Transactional
    public String getResultsForCommentThreadAsTextInDefaultLanguage(final UUID commentThreadId) {
        final StringBuilder results = new StringBuilder();
        final Set<CommentThreadResultDTO> commentThreadResults = getResultsForCommentThread(commentThreadId);
        for (final CommentThreadResultDTO result : commentThreadResults) {
            results.append(localizeResourceStatusToDefaultLanguage(result.getStatus(), messageSource, Locale.forLanguageTag(this.commentsApiProperties.getDefaultLanguage())));
            results.append(": ");
            results.append(result.getCount());
            results.append(" (");
            results.append(result.getPercentage());
            results.append(" %)\n");
        }
        return results.toString();
    }
}
