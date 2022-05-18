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

package io.nosqlbench.engine.core.lifecycle;

import io.nosqlbench.adapters.api.opmapping.uniform.DriverAdapter;
import io.nosqlbench.api.activityimpl.ActivityDef;
import io.nosqlbench.api.config.standard.NBConfigModel;
import io.nosqlbench.api.config.standard.NBConfigurable;
import io.nosqlbench.api.config.standard.NBConfiguration;
import io.nosqlbench.api.config.standard.NBEnvironment;
import io.nosqlbench.api.content.Content;
import io.nosqlbench.api.content.NBIO;
import io.nosqlbench.api.errors.BasicError;
import io.nosqlbench.api.spi.SelectorFilter;
import io.nosqlbench.engine.api.activityapi.core.ActivityType;
import io.nosqlbench.engine.api.activityconfig.StatementsLoader;
import io.nosqlbench.engine.api.activityconfig.yaml.StmtsDocList;
import io.nosqlbench.engine.api.activityimpl.uniform.StandardActivityType;
import io.nosqlbench.nb.annotations.types.Selector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class ActivityTypeLoader {

    private static final Logger logger = LogManager.getLogger(ActivityTypeLoader.class);
    private final Set<URL> jarUrls = new HashSet<>();

    public ActivityTypeLoader() {

        List<String> libpaths = NBEnvironment.INSTANCE.interpolateEach(":", "$" + NBEnvironment.NBLIBS);
        Set<URL> urlsToAdd = new HashSet<>();

        for (String libpaths_entry : libpaths) {
            Path libpath = Path.of(libpaths_entry);
            if (Files.isDirectory(libpath)) {
                urlsToAdd = addLibDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".zip")) {
                urlsToAdd = addZipDir(urlsToAdd, libpath);
            } else if (Files.isRegularFile(libpath) && libpath.toString().toLowerCase().endsWith(".jar")) {
                urlsToAdd = addJarFile(urlsToAdd, libpath);
            }
        }
        extendClassLoader(urlsToAdd);
    }

    private synchronized void extendClassLoader(String... paths) {
        Set<URL> urls = new HashSet<>();
        for (String path : paths) {
            URL url = null;
            try {
                url = new URL(path);
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            urls.add(url);
        }
        extendClassLoader(urls);
    }

    private synchronized void extendClassLoader(Set<URL> urls) {
        Set<URL> newUrls = new HashSet<>();
        if (!jarUrls.containsAll(urls)) {
            for (URL url : urls) {
                if (!jarUrls.contains(url)) {
                    newUrls.add(url);
                    jarUrls.add(url);
                }
            }
            URL[] newUrlAry = newUrls.toArray(new URL[]{});
            URLClassLoader ucl = URLClassLoader.newInstance(newUrlAry, Thread.currentThread().getContextClassLoader());
            Thread.currentThread().setContextClassLoader(ucl);
            logger.debug("Extended class loader layering with " + newUrls);
        } else {
            logger.debug("All URLs specified were already in a class loader.");
        }
    }

    private Set<URL> addJarFile(Set<URL> urls, Path libpath) {
        try {
            urls.add(libpath.toUri().toURL());
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        return urls;
    }

    private Set<URL> addZipDir(Set<URL> urlsToAdd, Path libpath) {
        return urlsToAdd;
    }

    private Set<URL> addLibDir(Set<URL> urlsToAdd, Path libpath) {
        Set<URL> urls = NBIO.local()
            .prefix(libpath.toString())
            .extension(".jar")
            .list().stream().map(Content::getURL)
            .collect(Collectors.toSet());
        urlsToAdd.addAll(urls);
        return urlsToAdd;
    }

    public Optional<ActivityType> load(ActivityDef activityDef,
                                       ServiceLoader<ActivityType> atLoader,
                                       ServiceLoader<DriverAdapter> daLoader) {

        final String driverName = activityDef.getParams()
            .getOptionalString("driver", "type")
            .orElseThrow(() -> new BasicError("The parameter 'driver=' is required."));

        activityDef.getParams()
            .getOptionalString("jar")
            .map(jar -> {
                Set<URL> urls = NBIO.local().search(jar)
                    .list()
                    .stream().map(Content::getURL)
                    .collect(Collectors.toSet());
                return urls;
            })
            .ifPresent(this::extendClassLoader);

        Optional<ActivityType> driverAdapter = this.getDriverAdapter(driverName, activityDef, daLoader);

        if (driverAdapter.isPresent()) {
            return driverAdapter;
        }

        ActivityType oneActivityType = SelectorFilter.of(driverName, atLoader).getOne();
        return Optional.of(oneActivityType);

    }

    private Optional<ActivityType> getDriverAdapter(String activityTypeName, ActivityDef activityDef, ServiceLoader<DriverAdapter> adapterLoader) {

        Optional<? extends DriverAdapter> oda = SelectorFilter.of(activityTypeName, adapterLoader).get();

        if (oda.isPresent()) {
            DriverAdapter<?, ?> driverAdapter = oda.get();

            activityDef.getParams().remove("driver");
            if (driverAdapter instanceof NBConfigurable) {
                NBConfigModel cfgModel = ((NBConfigurable) driverAdapter).getConfigModel();
                Optional<String> op_yaml_loc = activityDef.getParams().getOptionalString("yaml", "workload");
                if (op_yaml_loc.isPresent()) {
                    Map<String,Object> disposable = new LinkedHashMap<>(activityDef.getParams());
                    StmtsDocList workload = StatementsLoader.loadPath(logger, op_yaml_loc.get(), disposable, "activities");
                    cfgModel=cfgModel.add(workload.getConfigModel());
                }
                NBConfiguration cfg = cfgModel.apply(activityDef.getParams());
                ((NBConfigurable) driverAdapter).applyConfig(cfg);
            }
            ActivityType activityType = new StandardActivityType<>(driverAdapter, activityDef);
            return Optional.of(activityType);
        } else {
            return Optional.empty();
        }
    }

    public Set<String> getAllSelectors() {
        Set<String> all = new LinkedHashSet<>();
        ServiceLoader.load(ActivityType.class)
            .stream()
            .filter(p -> p.type().getAnnotation(Selector.class)!=null)
            .map(p -> p.type().getAnnotation(Selector.class).value())
            .forEach(all::add);
        ServiceLoader.load(DriverAdapter.class)
            .stream()
            .filter(p -> p.type().getAnnotation(Selector.class)!=null)
            .map(p -> p.type().getAnnotation(Selector.class).value())
            .forEach(all::add);
        return all;
    }
}
