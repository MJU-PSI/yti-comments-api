package fi.vm.yti.comments.api.export.impl;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;

import fi.vm.yti.comments.api.configuration.CommentsApiProperties;
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
import static fi.vm.yti.comments.api.utils.StatusUtils.localizeResourceStatusToDefaultLanguage;

@Component
public class ExportServiceImpl implements ExportService {

    private static final Logger LOG = LoggerFactory.getLogger(ExportServiceImpl.class);

    private static final String DATEFORMAT = "dd/MM/yyyy";
    private static final String DATEFORMAT_WITH_MINUTES = "dd/MM/yyyy HH:mm";

    private String exportLanguage = LANGUAGE_CODE_EN;

    private final UserService userService;
    private final ResultService resultService;
    private final CommentDao commentDao;
    
    @Autowired
    private MessageSource messageSource;

    public ExportServiceImpl(final UserService userService,
                             final ResultService resultService,
                             final CommentDao commentDao,
                             final CommentsApiProperties commentsApiProperties) {
        this.userService = userService;
        this.resultService = resultService;
        this.commentDao = commentDao;
    }

    public Workbook exportCommentRoundToExcel(final CommentRound commentRound, final String lang) {
        this.exportLanguage = lang;
        final Workbook workbook = new XSSFWorkbook();
        addCommentRoundSheet(workbook, commentRound);
        final Set<CommentThread> commentThreads = commentRound.getCommentThreads().stream().sorted(Comparator.comparing(CommentThread::getCreated)).collect(Collectors.toCollection(LinkedHashSet::new));
        addCommentThreadsSheet(workbook, commentThreads);
        addCommentsSheet(workbook, commentThreads);
        return workbook;
    }

