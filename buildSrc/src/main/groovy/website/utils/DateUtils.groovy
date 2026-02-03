/*
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */
package website.utils

import groovy.transform.CompileStatic

import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException

import org.gradle.api.GradleException

@CompileStatic
class DateUtils {

    public static final DateTimeFormatter MMM_D_YYYY_HHMM = DateTimeFormatter.ofPattern('MMM d, yyyy HH:mm', Locale.ENGLISH)
    public static final DateTimeFormatter MMM_D_YYYY = DateTimeFormatter.ofPattern('MMM d, yyyy', Locale.ENGLISH)
    public static final DateTimeFormatter MMMM_D_YYYY = DateTimeFormatter.ofPattern('MMMM d, yyyy', Locale.ENGLISH)
    public static final DateTimeFormatter D_MMM_YYYY = DateTimeFormatter.ofPattern('d MMM yyyy', Locale.ENGLISH)
    public static final DateTimeFormatter D_MMMM_YYYY = DateTimeFormatter.ofPattern('d MMMM yyyy', Locale.ENGLISH)

    static Date parseDate(String date) {
        if (date == null) { throw new GradleException('Date cannot be null') }

        // Try MMM d, yyyy HH:mm (e.g., "Jan 15, 2024 14:30")
        try { return parse(date, MMM_D_YYYY_HHMM) }
        catch (DateTimeParseException ignore) {}

        // Try MMM d, yyyy (e.g., "Jan 15, 2024")
        try { return parse(date, MMM_D_YYYY) }
        catch (DateTimeParseException ignore) {}

        // Try MMMM d, yyyy (e.g., "March 1, 2015")
        try { return parse(date, MMMM_D_YYYY) }
        catch (DateTimeParseException ignore) {}

        // Try d MMM YYYY (e.g., "11 Nov 2016")
        try { return parse(date, D_MMM_YYYY) }
        catch (DateTimeParseException ignore) {}

        // Try d MMMM YYYY (e.g., "11 November 2016")
        try { return parse(date, D_MMMM_YYYY) }
        catch (DateTimeParseException ignore) {}

        throw new GradleException("Could not parse date [$date]")
    }

    private static Date parse(String date, DateTimeFormatter fmt) throws DateTimeParseException {
        def zone = ZoneId.systemDefault()
        def parsed = fmt.parseBest(date, LocalDateTime.&from, LocalDate.&from)
        if (parsed instanceof LocalDateTime) {
            return Date.from(((LocalDateTime) parsed).atZone(zone).toInstant())
        }
        return Date.from(((LocalDate) parsed).atStartOfDay(zone).toInstant())
    }

    /**
     * Format a date using MMMM d, yyyy pattern (e.g., "January 15, 2024")
     */
    static String format_MMMM_D_YYYY(Date date) {
        LocalDate localDate = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
        return localDate.format(MMMM_D_YYYY)
    }

    /**
     * Format a date using MMM d, yyyy HH:mm pattern (e.g., "Jan 15, 2024 14:30")
     */
    static String format_MMM_D_YYYY_HHMM(Date date) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
        return localDateTime.format(MMM_D_YYYY_HHMM)
    }

    /**
     * Format a LocalDateTime using MMM d, yyyy pattern (e.g., "Jan 15, 2024")
     */
    static String format_MMM_D_YYYY(LocalDateTime dateTime) {
        return dateTime?.format(MMM_D_YYYY) ?: ''
    }
}
