package fi.vm.yti.comments.api.constants;

public abstract class ApiConstants {

    public static final String API_VERSION_V1 = "v1";
    public static final String API_CONTEXT_PATH = "comments-api";
    public static final String API_BASE_PATH = "api";
    public static final String API_PATH_COMMENTS = "comments";
    public static final String API_PATH_SOURCES = "sources";
    public static final String API_PATH_THREADS = "threads";
    public static final String API_PATH_COMMENTROUNDS = "commentrounds";
    public static final String API_PATH_COMMENTTHREADS = "commentthreads";
    public static final String CODELIST_API_CONTEXT_PATH = "codelist-api";
    public static final String DATAMODEL_API_CONTEXT_PATH = "datamodel-api";
    public static final String TERMINOLOGY_API_CONTEXT_PATH = "terminology-api";
    public static final String API_INTEGRATION = "integration";
    public static final String API_CONTAINERS = "containers";
    public static final String API_RESOURCES = "resources";
    public static final String GROUPMANAGEMENT_API_CONTEXT_PATH = "public-api";
    public static final String GROUPMANAGEMENT_API_PRIVATE_CONTEXT_PATH = "private-api";
    public static final String GROUPMANAGEMENT_API_USERS = "users";
    public static final String GROUPMANAGEMENT_API_REQUESTS = "requests";
    public static final String GROUPMANAGEMENT_API_REQUEST = "/request";
    public static final String GROUPMANAGEMENT_API_ORGANIZATIONS = "organizations";
    public static final String GROUPMANAGEMENT_API_TEMPUSERS = "tempusers";
    public static final String GROUPMANAGEMENT_API_SENDCONTAINEREMAILS = "sendcontaineremails";
    public static final String FILTER_NAME_COMMENT = "comment";
    public static final String FILTER_NAME_SOURCE = "source";
    public static final String FILTER_NAME_COMMENTROUND = "commentRound";
    public static final String FILTER_NAME_COMMENTTHREAD = "commentThread";
    public static final String FILTER_NAME_ORGANIZATION = "organization";
    public static final String FILTER_NAME_COMMENTROUNDORGANIZATION = "commentRoundGroup";
    public static final String FIELD_NAME_ID = "id";
    public static final String LANGUAGE_CODE_EN = "en";
    public static final String FORMAT_JSON = "json";
    public static final String FORMAT_EXCEL = "excel";
    public static final String STATUS_INCOMPLETE = "INCOMPLETE";
    public static final String STATUS_AWAIT = "AWAIT";
    public static final String STATUS_INPROGRESS = "INPROGRESS";
    public static final String STATUS_ENDED = "ENDED";
    public static final String HEADER_CONTENT_DISPOSITION = "content-disposition";

    public static final String EXPORT_EXCEL_TAB_COMMENTROUND_META = "Kierroksen tiedot";
    public static final String EXPORT_EXCEL_TAB_RESOURCES = "Tietosisällöt";
    public static final String EXPORT_EXCEL_TAB_COMMENTS = "Kommentit";

    public static final String EXPORT_HEADER_COMMENTROUND_NAME = "KIERROKSEN NIMI";
    public static final String EXPORT_HEADER_COMMENTROUND_DESCRIPTION = "KIERROKSEN KUVAUS";
    public static final String EXPORT_HEADER_COMMENTROUND_STATUS = "KIERROKSEN TILA";
    public static final String EXPORT_HEADER_COMMENTROUND_URI = "KIERROKSEN URI";
    public static final String EXPORT_HEADER_COMMENTROUND_ADMIN = "KIERROKSEN YLLÄPITÄJÄ";
    public static final String EXPORT_HEADER_COMMENTROUND_ORGANIZATIONS = "ORGANISAATIO(T)";
    public static final String EXPORT_HEADER_COMMENTROUND_SOURCE_NAME = "LÄHTEEN NIMI";
    public static final String EXPORT_HEADER_COMMENTROUND_SOURCE_TYPE = "LÄHTEEN TYYPPI";
    public static final String EXPORT_HEADER_COMMENTROUND_SOURCE_URI = "LÄHTEEN URI";
    public static final String EXPORT_HEADER_COMMENTROUND_STARTDATE = "ALKUPÄIVÄ";
    public static final String EXPORT_HEADER_COMMENTROUND_ENDDATE = "PÄÄTTYMISPÄIVÄ";
    public static final String EXPORT_HEADER_CREATED = "LUOTU";
    public static final String EXPORT_HEADER_MODIFIED = "MUOKATTU";
    public static final String EXPORT_HEADER_SOURCE_LABEL_LOCALNAME = "TIETOSISÄLTÖ TUNNISTE";
    public static final String EXPORT_HEADER_SOURCE_LABEL = "TIETOSISÄLTÖ";
    public static final String EXPORT_HEADER_DESCRIPTION = "KUVAUS";
    public static final String EXPORT_HEADER_RESOURCE_URI = "TIETOSISÄLLÖN URI";
    public static final String EXPORT_HEADER_MAIN_COMMENTS_COUNT = "PÄÄTASON KOMMENTTEJA YHTEENSÄ";
    public static final String EXPORT_HEADER_STATUSCHANGES = "TILAMUUTOKSET";
    public static final String EXPORT_HEADER_SOURCE_ORIGINAL_STATUS = "AINEISTON ALKUPERÄINEN TILA";
    public static final String EXPORT_HEADER_ADMIN_STATUS_SUGGESTION = "YLLÄPITÄJÄN TILAEHDOTUS";
    public static final String EXPORT_HEADER_ADMIN_COMMENT = "YLLÄPITÄJÄN KOMMENTTI";
    public static final String EXPORT_HEADER_RESOURCE = "TIETOSISÄLTÖ";
    public static final String EXPORT_HEADER_USER = "KÄYTTÄJÄ";
    public static final String EXPORT_HEADER_MAIN_LEVEL = "PÄÄTASO";
    public static final String EXPORT_HEADER_LEVEL = "TASO";
    public static final String EXPORT_HEADER_SUGGESTED_STATUS = "EHDOTETTU TILA";
    public static final String EXPORT_HEADER_COMMENT_URI = "KOMMENTIN URI";

    private ApiConstants() {
    }
}
