package fi.vm.yti.comments.api.export.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import fi.vm.yti.comments.api.dao.CommentDao;
import fi.vm.yti.comments.api.dto.UserDTO;
import fi.vm.yti.comments.api.entity.Comment;
import fi.vm.yti.comments.api.entity.CommentRound;
import fi.vm.yti.comments.api.entity.CommentThread;
import fi.vm.yti.comments.api.entity.Organization;
import fi.vm.yti.comments.api.export.ExportService;
import fi.vm.yti.comments.api.service.ResultService;
import fi.vm.yti.comments.api.service.UserService;
import static fi.vm.yti.comments.api.constants.ApiConstants.*;
import static fi.vm.yti.comments.api.utils.StatusUtils.localizeResourceStatusToFinnish;

@Component
public class ExportServiceImpl implements ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ExportServiceImpl.class);

    private static final String DATEFORMAT = "dd/MM/yyyy";
    private static final String DATEFORMAT_WITH_MINUTES = "dd/MM/yyyy HH:mm";

    private static final String LANGUAGE_FI = "fi";
    private static final String LANGUAGE_EN = "en";
    private static final String LANGUAGE_SV = "sv";
    private static final String LANGUAGE_UND = "und";

    private final UserService userService;
    private final ResultService resultService;
    private final CommentDao commentDao;

    public ExportServiceImpl(final UserService userService,
                             final ResultService resultService,
                             final CommentDao commentDao) {
        this.userService = userService;
        this.resultService = resultService;
        this.commentDao = commentDao;
    }

    public Workbook exportCommentRoundToExcel(final CommentRound commentRound) {
        final Workbook workbook = new XSSFWorkbook();
        addCommentRoundSheet(workbook, commentRound);
        final Set<CommentThread> commentThreads = commentRound.getCommentThreads().stream().sorted(Comparator.comparing(CommentThread::getCreated)).collect(Collectors.toCollection(LinkedHashSet::new));
        addCommentThreadsSheet(workbook, commentThreads);
        addCommentsSheet(workbook, commentThreads);
        return workbook;
    }

    private void addCommentRoundSheet(final Workbook workbook,
                                      final CommentRound commentRound) {
        final Sheet sheet = workbook.createSheet(EXPORT_EXCEL_TAB_COMMENTROUND_META);
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_NAME);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_DESCRIPTION);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_STATUS);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_URI);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_ADMIN);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_ORGANIZATIONS);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_SOURCE_NAME);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_SOURCE_TYPE);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_SOURCE_URI);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_STARTDATE);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENTROUND_ENDDATE);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_CREATED);
        addCellToRow(rowhead, style, headerCellIndex, EXPORT_HEADER_MODIFIED);
        final Row row = sheet.createRow(1);
        int cellIndex = 0;
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getLabel()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getDescription()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeRoundStatusToFinnish(commentRound.getStatus())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getUri()));
        addCellToRow(row, style, cellIndex++, getUserName(commentRound.getUserId()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(getOrganizationsOfCommentRound(commentRound)));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeSourceLabelToFinnish(commentRound.getSourceLabel())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeSourceTypeToFinnish(commentRound.getSource().getContainerType())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getSource().getContainerUri()));
        addCellToRow(row, style, cellIndex++, formatDateToExport(commentRound.getStartDate()));
        addCellToRow(row, style, cellIndex++, formatDateToExport(commentRound.getEndDate()));
        addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(commentRound.getCreated()));
        addCellToRow(row, style, cellIndex, formatDateToExportWithMinutesInHelsinkiTimezone(commentRound.getModified()));
        autoSizeColumns(sheet, headerCellIndex);
    }

    private void addCommentThreadsSheet(final Workbook workbook,
                                        final Set<CommentThread> commentThreads) {
        final Sheet sheet = workbook.createSheet(EXPORT_EXCEL_TAB_RESOURCES);
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_SOURCE_LABEL);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_SOURCE_LABEL_LOCALNAME);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_DESCRIPTION);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_RESOURCE_URI);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_MAIN_COMMENTS_COUNT);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_STATUSCHANGES);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_SOURCE_ORIGINAL_STATUS);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_ADMIN_STATUS_SUGGESTION);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_ADMIN_COMMENT);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_CREATED);
        addCellToRow(rowhead, style, headerCellIndex, EXPORT_HEADER_USER);
        int rowIndex = 1;
        for (final CommentThread commentThread : commentThreads) {
            final Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            addCellToRow(row, style, cellIndex++, formatResourceLabel(commentThread.getLabel(), null));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getLocalName()));
            addCellToRow(row, style, cellIndex++, formatResourceLabel(commentThread.getDescription(), null));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getResourceUri()));
            addCellToRow(row, style, cellIndex++, Long.toString(commentDao.getCommentThreadMainCommentCount(commentThread.getId())));
            addCellToRow(row, style, cellIndex++, resultService.getResultsForCommentThreadAsTextInFinnish(commentThread.getId()));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToFinnish(commentThread.getCurrentStatus())));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToFinnish(commentThread.getProposedStatus())));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getProposedText()));
            addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(commentThread.getCreated()));
            addCellToRow(row, style, cellIndex, getUserName(commentThread.getUserId()));
        }
        autoSizeColumns(sheet, headerCellIndex);
    }

    private void addCommentsSheet(final Workbook workbook,
                                  final Set<CommentThread> commentThreads) {
        final Sheet sheet = workbook.createSheet(EXPORT_EXCEL_TAB_COMMENTS);
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_RESOURCE);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_USER);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_MAIN_LEVEL);
        final int maxLevel = getCommentsMaxLevels(commentThreads);
        int level = 2;
        while (level <= maxLevel) {
            addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_LEVEL + " " + level);
            level++;
        }
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_SUGGESTED_STATUS);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_CREATED);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_MODIFIED);
        addCellToRow(rowhead, style, headerCellIndex++, EXPORT_HEADER_COMMENT_URI);
        addCellToRow(rowhead, style, headerCellIndex, EXPORT_HEADER_RESOURCE_URI);
        final int resourceUriHeaderIndex = headerCellIndex;
        int rowIndex = 1;
        for (final CommentThread commentThread : commentThreads) {
            final Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            addCellToRow(row, createCellStyle(workbook, true), cellIndex, formatResourceLabel(commentThread.getLabel(), commentThread.getLocalName()));
            addCellToRow(row, createCellStyle(workbook, false), resourceUriHeaderIndex, commentThread.getResourceUri());
            final Map<UUID, Set<Comment>> childCommentMap = new HashMap<>();
            final Set<Comment> topLevelComments = mapMainLevelComments(commentThread, childCommentMap);
            if (!topLevelComments.isEmpty()) {
                rowIndex = addCommentRows(sheet, rowIndex, 1, maxLevel, topLevelComments, style, childCommentMap);
            }
            rowIndex++;
        }
        autoSizeColumns(sheet, headerCellIndex);
    }

    private Set<Comment> mapMainLevelComments(final CommentThread commentThread,
                                              final Map<UUID, Set<Comment>> childCommentMap) {
        final Set<Comment> mainLevelComments = new LinkedHashSet<>();
        final Set<Comment> comments = commentThread.getComments().stream().sorted(Comparator.comparing(Comment::getCreated)).collect(Collectors.toCollection(LinkedHashSet::new));
        comments.forEach(comment -> {
            final Comment parentComment = comment.getParentComment();
            if (parentComment == null) {
                mainLevelComments.add(comment);
            } else {
                Set<Comment> childComments = childCommentMap.get(parentComment.getId());
                if (childComments == null) {
                    childComments = new LinkedHashSet<>();
                }
                childComments.add(comment);
                childCommentMap.put(parentComment.getId(), childComments);
            }
        });
        return mainLevelComments;
    }

    private int getCommentsMaxLevels(final Set<CommentThread> commentThreads) {
        int maxLevel = 0;
        for (final CommentThread commentThread : commentThreads) {
            final Map<UUID, Set<Comment>> childCommentMap = new HashMap<>();
            final Set<Comment> mainLevelComments = mapMainLevelComments(commentThread, childCommentMap);
            for (final Comment mainLevelComment : mainLevelComments) {
                final int level = getChildCommentMaxLevel(childCommentMap, mainLevelComment.getId(), 1);
                if (level > maxLevel) {
                    maxLevel = level;
                }
            }
        }
        return maxLevel;
    }

    private int getChildCommentMaxLevel(final Map<UUID, Set<Comment>> childCommentMap,
                                        final UUID commentId,
                                        final int level) {
        int maxLevel = level;
        final Set<Comment> childComments = childCommentMap.get(commentId);
        if (childComments != null && !childComments.isEmpty()) {
            final int nextLevel = level + 1;
            for (final Comment comment : childComments) {
                final int commentLevel = getChildCommentMaxLevel(childCommentMap, comment.getId(), nextLevel);
                if (commentLevel > maxLevel) {
                    maxLevel = commentLevel;
                }
            }
        }
        return maxLevel;
    }

    private String getUserName(final UUID userId) {
        final UserDTO user = userService.getUserById(userId);
        if (user != null) {
            final String firstName = user.getFirstName();
            final String lastName = user.getLastName();
            if (firstName != null && !firstName.isEmpty() && lastName != null && !lastName.isEmpty()) {
                return firstName + " " + lastName;
            }
        }
        return "Poistettu käyttäjä";
    }

    private int addCommentRows(final Sheet sheet,
                               int rowIndex,
                               final int level,
                               final int maxLevel,
                               final Set<Comment> comments,
                               final CellStyle style,
                               final Map<UUID, Set<Comment>> childCommentMap) {
        for (final Comment comment : comments) {
            final Row row = sheet.createRow(rowIndex++);
            int cellIndex = 1;
            addCellToRow(row, style, cellIndex, getUserName(comment.getUserId()));
            cellIndex = cellIndex + level;
            addCellToRow(row, style, cellIndex, checkEmptyValue(comment.getContent()));
            cellIndex = 2 + maxLevel;
            if (level == 1) {
                addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToFinnish(comment.getProposedStatus())));
            } else {
                cellIndex++;
            }
            addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(comment.getCreated()));
            addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(comment.getModified()));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(comment.getUri()));
            addCellToRow(row, style, cellIndex, checkEmptyValue(comment.getCommentThread().getResourceUri()));
            final Set<Comment> childComments = childCommentMap.get(comment.getId());
            if (childComments != null && !childComments.isEmpty()) {
                rowIndex = addCommentRows(sheet, rowIndex, level + 1, maxLevel, childComments, style, childCommentMap);
            }
        }
        return rowIndex;
    }

    private CellStyle createCellStyle(final Workbook workbook) {
        return createCellStyle(workbook, false);
    }

    private CellStyle createCellStyle(final Workbook workbook,
                                      final boolean bold) {
        final CellStyle style = workbook.createCellStyle();
        style.setWrapText(true);
        if (bold) {
            final Font font = workbook.createFont();
            font.setBold(true);
            style.setFont(font);
        }
        style.setVerticalAlignment(VerticalAlignment.TOP);
        return style;
    }

    private void addCellToRow(final Row row,
                              final CellStyle style,
                              final int index,
                              final String value) {
        final Cell cell = row.createCell(index);
        cell.setCellStyle(style);
        cell.setCellValue(value);
    }

    private String checkEmptyValue(final String value) {
        if (value == null) {
            return "";
        }
        return value;
    }

    private void autoSizeColumns(final Sheet sheet,
                                 final int columnCount) {
        for (int i = 0; i <= columnCount; i++) {
            try {
                sheet.autoSizeColumn(i);
                final int columnWidth = sheet.getColumnWidth(i);
                if (columnWidth > 15000) {
                    sheet.setColumnWidth(i, 15000);
                }
            } catch (final NullPointerException e) {
                LOG.warn("Auto sizing Excel columns failed due to issue for column " + i + ", with column count: " + columnCount, e);
            }
        }
    }

    private String formatDateToExport(final LocalDate date) {
        if (date != null) {
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT);
            return date.format(formatter);
        } else {
            return "";
        }
    }

    private String formatDateToExportWithMinutesInHelsinkiTimezone(final LocalDateTime dateTime) {
        if (dateTime != null) {
            final ZoneId utcZoneId = ZoneId.of("UTC");
            final ZonedDateTime zonedDateTime = dateTime.atZone(utcZoneId);
            final ZoneId helsinkiZoneId = ZoneId.of("Europe/Helsinki");
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT_WITH_MINUTES).withZone(helsinkiZoneId);
            return zonedDateTime.format(formatter);
        } else {
            return "";
        }
    }

    private String getOrganizationsOfCommentRound(final CommentRound commentRound) {
        final Set<Organization> organizations = commentRound.getOrganizations();
        final StringBuffer buffer = new StringBuffer();
        boolean first = true;
        if (organizations != null && !organizations.isEmpty()) {
            for (final Organization organization : organizations) {
                if (!first) {
                    buffer.append(", ");
                } else {
                    first = false;
                }
                String organizationName = organization.getPrefLabel(LANGUAGE_FI);
                if (organizationName == null) {
                    organizationName = organization.getPrefLabel(LANGUAGE_EN);
                }
                if (organizationName == null) {
                    organizationName = organization.getPrefLabel(LANGUAGE_SV);
                }
                buffer.append(organizationName);
            }
        } else {
            buffer.append("-");
        }
        return buffer.toString();
    }

    private String localizeSourceLabelToFinnish(final Map<String, String> sourceLabel) {
        String label = sourceLabel.get(LANGUAGE_FI);
        if (label == null) {
            sourceLabel.get(LANGUAGE_EN);
        }
        if (label == null) {
            sourceLabel.get(LANGUAGE_SV);
        }
        return label;
    }

    private String localizeSourceTypeToFinnish(final String type) {
        switch (type) {
            case "codelist": {
                return "koodisto";
            }
            case "terminology": {
                return "sanasto";
            }
            case "datamodel": {
                return "tietomalli";
            }
            case "library": {
                return "tietokomponenttikirjasto";
            }
            case "profile": {
                return "soveltamisprofiili";
            }
            case "commentround": {
                return "kommentointikierros";
            }
            default: {
                return type;
            }
        }
    }

    private String localizeRoundStatusToFinnish(final String status) {
        switch (status) {
            case "INPROGRESS": {
                return "Käynnissä";
            }
            case "ENDED": {
                return "Päättynyt";
            }
            case "INCOMPLETE": {
                return "Keskeneräinen";
            }
            case "AWAIT": {
                return "Odottaa";
            }
            default: {
                return status;
            }
        }
    }

    private String formatResourceLabel(final Map<String, String> label, final String localName) {
        String labelOut = "";
        for (Map.Entry<String, String> pair : label.entrySet()) {
            if (labelOut.length() > 0) {
                labelOut += "\n";
            }
            labelOut += pair.getKey().toUpperCase() + ": " + pair.getValue();
        }
        if (localName != null) {
            labelOut += "\nlocalName: " + localName;
        }        
        return labelOut;
    }
}
