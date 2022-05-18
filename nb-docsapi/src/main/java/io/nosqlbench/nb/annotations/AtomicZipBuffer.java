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

import io.nosqlbench.docexporter.BundledMarkdownProcessor;
import io.nosqlbench.docsapi.DocsBundles;
import io.nosqlbench.nb.api.markdown.aggregator.MutableMarkdown;

import javax.annotation.processing.Filer;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class AtomicZipBuffer implements AutoCloseable {
    private final Function<Path, MutableMarkdown> parser = MutableMarkdown::new;
    private final BundledMarkdownProcessor[] filters;

    private final HashMap<String, ZipOutputStream> streams = new HashMap<>();
    private final Filer filer;

    public AtomicZipBuffer(Filer filer, BundledMarkdownProcessor... filters) {
        this.filer = filer;
        this.filters = filters;
    }

    public synchronized void export(DocsBundles targets) {

        for (List<DocsBundle> value : targets.values()) {
            DocsBundle combined = null;
            for (DocsBundle docsBundle : value) {
                combined = (combined==null) ? docsBundle : docsBundle.merge(docsBundle);
            }
            export(combined);
        }

    }

    private synchronized void export(DocsBundle bundle) {
        try {

            if (bundle.relativePaths().size() == 0) {
                return;
            }

            // A saveAs zip file can't be absolute, must not have special characters, and must end in .zip
            Pattern zipout = Pattern.compile(
                "^(?<dirpath>[^/\\\\][-a-zA-Z0-9_./]+)?(?<filename>[a-zA-Z0-9][-a-zA-Z0-9_.]\\.zip$)"
            );
            Matcher saveAsMatcher = zipout.matcher(bundle.saveAs());

            if (saveAsMatcher.matches()) {
                String saveAsName = bundle.saveAs();
                ZipOutputStream zipOutputStream = streams.computeIfAbsent(
                    saveAsName, name -> createZipResource(filer, bundle)
                );
                for (VirtualizedPath vpath : bundle.relativePaths()) {
                    Path parent = vpath.basePath();
                    Path subpath = parent.resolve(vpath.subpath());
                    addEntry(subpath, parent, zipOutputStream);
                }
            } else {
                throw new RuntimeException(
                    "The output format was not recognized. You must specify a relative zipfile path, with no dots or special characters. See docs on the DocsBundle type for more details."
                );
            }

        } catch (Exception e) {
            // possible "Attempt to reopen a file for path .../target/classes/docs.zip"
            throw new RuntimeException(e);
        }

    }

    private synchronized ZipOutputStream createZipResource(Filer filer, DocsBundle bundle) {
        if (streams.containsKey(bundle.saveAs())) {
            return streams.get(bundle.saveAs());
        }

        ZipOutputStream zos;
        try {
            FileObject outFileObject = filer.createResource(StandardLocation.CLASS_OUTPUT, "", bundle.saveAs(), bundle.originatingElement());
            OutputStream stream = outFileObject.openOutputStream();
            zos = new ZipOutputStream(stream);
            zos.setMethod(ZipOutputStream.DEFLATED);
            zos.setLevel(9);
            return zos;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    private synchronized void addEntry(Path path, Path parent, ZipOutputStream zos) throws IOException {

        String name = parent.relativize(path).toString();
        name = Files.isDirectory(path) ? (name.endsWith(File.separator) ? name : name + File.separator) : name;

        ZipEntry entry = new ZipEntry(name);

        if (Files.isDirectory(path)) {
            zos.putNextEntry(entry);
            DirectoryStream<Path> stream = Files.newDirectoryStream(path);
            for (Path pathInDir : stream) {
                addEntry(pathInDir, parent, zos);
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
        zos.flush();
    }

    @Override
    public synchronized void close() throws IOException {
        for (ZipOutputStream stream : streams.values()) {
            stream.close();
        }
    }
}
