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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Package Docs is a double entendre. It refers to docs contained in a package
 * as well as the intention to package them up for you.
 *
 * When this annotation is added to a package (via a package-info.java file), or
 * another Java type, like a class or interface, then the containing source
 * directory will be scanned for any non-code contents to be exported.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.PACKAGE,ElementType.TYPE})
public @interface PackageDocs {

    /**
     * The file filter which determines what is included in the exported artifact.
     * This is a regular expression that may or may not contain path elements.
     * By default it is simply a filename filter for markdown files.
     * @return
     */
    String[] matchers() default ".+\\.md";

    /**
     * The output target for this entry. You should leave this as the default "docs.zip"
     * in most cases.
     * @return
     */
    String as() default "docs.zip";
}
