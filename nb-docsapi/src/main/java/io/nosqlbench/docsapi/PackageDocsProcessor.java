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
import io.nosqlbench.nb.annotations.DocsExporter;
import io.nosqlbench.nb.annotations.MarkdownScanner;
import io.nosqlbench.nb.annotations.Service;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This annotation processor is responsible for adding services to the
 * <pre>classes/META-INF/services/servicename</pre> file for each
 * implemented and annotated service name.
 */
@SupportedSourceVersion(SourceVersion.RELEASE_17)
@Service(value=javax.annotation.processing.Processor.class,selector = "PackageDocsProcessor")
public class PackageDocsProcessor extends AbstractProcessor {
    public final static String SERVICE_NAME = Service.class.getCanonicalName();

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> supportedAnnotations = new HashSet<>();
        supportedAnnotations.add(PackageDocs.class.getCanonicalName());
        return supportedAnnotations;
    }

    private static final Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
    private Filer filer;
    private Map<String, String> options;
    private Elements elementUtils;
    private Messager messenger;
    private SourceVersion sourceVersion;
    private Types typeUtils;
    private final Map<String, Writer> writers = new HashMap<>();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.options = processingEnv.getOptions();
        this.elementUtils = processingEnv.getElementUtils();
        this.messenger = processingEnv.getMessager();
        this.sourceVersion = processingEnv.getSourceVersion();
        this.typeUtils = processingEnv.getTypeUtils();
    }

//    private Writer getWriterForClass(String className, Element... elements) {
//        return writers.computeIfAbsent(className, s -> {
//            try {
//                return filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + s, elements)
//                        .openWriter();
//            } catch (IOException e) {
//                messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
//                return null;
//            }
//        });
//    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<DocsBundle> targets = new ArrayList<>();
        Set<DocsBundle> sourcepaths = new LinkedHashSet<>();
        try {
            for (String annotationType : this.getSupportedAnnotationTypes()) {
                Class<? extends Annotation> annotationClass =
                    (Class<? extends Annotation>) Class.forName(annotationType);
                Set<? extends Element> tsms = roundEnv.getElementsAnnotatedWith(annotationClass);

                for (Element element : tsms) {
                    PackageDocs packagedocs = element.getAnnotation(PackageDocs.class);
                    String as = packagedocs.as();
                    FileObject referenceFile = null;
                    if (element.getKind() == ElementKind.CLASS) {
                        messenger.printMessage(Diagnostic.Kind.NOTE, "found documented class " + element.getSimpleName());
                        TypeElement classType = ((TypeElement) element);
                        PackageElement packageElement = (PackageElement) classType.getEnclosingElement();
                        referenceFile = filer.getResource(StandardLocation.SOURCE_PATH, packageElement.getQualifiedName(), classType.getSimpleName()+".java");
                    } else if (element.getKind() == ElementKind.PACKAGE) {
                        PackageElement packageElement = (PackageElement) element;
                        referenceFile = filer.getResource(StandardLocation.SOURCE_PATH, packageElement.getQualifiedName(), "package-info.java");
                    } else {
                        throw new RuntimeException("unrecognized documented element type" + element.getClass());
                    }
                    Path dir = Path.of(referenceFile.getName()).getParent();
                    List<Pattern> patterns = Arrays.stream(packagedocs.matchers()).map(Pattern::compile).toList();
                    MarkdownScanner scanner = new MarkdownScanner(dir, patterns);
                    Files.walkFileTree(dir, scanner);

                    targets.add(new DocsBundle(dir, scanner.getRelativePaths(), as));
                }
            }

        } catch (Exception e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }

        for (DocsBundle target : targets) {
            new DocsExporter().export(target);
        }

        return true;
    }
}
