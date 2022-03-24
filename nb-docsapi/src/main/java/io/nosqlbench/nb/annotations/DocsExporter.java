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

import io.nosqlbench.docexporter.BundledFrontmatterInjector;
import io.nosqlbench.docexporter.BundledMarkdownProcessor;
import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class DocsExporter {

    private final Function<Path, MutableMarkdown> parser = MutableMarkdown::new;
    private final BundledMarkdownProcessor[] filters;

    public DocsExporter(BundledMarkdownProcessor... filters) {
        this.filters = filters;
    }

    public DocsExporter() {
        this.filters = new BundledMarkdownProcessor[] {new BundledFrontmatterInjector()};
    }

    public void export(DocsBundle docsBundle) {

        // A saveAs zip file can't be absolute, must not have special characters, and must end in .zip
        Pattern zipout = Pattern.compile(
            "^(?<dirpath>[^/\\\\][-a-zA-Z0-9_./]+)?(?<filename>[a-zA-Z0-9][-a-zA-Z0-9_.]\\.zip$)"
        );
        Matcher zipoutMatcher = zipout.matcher(docsBundle.saveAs());
        if (zipoutMatcher.matches()) {
            Path filepath = Path.of(docsBundle.saveAs());

            // Create directory for file path
            Path dirpath = filepath.getParent();
            if (!Files.exists(dirpath)) {
                try {
                    Files.createDirectories(dirpath,
                        PosixFilePermissions.asFileAttribute(
                            PosixFilePermissions.fromString("rwxr-x---")
                        )
                    );
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            } else if (!Files.isDirectory(dirpath)) {
                throw new RuntimeException("can't create file for '" + filepath + ", since '" + dirpath + "' is not a directory.");
            }

            ZipOutputStream zos;

            try {
                OutputStream stream = Files.newOutputStream(filepath, StandardOpenOption.WRITE, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
                zos = new ZipOutputStream(stream);
                zos.setMethod(ZipOutputStream.DEFLATED);
                zos.setLevel(9);

                for (Path relativePath : docsBundle.relativePaths()) {
                    addEntry(relativePath,relativePath.getParent(),zos);
                }

                zos.finish();
                stream.close();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else {
            throw new RuntimeException(
                "The output format was not recognized. You must specify a relative zipfile path, with no dots or special characters. See docs on the DocsBundle type for more details."
            );
        }
    }

    private void addEntry(Path path, Path parent, ZipOutputStream zos) throws IOException {


        String name = parent.relativize(path).toString();
        name = Files.isDirectory(path) ? (name.endsWith(File.separator) ? name : name + File.separator) : name;

        ZipEntry entry = new ZipEntry(name);

        if (Files.isDirectory(path)) {
            zos.putNextEntry(entry);
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            for (Path pathInDir : stream) {
                addEntry(pathInDir,parent,zos);
            }
        } else {
            entry.setTime(Files.getLastModifiedTime(path).toMillis());
            zos.putNextEntry(entry);

            if (path.toString().toLowerCase(Locale.ROOT).endsWith(".md")) {
                MutableMarkdown parsed = parser.apply(path);
                for (BundledMarkdownProcessor filter : this.filters) {
                    parsed = filter.apply(parsed);
                }
                zos.write(parsed.getComposedMarkdown().getBytes(StandardCharsets.UTF_8));
            } else {
                byte[] bytes = Files.readAllBytes(path);
                zos.write(bytes);
            }
        }
        zos.closeEntry();

    }

}
