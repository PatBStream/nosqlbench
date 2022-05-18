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

package io.nosqlbench.adapter.cqld4;

import io.nosqlbench.adapter.cqld4.opmappers.Cqld4CoreOpMapper;
import io.nosqlbench.adapters.api.opmapping.OpMapper;
import io.nosqlbench.adapters.api.opmapping.uniform.BaseDriverAdapter;
import io.nosqlbench.adapters.api.opmapping.uniform.DriverSpaceCache;
import io.nosqlbench.adapters.api.opmapping.uniform.flowtypes.Op;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.docsapi.PackageDocs;
import io.nosqlbench.nb.annotations.types.Selector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

@PackageDocs
@Selector("cqld4")
public class Cqld4DriverAdapter extends BaseDriverAdapter<Op, Cqld4Space> {
    private final static Logger logger = LogManager.getLogger(Cqld4DriverAdapter.class);
    private final Cqld4OpFieldRemapper cqld4OpFieldRemapper = new Cqld4OpFieldRemapper();

    @Override
    public OpMapper<Op> getOpMapper() {
        DriverSpaceCache<? extends Cqld4Space> spaceCache = getSpaceCache();
        NBConfiguration config = getConfiguration();
        return new Cqld4CoreOpMapper(config, spaceCache);
    }

    @Override
    public Function<String, ? extends Cqld4Space> getSpaceInitializer(NBConfiguration cfg) {
        return s -> new Cqld4Space(s,cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(Cqld4Space.getConfigModel());
    }


    @Override
    public Supplier<List<Function<Map<String, Object>, Map<String, Object>>>> getOpFieldRemappers() {
        return new Cqld4OpFieldRemapper();
    }
}
