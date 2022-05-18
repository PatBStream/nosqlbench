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

package io.nosqlbench.nb.annotations.processors;

import io.nosqlbench.nb.annotations.types.Selector;
import io.nosqlbench.nb.annotations.types.Service;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.StandardLocation;
import java.io.IOException;
import java.io.Writer;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.regex.Pattern;

/**
 * This annotation processor is responsible for adding services to the
 * <pre>classes/META-INF/services/servicename</pre> file for each
 * implemented and annotated service name.
 */

// TODO: read module-info.java and warn if there are not matching entries
public class ServiceProcessor extends AbstractProcessor {

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        return Set.of(Service.class.getCanonicalName(), Selector.class.getCanonicalName());
    }

    private static final Pattern packageNamePattern = Pattern.compile("(?<packageName>.+)?\\.(?<className>.+)");
    private Filer filer;
    private Map<String, String> options;
    private Elements elementUtils;
    private Messager messenger;
    private SourceVersion sourceVersion;
    private Types typeUtils;
    private final Map<String, Writer> writers = new HashMap<>();
    private final Map<String, Services> services = new LinkedHashMap<>();

    @Override
    public Set<String> getSupportedOptions() {
        return Set.of(SourceVersion.latest().toString());
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
    }

    private Services getServicesForName(String svcName, Element... elements) {
        return services.computeIfAbsent(svcName, Services::new);
    }

    private Writer getWriterForClass(String className, Element... elements) {
        return writers.computeIfAbsent(className, s -> {
            try {
                return filer.createResource(StandardLocation.CLASS_OUTPUT, "", "META-INF/services/" + s, elements)
                    .openWriter();
            } catch (IOException e) {
                messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
                return null;
            }
        });
    }

    private final static class Services {
        private final String serviceName;
        private final Set<String> implementors = new LinkedHashSet<>();
        private final Set<Element> elements = new LinkedHashSet<>();

        public Services(String svcName) {
            this.serviceName = svcName;
        }

        public Services addImpl(String implClassName, Element... elements) {
            this.implementors.add(implClassName);
            this.elements.addAll(Arrays.asList(elements));
            return this;
        }

        public Element[] getElements() {
            return elements.toArray(new Element[0]);
        }

        public Set<String> getImpls() {
            return this.implementors;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        if (roundEnv.processingOver()) {

            try {
                for (String svc : services.keySet()) {
                    Services impls = this.services.get(svc);

                    Writer writerForClass = getWriterForClass(svc, impls.getElements());
                    for (String impl : impls.getImpls()) {
                        writerForClass.write(impl + "\n");
                    }
                }
            } catch (Exception e) {
                messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
            }

            try {
                for (Writer writer : this.writers.values()) {
                    writer.close();
                }
            } catch (Exception e) {
                messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
            }
            return true;
        }

        try {
            for (String annotationType : this.getSupportedAnnotationTypes()) {
                Class<? extends Annotation> annotationClass =
                    (Class<? extends Annotation>) Class.forName(annotationType);
                Set<? extends Element> tsms = roundEnv.getElementsAnnotatedWith(annotationClass);

                for (Element element : tsms) {
                    String serviceClass = null;
                    for (AnnotationMirror am : element.getAnnotationMirrors()) {
                        DeclaredType atype = am.getAnnotationType();
                        if (!annotationType.equals(atype.toString())) {
                            continue;
                        }

//                        List<? extends ExecutableElement> valueKeys = am.getElementValues().keySet().stream()
//                                .filter(k -> k.toString().equals("value()")).collect(Collectors.toList());
//                        if (valueKeys.size()==0) {
//                            messenger.printMessage(Diagnostic.Kind.ERROR, "Annotation missing required value");
//                            return false;
//                        }

                        if (element instanceof TypeElement impltype) {


                            Element svcElement = element;
                            TypeElement svcTypeElement = (TypeElement) svcElement;
                            TypeMirror svcelement_basetype = svcTypeElement.getSuperclass();
                            Element svcSuperClass = typeUtils.asElement(svcelement_basetype);
                            while (svcSuperClass!=null && !svcSuperClass.getSimpleName().toString().endsWith("Object")) {
//                                Element enclosing = scelement.getEnclosingElement();
//                                Name simpleName = scelement.getSimpleName();
                                svcElement = typeUtils.asElement(svcelement_basetype);
                                svcTypeElement=(TypeElement)svcElement;
                                svcelement_basetype = svcTypeElement.getSuperclass();
                                svcSuperClass = typeUtils.asElement(svcelement_basetype);
                            }

                            List<? extends TypeMirror> interfaces = svcTypeElement.getInterfaces();
                            if (interfaces.size() >= 1) {
                                svcTypeElement= (TypeElement)typeUtils.asElement(interfaces.get(0));
                                serviceClass=svcTypeElement.getQualifiedName().toString();
                                break;
                            }
                        }

                        messenger.printMessage(Diagnostic.Kind.WARNING, "Unable to process Selector annotation for " + element + ": wrong type, not a TypeElement");
                        continue;

//                        AnnotationValue annotationValue = am.getElementValues().get(valueKeys.get(0));
//                        serviceClass = annotationValue.getValue().toString();
                    }
                    if (serviceClass == null) {
                        messenger.printMessage(Diagnostic.Kind.ERROR, "Unable to find service class for " + element.getSimpleName());
                        continue;
                    }

                    String name = ((TypeElement)element).getQualifiedName().toString();
                    messenger.printMessage(Diagnostic.Kind.NOTE, "Adding service entry for implementation of " + serviceClass + ": " + name);

                    Services services = this.services.computeIfAbsent(serviceClass, Services::new);
                    services.addImpl(name, tsms.toArray(new Element[0]));

//                    Writer w = getWriterForClass(serviceClass, tsms.toArray(new Element[0]));
//                    Name name = ((TypeElement) element).getQualifiedName();
//                    w.write(name + "\n");

                }
            }

        } catch (Exception e) {
            messenger.printMessage(Diagnostic.Kind.ERROR, e.toString());
        }


        return true;
    }
}
