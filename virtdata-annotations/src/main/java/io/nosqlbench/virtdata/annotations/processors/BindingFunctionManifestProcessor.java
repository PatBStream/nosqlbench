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

package io.nosqlbench.virtdata.annotations.processors;
//io.nosqlbench.virtdata.api.processors.FunctionDocInfoProcessor

//io.nosqlbench.virtdata.api.processors.FunctionDocInfoProcessor


import io.nosqlbench.virtdata.annotations.gen.DocForFunc;
import io.nosqlbench.virtdata.annotations.gen.FuncEnumerator;
import io.nosqlbench.virtdata.annotations.gen.FunctionDocInfoWriter;
import io.nosqlbench.virtdata.annotations.types.PerThreadMapper;
import io.nosqlbench.virtdata.annotations.types.ThreadSafeMapper;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Name;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This documentation processor is responsible for finding all the enumerated that feed documentation
 * manifests. It simply calls listener interfaces to do the rest of the work.
 */
public class BindingFunctionManifestProcessor extends AbstractProcessor {

    public final static String AUTOSUFFIX = "AutoDocsInfo";

    private static final Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
    private Filer filer;
    private Map<String, String> options;
    private Elements elementUtils;
    private Messager messenger;
    private SourceVersion sourceVersion;
    private Types typeUtils;
    private FuncEnumerator enumerator;


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(PerThreadMapper.class.getCanonicalName(),ThreadSafeMapper.class.getCanonicalName());
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.filer = processingEnv.getFiler();
        this.options = processingEnv.getOptions();
        this.elementUtils = processingEnv.getElementUtils();
        this.messenger = processingEnv.getMessager();
        this.sourceVersion = processingEnv.getSourceVersion();
        this.typeUtils = processingEnv.getTypeUtils();

        this.enumerator = new FuncEnumerator(this.typeUtils, this.elementUtils, this.filer);
//        enumerator.addListener(new StdoutListener());
//        enumerator.addListener(new YamlDocsEnumerator(this.filer, this.messenger));
        enumerator.addListener(new FunctionDocInfoWriter(this.filer, this.messenger, AUTOSUFFIX));

    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        List<Element> ts = new ArrayList<>();

        ts.addAll(roundEnv.getElementsAnnotatedWith(ThreadSafeMapper.class));
        ts.addAll(roundEnv.getElementsAnnotatedWith(PerThreadMapper.class));

        for (Element classElem : ts) {

            if (classElem.getKind() != ElementKind.CLASS) {
                throw new RuntimeException("Unexpected kind of element: " + classElem.getKind() + " for " + classElem);
            }

            // package and Class Name

            Name qualifiedName = ((TypeElement) classElem).getQualifiedName();
            Matcher pnm = packageNamePattern.matcher(qualifiedName);
            if (!pnm.matches()) {
                throw new RuntimeException("Unable to match qualified name for package and name: " + qualifiedName);
            }
            String packageName = pnm.group("packageName");
            String simpleClassName = pnm.group("className");


//            System.out.println("here");
            // Class JavaDoc

        }

        return false;
    }

    private static final Pattern inheritDocPattern = Pattern.compile("(?ms)(?<pre>.*)(?<inherit>\\{@inheritDoc})(?<post>.*)$");
    private String inheritDocs(String classDoc, Element classElem) {
        if (classDoc==null) {
            return null;
        }
        Matcher matcher = inheritDocPattern.matcher(classDoc);
        if (!matcher.matches()) {
            return classDoc;
        }
        StringBuilder docData = new StringBuilder();
        String pre = matcher.group("pre");
        String post = matcher.group("post");

        Optional<TypeElement> inheritFromElement = Optional.ofNullable(((TypeElement) classElem).getSuperclass())
                .map(String::valueOf)
                .map(elementUtils::getTypeElement);


        if (!inheritFromElement.isPresent()) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "Element " + classElem + " has '{@inheritDoc}', but a superclass was not found.");
            return pre + "UNABLE TO FIND ELEMENT TO INHERIT DOCS FROM for " + classElem + " " + post;
        }
        TypeElement inheritFromType = inheritFromElement.get();
        String inheritedDocs = elementUtils.getDocComment(inheritFromType);
        if (inheritedDocs==null) {
            messenger.printMessage(Diagnostic.Kind.ERROR, "javadocs are missing on " + inheritFromElement + ", but "
            + classElem + " is trying to inherit docs from it.");
            return pre + "UNABLE TO FIND INHERITED DOCS for " + classElem + " " + post;
        }

        if (inheritDocPattern.matcher(inheritedDocs).matches()) {
            return pre + inheritDocs(inheritedDocs,inheritFromType) + post;
        } else {
            return pre + inheritedDocs + post;
        }

    }

    private String cleanJavadoc(String ctorDoc) {
        return ctorDoc.replaceAll("(?m)^ ", "");
    }

    private static class StdoutListener implements FuncEnumerator.Listener {
        @Override
        public void onFunctionModel(DocForFunc functionDoc) {
            System.out.println(functionDoc);
        }
    }
}