    private void addCommentRoundSheet(final Workbook workbook,
                                      final CommentRound commentRound) {
        final Sheet sheet = workbook.createSheet(messageSource.getMessage("l19", null, Locale.forLanguageTag(this.exportLanguage)));
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l22", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l23", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l24", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l25", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l26", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l27", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l28", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l29", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l30", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l31", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l32", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l33", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex, messageSource.getMessage("l34", null, Locale.forLanguageTag(this.exportLanguage)));
        final Row row = sheet.createRow(1);
        int cellIndex = 0;
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getLabel()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getDescription()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeRoundStatus(commentRound.getStatus())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getUri()));
        addCellToRow(row, style, cellIndex++, getUserName(commentRound.getUserId()));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(getOrganizationsOfCommentRound(commentRound)));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeSourceLabel(commentRound.getSourceLabel())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeSourceType(commentRound.getSource().getContainerType())));
        addCellToRow(row, style, cellIndex++, checkEmptyValue(commentRound.getSource().getContainerUri()));
        addCellToRow(row, style, cellIndex++, formatDateToExport(commentRound.getStartDate()));
        addCellToRow(row, style, cellIndex++, formatDateToExport(commentRound.getEndDate()));
        addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(commentRound.getCreated()));
        addCellToRow(row, style, cellIndex, formatDateToExportWithMinutesInHelsinkiTimezone(commentRound.getModified()));
        autoSizeColumns(sheet, headerCellIndex);
    }

    private void addCommentThreadsSheet(final Workbook workbook,
                                        final Set<CommentThread> commentThreads) {
        final Sheet sheet = workbook.createSheet(messageSource.getMessage("l20", null, Locale.forLanguageTag(this.exportLanguage)));
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l36", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l35", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l37", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l38", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l39", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l40", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l41", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l42", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l43", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l33", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex, messageSource.getMessage("l45", null, Locale.forLanguageTag(this.exportLanguage)));
        int rowIndex = 1;
        for (final CommentThread commentThread : commentThreads) {
            final Row row = sheet.createRow(rowIndex++);
            int cellIndex = 0;
            addCellToRow(row, style, cellIndex++, formatResourceLabel(commentThread.getLabel(), null));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getLocalName()));
            addCellToRow(row, style, cellIndex++, formatResourceLabel(commentThread.getDescription(), null));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getResourceUri()));
            addCellToRow(row, style, cellIndex++, Long.toString(commentDao.getCommentThreadMainCommentCount(commentThread.getId())));
            addCellToRow(row, style, cellIndex++, resultService.getResultsForCommentThreadAsTextInDefaultLanguage(commentThread.getId()));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToDefaultLanguage(commentThread.getCurrentStatus(), messageSource, Locale.forLanguageTag(this.exportLanguage))));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToDefaultLanguage(commentThread.getProposedStatus(), messageSource, Locale.forLanguageTag(this.exportLanguage))));
            addCellToRow(row, style, cellIndex++, checkEmptyValue(commentThread.getProposedText()));
            addCellToRow(row, style, cellIndex++, formatDateToExportWithMinutesInHelsinkiTimezone(commentThread.getCreated()));
            addCellToRow(row, style, cellIndex, getUserName(commentThread.getUserId()));
        }
        autoSizeColumns(sheet, headerCellIndex);
    }

    private void addCommentsSheet(final Workbook workbook,
                                  final Set<CommentThread> commentThreads) {
        final Sheet sheet = workbook.createSheet(messageSource.getMessage("l21", null, Locale.forLanguageTag(this.exportLanguage)));
        final Row rowhead = sheet.createRow((short) 0);
        int headerCellIndex = 0;
        final CellStyle style = createCellStyle(workbook);
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l44", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l45", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l46", null, Locale.forLanguageTag(this.exportLanguage)));
        final int maxLevel = getCommentsMaxLevels(commentThreads);
        int level = 2;
        while (level <= maxLevel) {
            addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l47", null, Locale.forLanguageTag(this.exportLanguage)) + " " + level);
            level++;
        }
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l48", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l33", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l34", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex++, messageSource.getMessage("l49", null, Locale.forLanguageTag(this.exportLanguage)));
        addCellToRow(rowhead, style, headerCellIndex, messageSource.getMessage("l38", null, Locale.forLanguageTag(this.exportLanguage)));
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
        return messageSource.getMessage("l18", null, Locale.forLanguageTag(this.exportLanguage));
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
                addCellToRow(row, style, cellIndex++, checkEmptyValue(localizeResourceStatusToDefaultLanguage(comment.getProposedStatus(), messageSource, Locale.forLanguageTag(this.exportLanguage))));
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
            final ZoneId ljubljanaZoneId = ZoneId.of("Europe/Ljubljana");
            final DateTimeFormatter formatter = DateTimeFormatter.ofPattern(DATEFORMAT_WITH_MINUTES).withZone(ljubljanaZoneId);
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

                buffer.append(organization.getPrefLabel(this.exportLanguage));
                
            }
        } else {
            buffer.append("-");
        }
        return buffer.toString();
    }

    private String localizeSourceLabel(final Map<String, String> sourceLabel) {
        String label = sourceLabel.get(this.exportLanguage);
        if (StringUtils.isEmpty(label)) {
            Iterator<Map.Entry<String,String>> iterator = sourceLabel.entrySet().iterator();
            while(iterator.hasNext()) {
                Map.Entry<String, String> entry = iterator.next();
                if (!StringUtils.isEmpty(entry.getValue())) {
                    return entry.getValue() + "(" + entry.getKey() +  ")";
                }
            }
        }
        return label;
    }

    private String localizeSourceType(final String type) {
        switch (type) {
            case "codelist": {
                return messageSource.getMessage("l1", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "terminology": {
                return messageSource.getMessage("l2", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "datamodel": {
                return messageSource.getMessage("l3", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "library": {
                return messageSource.getMessage("l4", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "profile": {
                return messageSource.getMessage("l5", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "commentround": {
                return messageSource.getMessage("l6", null, Locale.forLanguageTag(this.exportLanguage));
            }
            default: {
                return type;
            }
        }
    }

    private String localizeRoundStatus(final String status) {
        switch (status) {
            case "INPROGRESS": {
                return messageSource.getMessage("l7", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "ENDED": {
                return messageSource.getMessage("l8", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "INCOMPLETE": {
                return messageSource.getMessage("l9", null, Locale.forLanguageTag(this.exportLanguage));
            }
            case "AWAIT": {
                return messageSource.getMessage("l10", null, Locale.forLanguageTag(this.exportLanguage));
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
