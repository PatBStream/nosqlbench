package io.nosqlbench.virtdata.library.basics.shared.stateful.from_long;

/*
 * Copyright (c) 2022 nosqlbench
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.virtdata.api.annotations.Categories;
import io.nosqlbench.virtdata.api.annotations.Category;
import io.nosqlbench.virtdata.api.annotations.Example;
import io.nosqlbench.virtdata.api.annotations.ThreadSafeMapper;
import io.nosqlbench.virtdata.library.basics.core.threadstate.SharedState;

import java.util.HashMap;
import java.util.function.LongFunction;

@ThreadSafeMapper
@Categories({Category.state})
public class Swap implements LongFunction<Object> {

    private final String name;
    private final LongFunction<Object> nameFunc;
    private final Object defaultValue;

    @Example({"Swap('foo')","for the current thread" +
            ", swap the input value with the named variable and returned the named variable"})
    public Swap(String name) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=null;
    }

    @Example({"Swap('foo','examplevalue')","for the current thread" +
            ", swap the input value with the named variable and returned the named variable" +
            ", or return the default value if the named value is not defined."})
    public Swap(String name, Object defaultValue) {
        this.name = name;
        this.nameFunc=null;
        this.defaultValue=defaultValue;
    }

    @Example({"Swap(NumberNameToString())","for the current thread" +
            ", swap the input value with the named variable and returned the named variable" +
            ", where the variable name is generated by the provided function."})
    public Swap(LongFunction<Object> nameFunc) {
        this.nameFunc = nameFunc;
        this.name = null;
        this.defaultValue=null;
    }

    @Example({"Swap(NumberNameToString(),'examplevalue')","for the current thread" +
            ", swap the input value with the named variable and returned the named variable" +
            ", where the variable name is generated by the provided function" +
            ", or the default value if the named value is not defined."})
    public Swap(LongFunction<Object> nameFunc, Object defaultValue) {
        this.nameFunc = nameFunc;
        this.name = null;
        this.defaultValue = defaultValue;
    }

    @Override
    public Object apply(long value) {
        HashMap<String, Object> map = SharedState.tl_ObjectMap.get();
        String varname=(nameFunc!=null) ? String.valueOf(nameFunc.apply(value)) : name;

        Object output = map.get(varname);
        map.put(name,value);
        return (output!=null) ? output : defaultValue;
    }
}
