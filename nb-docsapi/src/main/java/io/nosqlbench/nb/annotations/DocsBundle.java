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

import javax.lang.model.element.Element;
import java.util.*;

/**
 *
 * <p>This type represents the intention to save specifically included
 * docs resources into a specific target.</p>
 * <hr></hr>
 * <p>Supported target formats are:
 * <ul>
 *     <li>relative/file/path.zip - Any relative file path which ends with {@code .zip}.
 *     The included files will be added to a file in relative path from current working directory.</li>
 * </ul></p>
 *
 * @param saveAs is the target output file or directory.
 * @param relativePaths are the paths which are included, relative to the sourceDir
 * @param originatingElement The originating elements for the contained paths
 */
public record DocsBundle(String saveAs, Collection<VirtualizedPath> relativePaths, Element... originatingElement) {

    public DocsBundle merge(DocsBundle other) {
        if (saveAs.equals(other.saveAs)) {

            Set<VirtualizedPath> combinedPaths = new HashSet<>();
            combinedPaths.addAll(relativePaths);
            combinedPaths.addAll(other.relativePaths);

            List<Element> combinedOriginating = new ArrayList<>();
            combinedOriginating.addAll(Arrays.stream(originatingElement).toList());
            combinedOriginating.addAll(Arrays.stream(other.originatingElement).toList());

            return new DocsBundle(
                saveAs,
                combinedPaths,
                combinedOriginating.toArray(new Element[0])
            );
        } else {
            throw new RuntimeException("unable to merge two different saveAs specs");
        }
    }
}
