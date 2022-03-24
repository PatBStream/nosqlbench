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
import java.util.List;

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
 *
 * @param dir Is the base directory of the content
 * @param relativePaths are the paths which are included, relative to the dir
 * @param saveAs is the target output file or directory.
 */
public record DocsBundle(Path dir, List<Path> relativePaths, String saveAs) {
}
