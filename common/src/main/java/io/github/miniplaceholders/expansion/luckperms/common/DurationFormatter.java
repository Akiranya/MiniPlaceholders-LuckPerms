/*
 * This file is part of LuckPerms, licensed under the MIT License.
 *
 *  Copyright (c) lucko (Luck) <luck@lucko.me>
 *  Copyright (c) contributors
 *
 *  Permission is hereby granted, free of charge, to any person obtaining a copy
 *  of this software and associated documentation files (the "Software"), to deal
 *  in the Software without restriction, including without limitation the rights
 *  to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  copies of the Software, and to permit persons to whom the Software is
 *  furnished to do so, subject to the following conditions:
 *
 *  The above copyright notice and this permission notice shall be included in all
 *  copies or substantial portions of the Software.
 *
 *  THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  SOFTWARE.
 */

package io.github.miniplaceholders.expansion.luckperms.common;

import com.google.common.collect.ImmutableMap;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import java.util.Map;

// Copied from https://github.com/lucko/LuckPerms/blob/master/common/src/main/java/me/lucko/luckperms/common/util/DurationFormatter.java
// Uses String instead of Chat Components

/**
 * Formats durations to a readable form
 */
class DurationFormatter {
    public static final DurationFormatter LONG = new DurationFormatter(false);
    public static final DurationFormatter YEARS = new DurationFormatter(true, ChronoUnit.YEARS);
    public static final DurationFormatter MONTHS = new DurationFormatter(true, ChronoUnit.MONTHS);
    public static final DurationFormatter WEEKS = new DurationFormatter(true, ChronoUnit.WEEKS);
    public static final DurationFormatter DAYS = new DurationFormatter(true, ChronoUnit.DAYS);
    public static final DurationFormatter HOURS = new DurationFormatter(true, ChronoUnit.HOURS);
    public static final DurationFormatter MINUTES = new DurationFormatter(true, ChronoUnit.MINUTES);
    public static final DurationFormatter SECONDS = new DurationFormatter(true, ChronoUnit.SECONDS);

    private static final ChronoUnit[] UNITS = new ChronoUnit[]{
        ChronoUnit.YEARS,
        ChronoUnit.MONTHS,
        ChronoUnit.WEEKS,
        ChronoUnit.DAYS,
        ChronoUnit.HOURS,
        ChronoUnit.MINUTES,
        ChronoUnit.SECONDS
    };

    private final boolean concise;
    private final ChronoUnit accuracy;

    public DurationFormatter(boolean concise) {
        this(concise, ChronoUnit.SECONDS);
    }

    public DurationFormatter(boolean concise, ChronoUnit accuracy) {
        this.concise = concise;
        this.accuracy = accuracy;
    }

    /**
     * Formats {@code duration} as a string.
     *
     * @param duration the duration
     * @return the formatted string
     */
    public String format(Duration duration) {
        long seconds = duration.getSeconds();
        StringBuilder builder = new StringBuilder();
        int outputSize = 0;

        for (ChronoUnit unit : UNITS) {
            long n = seconds / unit.getDuration().getSeconds();
            if (n > 0) {
                seconds -= unit.getDuration().getSeconds() * n;
                if (outputSize != 0) {
                    builder.append(' ');
                }
                builder.append(formatPart(n, unit));
                outputSize++;
            }
            if (seconds <= 0 || unit == this.accuracy) {
                break;
            }
        }

        if (outputSize == 0) {
            return formatPart(seconds, ChronoUnit.SECONDS);
        }
        return builder.toString();
    }

    // Taken from https://github.com/lucko/LuckPerms/blob/master/common/src/main/resources/luckperms_en.properties
    private static final Map<String, String> TRANSLATIONS = ImmutableMap.<String, String>builder()
        .put("luckperms.duration.unit.years.plural", "%s 年")
        .put("luckperms.duration.unit.years.singular", "%s 年")
        .put("luckperms.duration.unit.years.short", "%s年")
        .put("luckperms.duration.unit.months.plural", "%s 月")
        .put("luckperms.duration.unit.months.singular", "%s 月")
        .put("luckperms.duration.unit.months.short", "%s月")
        .put("luckperms.duration.unit.weeks.plural", "%s 周")
        .put("luckperms.duration.unit.weeks.singular", "%s 周")
        .put("luckperms.duration.unit.weeks.short", "%s周")
        .put("luckperms.duration.unit.days.plural", "%s 天")
        .put("luckperms.duration.unit.days.singular", "%s 天")
        .put("luckperms.duration.unit.days.short", "%s天")
        .put("luckperms.duration.unit.hours.plural", "%s 小时")
        .put("luckperms.duration.unit.hours.singular", "%s 小时")
        .put("luckperms.duration.unit.hours.short", "%s时")
        .put("luckperms.duration.unit.minutes.plural", "%s 分钟")
        .put("luckperms.duration.unit.minutes.singular", "%s 分钟")
        .put("luckperms.duration.unit.minutes.short", "%s分")
        .put("luckperms.duration.unit.seconds.plural", "%s 秒")
        .put("luckperms.duration.unit.seconds.singular", "%s 秒")
        .put("luckperms.duration.unit.seconds.short", "%s秒")
        .build();

    private String formatPart(long amount, ChronoUnit unit) {
        String format = this.concise ? "short" : amount == 1 ? "singular" : "plural";
        String translationKey = "luckperms.duration.unit." + unit.name().toLowerCase(Locale.ROOT) + "." + format;
        return String.format(TRANSLATIONS.get(translationKey), amount);
    }

}