package io.nosqlbench.engine.cli;

/*
 * Copyright (c) 2022 nosqlbench
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


import io.nosqlbench.engine.api.scenarios.NBCLIScenarioParser;
import io.nosqlbench.nb.api.content.Content;
import io.nosqlbench.nb.api.content.NBIO;

import java.security.InvalidParameterException;
import java.util.*;

public class NBCLICommandParser {
    private static final String FRAGMENT = "fragment";
    private static final String SCRIPT = "script";
    private static final String START = "start";
    private static final String RUN = "run";
    private static final String AWAIT = "await";
    private static final String STOP = "stop";
    private static final String FORCE_STOP = "forceStop";
    private static final String ACTIVITY = "activity";
    private static final String SCENARIO = "scenario";
    private static final String WAIT_MILLIS = "waitmillis";

    public static final Set<String> RESERVED_WORDS = new HashSet<>() {{
        addAll(
                Arrays.asList(
                        SCRIPT, ACTIVITY, SCENARIO, RUN, START,
                        FRAGMENT, STOP, FORCE_STOP, AWAIT, WAIT_MILLIS
                )
        );
    }};

    public static void parse(
            LinkedList<String> arglist,
            LinkedList<Cmd> cmdList,
            String... includes
    ) {
        PathCanonicalizer canonicalizer = new PathCanonicalizer(includes);
        while (arglist.peekFirst() != null) {
            String word = arglist.peekFirst();
            Cmd cmd = null;
            switch (word) {
                case FRAGMENT:
                case SCRIPT:
                case START:
                case RUN:
                case AWAIT:
                case STOP:
                case FORCE_STOP:
                case WAIT_MILLIS:
                    cmd = Cmd.parseArg(arglist, canonicalizer);
                    cmdList.add(cmd);
                    break;
                default:
                    Optional<Content<?>> scriptfile = NBIO.local()
                            .prefix("scripts/auto")
                            .name(word)
                            .extension("js")
                            .first();

                    //Script
                    if (scriptfile.isPresent()) {
                        arglist.removeFirst();
                        arglist.addFirst("scripts/auto/" + word);
                        arglist.addFirst("script");
                        cmd = Cmd.parseArg(arglist, canonicalizer);
                        cmdList.add(cmd);
                    } else if (NBCLIScenarioParser.isFoundWorkload(word, includes)) {
                        NBCLIScenarioParser.parseScenarioCommand(arglist, RESERVED_WORDS, includes);
                    } else {
                        throw new InvalidParameterException("unrecognized option:" + word);
                    }
                    break;
            }
        }

    }
}
