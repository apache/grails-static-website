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
package org.grails.utils

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

    static Date parseDate(String date) {
        if (date == null) {
            throw new GradleException('Date cannot be null')
        }
        // Try MMM d, yyyy HH:mm (e.g., "Jan 15, 2024 14:30")
        try {
            def dateTime = LocalDateTime.parse(date, MMM_D_YYYY_HHMM)
            return Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant())
        } catch (DateTimeParseException ignore) {}

        // Try MMM d, yyyy (e.g., "Jan 15, 2024")
        try {
            def localDate = LocalDate.parse(date, MMM_D_YYYY)
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } catch (DateTimeParseException ignore) {}

        // Try MMMM d, yyyy (e.g., "March 1, 2015")
        try {
            def localDate = LocalDate.parse(date, MMMM_D_YYYY)
            return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant())
        } catch (DateTimeParseException ignore) {}

        throw new GradleException("Could not parse date $date")
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
}
