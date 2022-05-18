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

package io.nosqlbench.engine.api.activityimpl;

import io.nosqlbench.api.spi.SelectorFilter;
import io.nosqlbench.engine.api.activityapi.core.Activity;
import io.nosqlbench.engine.api.activityapi.cyclelog.buffers.results.ResultReadable;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultFilterDispenser;
import io.nosqlbench.engine.api.activityapi.cyclelog.filters.ResultValueFilterType;
import io.nosqlbench.engine.api.activityapi.input.InputDispenser;
import io.nosqlbench.engine.api.activityapi.input.InputType;
import io.nosqlbench.engine.api.activityapi.output.OutputDispenser;
import io.nosqlbench.engine.api.activityapi.output.OutputType;
import io.nosqlbench.engine.api.util.SimpleConfig;
import io.nosqlbench.nb.annotations.types.Selector;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.function.Predicate;

public class CoreServices {

    public static <A extends Activity> Optional<OutputDispenser> getOutputDispenser(A activity) {
        Optional<String> string = new SimpleConfig(activity, "output").getString("type");
        if (string.isEmpty()) {
            return Optional.empty();
        }

        OutputType outputType = string
            .map(s -> SelectorFilter.of(s, ServiceLoader.load(OutputType.class)))
            .get().getOne();
        OutputDispenser outputDispenser = outputType.getOutputDispenser(activity);

        if (outputDispenser==null) {
            return Optional.empty();
        }

        Optional<Predicate<ResultReadable>> outputFilterDispenser = getOutputFilter(activity);
        if (outputFilterDispenser.isPresent()) {
            outputDispenser = new FilteringOutputDispenser(outputDispenser, outputFilterDispenser.get());
        }

        return Optional.ofNullable(outputDispenser);
    }

    public static <A extends Activity> Optional<Predicate<ResultReadable>> getOutputFilter(A activity) {
        String paramdata= activity.getParams().getOptionalString("of")
                .orElse(activity.getParams().getOptionalString("outputfilter").orElse(null));
        if (paramdata==null) {
            return Optional.empty();
        }
        return getFilterPredicate(paramdata);
    }

//    public static <A extends Activity> Optional<IntPredicateDispenser> getResultFilterDispenser(A activity) {
//        Optional<IntPredicateDispenser> intPredicateDispenser = new SimpleConfig(activity, "resultfilter")
//                .getString("type")
//                .flatMap(ExperimentalResultFilterType.FINDER::get)
//                .map(rft -> rft.getFilterDispenser(activity));
//        return intPredicateDispenser;
//    }
//
    public static <A extends Activity> InputDispenser getInputDispenser(A activity) {
        String inputTypeName = new SimpleConfig(activity, "input").getString("type").orElse("atomicseq");

        InputType inputType = SelectorFilter.of(inputTypeName,ServiceLoader.load(InputType.class)).getOne();
        InputDispenser dispenser = inputType.getInputDispenser(activity);
        Optional<Predicate<ResultReadable>> inputFilterDispenser = getInputFilter(activity);
        if (inputFilterDispenser.isPresent()) {
            dispenser = new FilteringInputDispenser(dispenser, inputFilterDispenser.get());
        }
        return dispenser;
    }

    public static <A extends Activity> Optional<Predicate<ResultReadable>> getInputFilter(A activity) {
        String paramdata= activity.getParams().getOptionalString("if")
                .orElse(activity.getParams().getOptionalString("inputfilter").orElse(null));
        if (paramdata==null) {
            return Optional.empty();
        }
        return getFilterPredicate(paramdata);
    }

    private static Optional<Predicate<ResultReadable>> getFilterPredicate(String paramdata) {
        String type = new SimpleConfig(paramdata).getString("type").orElse("core");

        Optional<ResultValueFilterType> cycleResultFilterType = ServiceLoader.load(ResultValueFilterType.class)
            .stream()
            .filter(p -> p.type().getAnnotation(Selector.class).value().equals(type))
            .findFirst()
            .map(ServiceLoader.Provider::get);

        Optional<ResultFilterDispenser> crfd = cycleResultFilterType.map(crft -> crft.getDispenser(paramdata));
        Optional<Predicate<ResultReadable>> predicate = crfd.map(ResultFilterDispenser::getResultFilter);
        return predicate;

    }

}
