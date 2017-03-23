package com.rollncode.basement.interfaces;

import java.nio.charset.Charset;

@SuppressWarnings("unused")
public @interface SharedStrings {

    //STRING
    String EMPTY = "";
    String NULL = "null";
    String SPACE = " ";
    String SLASH = "/";
    String SHARP = "#";
    String EQUALS = "=";
    String ZERO = "0";
    String ONE = "1";
    String COMMA = ",";
    String QUESTION_SYMBOL = "?";
    String AND_SYMBOL = "&";
    String PLUS = "+";
    String QUOTE = "\'";
    String QUOTE_DOUBLE = "\"";
    String DOT = ".";
    String DOTS = "…";
    String COLON = ":";
    String FILE_PREFIX = "file://";

    //CHAR
    char LESS_C = '<';
    char MORE_C = '>';
    char NEW_LINE_C = '\n';
    char TAB_C = '\t';
    char SPACE_C = ' ';
    char COMMA_C = ',';
    char COLON_C = ':';
    char PLUS_C = '+';
    char QUESTION_C = '?';
    char EQUALS_C = '=';
    char AND_C = '&';
    char QUOTE_C = '\'';
    char BRACKET_OPEN_C = '(';
    char BRACKET_CLOSE_C = ')';
    char DASH_C = '-';
    char STAR_C = '*';
    char DOT_C = '.';

    //SQL
    String LIMIT_1 = " LIMIT 1";
    String LIKE = " LIKE ";
    String NOT_EQUALS = "<>";
    String AND = " AND ";
    String OR = " OR ";
    String ASC = " ASC ";
    String DESC = " DESC ";
    String IN = " IN ";
    String NOT_IN = " NOT IN ";
    String IS_NULL = " IS NULL ";
    String IS_NOT_NULL = " IS NOT NULL ";
    String SELECT = " SELECT ";
    String FROM = " FROM ";
    String WHERE = " WHERE ";
    String REGEXP = " REGEXP ";
    String MORE_THAN_ZERO = " > 0 ";
    String EQUALS_ZERO = " = 0 ";

    //MIME TYPE's
    String MIME_ALL = "*/*";
    String MIME_IMAGE = "image/*";
    String MIME_VIDEO = "video/*";
    String MIME_TEXT = "text/*";
    String MIME_AUDIO = "audio/*";
    String MIME_JPEG = "image/jpeg";
    String MIME_PNG = "image/png";
    String MIME_PLAIN = "text/plain";
    String MIME_HTML = "text/html";

    String TYPE_JPG = "jpg";
    String TYPE_MP4 = "mp4";

    String TYPE_DOT_JPG = ".jpg";

    //OTHER
    String GMT = "GMT";
    String MAIL_TO = "mailto";
    String UTF_8 = "UTF-8";
    String DATA = "data";
    String VIDEO = "video";

    String PUT = "PUT";
    String POST = "POST";
    String GET = "GET";

    // CHARSET
    Charset UTF_8_CHARSET = Charset.forName(UTF_8);
    String TEL = "tel";
    String SMS = "sms";
}