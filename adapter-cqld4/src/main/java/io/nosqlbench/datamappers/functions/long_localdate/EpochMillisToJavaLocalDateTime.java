/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.nosqlbench.datamappers.functions.long_localdate;

import io.nosqlbench.virtdata.annotations.types.Categories;
import io.nosqlbench.virtdata.annotations.types.Category;
import io.nosqlbench.virtdata.annotations.types.Example;
import io.nosqlbench.virtdata.annotations.types.ThreadSafeMapper;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.function.LongFunction;

/**
 * Converts epoch millis to a
 * java.time.{@link LocalDateTime} object, using either the system
 * default timezone or the timezone provided. If the specified ZoneId is not
 * the same as the time base of the epoch millis instant, then conversion
 * errors will occur.
 *
 * Short form ZoneId values like 'CST' can be used, although US Domestic names
 * which specify the daylight savings hours are not supported. The full list of
 * short Ids at @see <a href="https://docs.oracle.com/en/java/javase/12/docs/api/java.base/java/time/ZoneId.html#SHORT_IDS">JavaSE ZoneId Ids</a>
 *
 * Any timezone specifier may be used which can be read by {@link ZoneId#of(String)}
 */
@ThreadSafeMapper
@Categories({Category.datetime})
public class EpochMillisToJavaLocalDateTime implements LongFunction<LocalDateTime> {

    ZoneId timezone;

    @Example({"EpochMillisToJavaLocalDateTime()","Yields the LocalDateTime for the system default ZoneId"})
    public EpochMillisToJavaLocalDateTime() {
        this.timezone = ZoneId.systemDefault();
    }

    @Example({"EpochMillisToJavaLocalDateTime('ECT')","Yields the LocalDateTime for the ZoneId entry for 'Europe/Paris'"})
    public EpochMillisToJavaLocalDateTime(String zoneid) {
        this.timezone = ZoneId.of(zoneid);
    }

    @Override
    public LocalDateTime apply(long value) {
        return Instant.ofEpochMilli(value).atZone(timezone).toLocalDateTime();
    }
}
