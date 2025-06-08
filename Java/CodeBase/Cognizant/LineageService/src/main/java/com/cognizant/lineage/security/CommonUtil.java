package com.cognizant.lineage.security;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;

/**
 * The CommonUtil
 *
 */
public class CommonUtil {

    /**
     * get current UTC timestamp
     * @return LocalDateTime
     */
    public static LocalDateTime currentUtcTime() {
        final ZoneId UTC = ZoneId.of("UTC");
        LocalDateTime localDateTime = LocalDateTime.now(UTC);
        return localDateTime;
    }

    /**
     * LocalDateTime (current UTC) to java.sql.Timestamp
     * @return java.sql.Timestamp
     */
    public static Timestamp currentUtcTimestamp() {
        final ZoneId UTC = ZoneId.of("UTC");
        LocalDateTime localDateTime = LocalDateTime.now(UTC);
        return Timestamp.valueOf(localDateTime);
    }

    /**
     * convert  java.sql.Timestamp To LocalDateTime
     * @param ts
     * @return LocalDateTime
     */
    public static LocalDateTime convertTimestampToLocalDateTime(Timestamp ts) {
        if(ts!=null){
            return ts.toLocalDateTime();
        }
        return null;
    }

    /**
     * add Days To Current Utc LocalDateTime
     * @param days
     * @return LocalDateTime
     */
    public static LocalDateTime addDaysToCurrentUtcTimestamp(long days) {
        return currentUtcTime().plusDays(days);
    }

    /**
     * add Days To Current Utc LocalDateTime
     * @param days
     * @return java.sql.Timestamp
     */
    public static Timestamp addDaysToCurrentUtcTimestampSql(long days) {
        return Timestamp.valueOf(currentUtcTime().plusDays(days));
    }

    /**
     * Convert a java.time.LocalDateTime to a String
     * @param localDateTime
     * @return String
     */
    public static String formatLocalDateTimeToString(LocalDateTime localDateTime) {
        if(localDateTime != null){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM/dd/yyyy hh:mm a");
            return localDateTime.format(formatter);
        }
        return null;
    }
    /**
     * Convert a String to a java.time.LocalDateTime
     * @param stringDate, stringFormat
     * @return LocalDateTime
     */
    public static LocalDateTime convertStringToLocalDateTime(String stringDate, String stringFormat) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(stringFormat);
        LocalDateTime dateTime = LocalDateTime.parse(stringDate, formatter);
        return dateTime;
    }

    /**
     * Concert a ZonedDateTime to String
     * @param zonedDateTime
     * @return
     */
    public static String formatZonedLocalDateTimeToString(ZonedDateTime zonedDateTime) {
        if(zonedDateTime != null){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            return zonedDateTime.format(formatter);
        }
        return null;
    }
    /**
     * Concert a ZonedDateTime to String
     * @param zonedDateTime
     * @return
     */
    public static String formatZonedLocalDateTimeToString2(ZonedDateTime zonedDateTime) {
        if(zonedDateTime != null){

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MM-dd-yyyy HH:mm");
            return zonedDateTime.format(formatter);
        }
        return null;
    }

    /**
     * Convert UTC LocalDateTime To system default ZonedDateTime
     * @param timestamp
     * @return ZonedDateTime
     */
    public static ZonedDateTime convertUtcTimestampToSystemDefaultZonedDateTime(LocalDateTime localDateTime) {
        if(localDateTime != null) {
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
                    .withZoneSameInstant(ZoneId.systemDefault());

            return zonedDateTime;
        }
        return null;
    }

    /**
     * Convert UTC LocalDateTime To given ZonedDateTime
     * @param timestamp
     * @param toTimeZone
     * @return
     */
    public static ZonedDateTime convertUtcTimestampToZonedDateTime(LocalDateTime localDateTime,
                                                                   int timezoneOffsetInMinutes) {
        if(localDateTime != null) {
            ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(timezoneOffsetInMinutes*60);
            ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
                    .withZoneSameInstant(zoneOffset.normalized());

            return zonedDateTime;
        }
        return null;
    }

    /**
     * Checking application License Expire date
     * @return
     */
    public static Boolean isApplicationLicenseExpired() {
        boolean bool = false;
        try {
            LocalDateTime applicationLicenseExpireDate = convertStringToLocalDateTime(
                    ApplicationConstant.APPLICATION_LICENSE_EXPIRY_DATE,
                    ApplicationConstant.APPLICATION_LICENSE_EXPIRY_DATE_FORMAT);
            if(currentUtcTime().isAfter(applicationLicenseExpireDate)) {
                bool = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return bool;
    }

    /**
     public static String correctName (String name) {

     if(StringUtils.isNoneBlank(name) && StringUtils.containsWhitespace(name)) {
     return "\"" + name + "\"";
     }

     return name;
     }
     **/

    /**
     * Name that contains any characters except Alphanumeric, Underscore
     * then it is covered by "". If the name start with any digit the it
     * is covered by "".
     * @param name
     * @return
     */
    /**	public static String correctName (String name) {

     String returnValue = name;
     if(StringUtils.isNoneBlank(name)) {

     try {
     String regex = "^[a-zA-Z0-9_]+$"; // Is Alphanumeric, Underscore only
     Pattern p = Pattern.compile(regex);
     Matcher m = p.matcher(name);

     if(m.matches()) {
     regex = "^[0-9].*$"; // Check Starts with a digit
     p = Pattern.compile(regex);
     m = p.matcher(name);
     if(m.matches()) {
     returnValue = "\"" + name + "\"";
     }
     }
     else if(name.startsWith("\"") && name.endsWith("\"")) {
     returnValue = name;
     }
     else {
     returnValue = "\"" + name + "\"";
     }
     }
     catch(Exception e) {e.printStackTrace();}
     }

     return returnValue;
     }
     **/
    public static String correctName (String name) {

        if(StringUtils.isBlank(name))
            return name;
        name = name.trim();
        if(name.length() > 50) {
            name = StringUtils.left(name, 50);
            name = name.trim();
        }

        return "\"" + name + "\"";

    }

/**	public static void main(String args []) {
 Timestamp timestamp = currentUtcTimestamp();
 LocalDateTime localDateTime = timestamp.toLocalDateTime();
 System.out.println(localDateTime);
 ZonedDateTime zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"));
 System.out.println(zonedDateTime);

 zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
 .withZoneSameInstant(ZoneId.of("Europe/Paris"));
 System.out.println(zonedDateTime);

 zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
 .withZoneSameInstant(ZoneId.of("Asia/Calcutta"));
 System.out.println(zonedDateTime);

 System.out.println(ZoneId.systemDefault());

 ZoneOffset zoneOffset = ZoneOffset.ofTotalSeconds(-8*60*60);
 System.out.println(zoneOffset.normalized());

 zonedDateTime = ZonedDateTime.of(localDateTime, ZoneId.of("UTC"))
 .withZoneSameInstant(zoneOffset.normalized());
 System.out.println(zonedDateTime);

 System.out.println(correctName ("﻿Contact                                                              ID"));

 }
 **/
}