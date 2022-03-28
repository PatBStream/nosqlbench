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

package io.nosqlbench.nb.annotations;

import java.nio.file.Path;

public record VirtualizedPath(Path basePath, Path subpath) {
    public VirtualizedPath {
        if (!basePath.isAbsolute()) {
            throw new RuntimeException("basePath must be absolute.");
        }
        if (!subpath.isAbsolute()) {
            throw new RuntimeException("subpath must be absolute on construction to allow for encapsulation checks.");
        }
        if (!subpath.startsWith(basePath)) {
            throw new RuntimeException("The subpath must be a nested element of the basePath");
        }
        subpath = basePath.relativize(subpath);
    }
}
