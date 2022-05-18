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

package io.nosqlbench.docsapi;

import io.nosqlbench.nb.annotations.DocsBundle;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DocsBundles extends HashMap<String, List<DocsBundle>> {

    public synchronized void addBundle(DocsBundle bundle) {
        List<DocsBundle> as = computeIfAbsent(bundle.saveAs(), s -> new ArrayList<DocsBundle>());
        as.add(bundle);
    }

    public List<List<DocsBundle>> getBundleGroups() {
        return new ArrayList<>(values());
    }
}
