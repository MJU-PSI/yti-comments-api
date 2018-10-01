package fi.vm.yti.comments.api.dao;

import java.util.Set;
import java.util.UUID;

import fi.vm.yti.comments.api.dto.CommentRoundDTO;
import fi.vm.yti.comments.api.entity.CommentRound;

public interface CommentRoundDao {

    Set<CommentRound> findAll();

    Set<CommentRound> findByOrganizationsIdAndStatus(final UUID organizationId,
                                                     final String status);

    Set<CommentRound> findByOrganizationsId(final UUID organizationId);

    Set<CommentRound> findByStatus(final String status);

    Set<CommentRound> findBySourceContainerType(final String containerType);

    Set<CommentRound> findByOrganizationsIdAndStatusAndSourceContainerType(final UUID organizationId,
                                                                           final String status,
                                                                           final String containerType);

    Set<CommentRound> findByOrganizationsIdAndSourceContainerType(final UUID organizationId,
                                                                  final String containerType);

    Set<CommentRound> findByStatusAndSourceContainerType(final String status,
                                                         final String containerType);

    CommentRound findById(final UUID commentRoundId);

    CommentRound addOrUpdateCommentRoundFromDto(final CommentRoundDTO commentRoundDto);

    Set<CommentRound> addOrUpdateCommentRoundsFromDtos(final Set<CommentRoundDTO> commentRoundDtos);
}
