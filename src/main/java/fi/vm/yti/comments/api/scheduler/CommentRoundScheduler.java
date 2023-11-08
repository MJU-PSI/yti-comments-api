package fi.vm.yti.comments.api.scheduler;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Set;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import fi.vm.yti.comments.api.dao.CommentRoundDao;
import fi.vm.yti.comments.api.entity.CommentRound;
import fi.vm.yti.comments.api.service.GroupmanagementProxyService;
import static fi.vm.yti.comments.api.constants.ApiConstants.*;

@Component
public class CommentRoundScheduler {

    private static final Logger LOG = LoggerFactory.getLogger(CommentRoundScheduler.class);

    private final CommentRoundDao commentRoundDao;
    private final GroupmanagementProxyService groupmanagementProxyService;

    @Inject
    public CommentRoundScheduler(final CommentRoundDao commentRoundDao,
                                 final GroupmanagementProxyService groupmanagementProxyService) {
        this.commentRoundDao = commentRoundDao;
        this.groupmanagementProxyService = groupmanagementProxyService;
    }

    @Transactional
    @Scheduled(cron = "0 0 0 * * *", zone = "Europe/Ljubljana")
    public void updateCommentRoundStatuses() {
        updateStatuses();
    }

    @Transactional
    public void updateStatuses() {
        final ZonedDateTime zonedDateTime = ZonedDateTime.now().withZoneSameInstant(ZoneId.of("Europe/Ljubljana"));
        LOG.info("*** Scheduled checking for comment round status changes at " + zonedDateTime + " ***");
        changeRoundStatuses(zonedDateTime, STATUS_INPROGRESS, STATUS_ENDED);
        changeRoundStatuses(zonedDateTime, STATUS_AWAIT, STATUS_INPROGRESS);
        changeRoundStatuses(zonedDateTime, STATUS_INCOMPLETE, STATUS_INPROGRESS);
    }

    private void changeRoundStatuses(final ZonedDateTime runTime,
                                     final String currentStatus,
                                     final String endStatus) {
        final Set<CommentRound> commentRounds;
        final LocalDate localDate = runTime.toLocalDate();
        if (endStatus.equalsIgnoreCase(STATUS_ENDED)) {
            commentRounds = commentRoundDao.findByStatusAndEndDateBefore(currentStatus, localDate);
        } else {
            commentRounds = commentRoundDao.findByStatusAndStartDateLessThanEqual(currentStatus, localDate);
        }
        if (endStatus.equalsIgnoreCase(STATUS_INPROGRESS) && commentRounds != null && commentRounds.size() > 0) {
            commentRounds.forEach(commentRound -> groupmanagementProxyService.sendInvitationEmailsToRound(commentRound.getUri()));
        }
        if (commentRounds != null) {
            commentRounds.forEach(commentRound -> {
                commentRound.setStatus(endStatus);
                final LocalDateTime modifiedTimeStamp = runTime.withZoneSameInstant(ZoneId.of("UTC")).toLocalDateTime();
                commentRound.setModified(modifiedTimeStamp);
                commentRound.setStatusModified(modifiedTimeStamp);
            });
            commentRoundDao.saveAll(commentRounds);
            if (commentRounds.size() > 0) {
                LOG.info("Updated " + commentRounds.size() + " comment round statuses from " + currentStatus + " to " + endStatus);
            }
        }
    }
}
