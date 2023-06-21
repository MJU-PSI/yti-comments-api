package fi.vm.yti.comments.api.utils;

import java.util.Locale;

import org.springframework.context.MessageSource;

public interface StatusUtils {

    public MessageSource getMessageSource();

    static String localizeResourceStatusToDefaultLanguage(final String status, final MessageSource messageSource, Locale locale) {
        if (status != null) {
            switch (status) {
                case "VALID": {
                    return messageSource.getMessage("l11", null, locale);
                }
                case "DRAFT": {
                    return messageSource.getMessage("l12", null, locale);
                }
                case "SUPERSEDED": {
                    return messageSource.getMessage("l13", null, locale);
                }
                case "INVALID": {
                    return messageSource.getMessage("l14", null, locale);
                }
                case "RETIRED": {
                    return messageSource.getMessage("l15", null, locale);
                }
                case "INCOMPLETE": {
                    return messageSource.getMessage("l16", null, locale);
                }
                case "SUGGESTED": {
                    return messageSource.getMessage("l17", null, locale);
                }
                default: {
                    return status;
                }
            }
        } else {
            return null;
        }
    }}
