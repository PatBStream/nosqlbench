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

import io.nosqlbench.docsys.api.WebServiceObject;
import io.nosqlbench.docsys.endpoints.DocServerStatusEndpoint;

module docsys {
    exports io.nosqlbench.docsys.api;
    requires nb.annotations;
    requires jakarta.inject;
    requires jakarta.ws.rs;
    requires org.apache.logging.log4j;
    requires jetty.servlet.api;
    requires org.eclipse.jetty.http;
    requires org.eclipse.jetty.server;
    requires org.eclipse.jetty.servlet;
    requires nb.api;
    requires org.eclipse.jetty.rewrite;
    requires org.eclipse.jetty.servlets;
    requires jersey.server;
    requires jersey.container.servlet.core;
    requires java.desktop;
    provides WebServiceObject with DocServerStatusEndpoint;
}
