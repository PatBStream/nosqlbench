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

package io.nosqlbench.virtdata.core.bindings;

import io.nosqlbench.virtdata.annotations.gen.DocFuncData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

public class VirtDataFunctionFinder {
    private final static Logger logger = LogManager.getLogger(VirtDataFunctionFinder.class);

    public VirtDataFunctionFinder() {
    }

    public List<String> getFunctionNames() {
        List<String> names = new ArrayList<>();

        try {
            Enumeration<URL> functionListList = getClass().getClassLoader().getResources("META-INF/functions");

            while (functionListList.hasMoreElements()) {
                URL url = functionListList.nextElement();
                logger.debug("loading function list from " + url.toString());
                InputStream stream = url.openStream();
                InputStreamReader isr = new InputStreamReader(stream);
                BufferedReader br = new BufferedReader(isr);
                for (;;) {
                    String line = br.readLine();
                    if (line == null)
                        break;
                    names.add(line);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        ServiceLoader<DocFuncData> loader =ServiceLoader.load(DocFuncData.class);
        loader.iterator().forEachRemaining(d -> names.add(d.getPackageName() + "." + d.getClassName()));
        List<String> cleaned = names.stream().sorted().distinct().collect(Collectors.toList());
        return cleaned;
    }
}
