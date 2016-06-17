package uk.co.informaticslab.filetos3.utils;

import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.ISODateTimeFormat;

/**
 * Created by tom on 17/06/2016.
 */
public final class Constants {

    public static final DateTimeFormatter DTF = ISODateTimeFormat.dateTimeNoMillis();

    private Constants() {

    }
}
