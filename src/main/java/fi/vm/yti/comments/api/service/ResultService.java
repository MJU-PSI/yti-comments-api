package fi.vm.yti.comments.api.service;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.comments.api.dto.CommentThreadResultDTO;

public interface ResultService {

    Set<CommentThreadResultDTO> getResultsForCommentThread(final UUID commentThreadId);

    String getResultsForCommentThreadAsTextInDefaultLanguage(final UUID commentThreadId);
}
