package fi.vm.yti.comments.api.dao;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

import org.springframework.data.domain.PageRequest;

import fi.vm.yti.comments.api.dto.CommentRoundDTO;
import fi.vm.yti.comments.api.entity.CommentRound;

public interface CommentRoundDao {

    void save(final CommentRound commentRound);

    void saveAll(final Set<CommentRound> commentRounds);

    Set<CommentRound> findAll();

    Set<CommentRound> findAll(final PageRequest pageRequest);

    Set<CommentRound> findByOrganizationsIdAndStatus(final UUID organizationId,
                                                     final String status);

    Set<CommentRound> findByOrganizationsId(final UUID organizationId);

    Set<CommentRound> findByStatus(final String status);

    Set<CommentRound> findByStatusAndEndDateBefore(final String status,
                                                   final LocalDate now);

    Set<CommentRound> findByStatusAndStartDateLessThanEqual(final String status,
                                                            final LocalDate now);

    Set<CommentRound> findBySourceContainerType(final String containerType);

    Set<CommentRound> findByOrganizationsIdAndStatusAndSourceContainerType(final UUID organizationId,
                                                                           final String status,
                                                                           final String containerType);

    Set<CommentRound> findByOrganizationsIdAndSourceContainerType(final UUID organizationId,
                                                                  final String containerType);

    Set<CommentRound> findByStatusAndSourceContainerType(final String status,
                                                         final String containerType);

    CommentRound findById(final UUID commentRoundId);

    CommentRound findByIdentifier(final String commentRoundIdentifier);

    CommentRound findBySequenceId(final Integer sequenceId);

    CommentRound addOrUpdateCommentRoundFromDto(final CommentRoundDTO commentRoundDto,
                                                final boolean removeCommentThreadOrphans);

    Set<CommentRound> addOrUpdateCommentRoundsFromDtos(final Set<CommentRoundDTO> commentRoundDtos,
                                                       final boolean removeCommentThreadOrphans);

    void deleteCommentRound(final CommentRound commentRound);

    Set<CommentRound> findByUriIn(final Set<String> uris,
                                  final LocalDateTime after,
                                  final LocalDateTime before,
                                  final PageRequest pageRequest);

    void updateContentModified(final UUID commentRoundId,
                               final LocalDateTime timeStamp);

    int getCommentRoundCount(final Set<String> commentRoundUris,
                             final LocalDateTime after,
                             final LocalDateTime before);
}
