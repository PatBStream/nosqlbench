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

package io.nosqlbench.adapter.diag;

import io.nosqlbench.adapters.api.opmapping.OpMapper;
import io.nosqlbench.adapters.api.opmapping.uniform.BaseDriverAdapter;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.nb.annotations.types.Selector;
import io.nosqlbench.nb.annotations.types.Service;

import java.util.function.Function;

@Selector("diag")
@Service
public class DiagDriverAdapter extends BaseDriverAdapter<DiagOp,DiagSpace> {
    @Override
    public OpMapper<DiagOp> getOpMapper() {
        return new DiagOpMapper();
    }

    @Override
    public Function<String, ? extends DiagSpace> getSpaceInitializer(NBConfiguration cfg) {
        return (String name) -> new DiagSpace(cfg);
    }

    @Override
    public NBConfigModel getConfigModel() {
        return super.getConfigModel().add(DiagSpace.getConfigModel());
    }
}
