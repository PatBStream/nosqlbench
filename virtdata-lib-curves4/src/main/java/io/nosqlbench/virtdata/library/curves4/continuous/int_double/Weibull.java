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

package io.nosqlbench.virtdata.library.curves4.continuous.int_double;

import io.nosqlbench.virtdata.annotations.types.Categories;
import io.nosqlbench.virtdata.annotations.types.Category;
import io.nosqlbench.virtdata.annotations.types.ThreadSafeMapper;
import org.apache.commons.statistics.distribution.WeibullDistribution;

/**
 * @see <a href="https://en.wikipedia.org/wiki/Weibull_distribution">Wikipedia: Weibull distribution</a>
 *
 * @see <a href="http://mathworld.wolfram.com/WeibullDistribution.html">Wolfram Mathworld: Weibull Distribution</a>
 *
 * @see <a href="https://commons.apache.org/proper/commons-statistics/commons-statistics-distribution/apidocs/org/apache/commons/statistics/distribution/WeibullDistribution.html">Commons Javadoc: WeibullDistribution</a>
 *
 * {@inheritDoc}
 */
@ThreadSafeMapper
@Categories({Category.distributions})
public class Weibull extends IntToDoubleContinuousCurve {
    public Weibull(double alpha, double beta, String... mods) {
        super(new WeibullDistribution(alpha, beta), mods);
    }
}
