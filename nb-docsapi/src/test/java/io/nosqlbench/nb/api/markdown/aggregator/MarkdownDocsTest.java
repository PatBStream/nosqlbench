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

package io.nosqlbench.nb.api.markdown.aggregator;

import io.nosqlbench.nb.api.markdown.types.MarkdownInfo;

import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.stream.Stream;

public class MarkdownDocsTest {


    private List<MarkdownInfo> fromRaw(String parentPath) {
        List<MarkdownInfo> fromraw = new ArrayList<>();
        List<Path> postpaths = getSubPaths("docs-for-testing-logical");
        for (Path postpath : postpaths) {
            ParsedMarkdown parsedMarkdown = new ParsedMarkdown(postpath);
            fromraw.add(parsedMarkdown);
        }
        Collections.sort(fromraw);
        return fromraw;
    }

    private static List<Path> getSubPaths(String resourcePath) {
        List<Path> subpaths = new ArrayList<>();

        try {
            Enumeration<URL> resources =
                    MarkdownDocsTest.class.getClassLoader().getResources(resourcePath);

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                System.out.println("url="+url.toExternalForm());
                Path path = Paths.get(url.toURI());
                try (Stream<Path> fileStream = Files.walk(path, FileVisitOption.FOLLOW_LINKS)) {
                    fileStream.filter(p -> !Files.isDirectory(p, LinkOption.NOFOLLOW_LINKS))
                            .forEach(subpaths::add);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return subpaths;
    }
}
