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

package io.nosqlbench.virtdata.library.basics.shared.from_long.to_int;

import io.nosqlbench.virtdata.annotations.types.Categories;
import io.nosqlbench.virtdata.annotations.types.Category;
import io.nosqlbench.virtdata.annotations.types.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.shared.from_long.to_long.CycleRange;

import java.util.function.LongToIntFunction;

@ThreadSafeMapper
@Categories({Category.general})
public class AddCycleRange implements LongToIntFunction {

    private final CycleRange cycleRange;

    public AddCycleRange(int maxValue) {
        this(0, maxValue);
    }

    public AddCycleRange(int minValue, int maxValue) {
        this.cycleRange = new CycleRange(minValue,maxValue);
    }

    @Override
    public int applyAsInt(long operand) {
        return (int) ((operand + cycleRange.applyAsLong(operand)) & Integer.MAX_VALUE);
    }
}
