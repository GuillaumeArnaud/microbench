/*
 * Copyright 2008-2012 Xebia and the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.gpressure.actors

import fr.xebia.gpressure.Data
import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

import static fr.xebia.gpressure.Bench.debug
import static java.lang.System.currentTimeMillis

class Feeder extends DefaultActor {

    private long startMs
    private Actor[] users = null
    private durationMs
    private Data data

    protected void act() {
        startMs = currentTimeMillis()
        loop {
            react { int id ->
                if (currentTimeMillis() - startMs > durationMs) {
                    // end of the benchmark.
                    // terminate all user actors
                    users.each { Actor user -> user.terminate(); }
                    users*.join()
                    debug.send "stop feeding the vusers";
                    terminate();
                } else users[id].send data.next()
            }
        }
    }

    @Override
    protected void handleException(Throwable exception) {
        exception.printStackTrace()
    }

}
