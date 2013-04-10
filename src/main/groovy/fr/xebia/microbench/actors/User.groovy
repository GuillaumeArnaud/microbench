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
package fr.xebia.microbench.actors

import fr.xebia.microbench.Test
import fr.xebia.microbench.internals.Measure
import groovy.transform.CompileStatic
import groovyx.gpars.actor.DefaultActor

import static java.lang.System.nanoTime

/**
 * The User actor run a test iteration with tempo and measure (may be the user is a musician ?).
 */
class User<T> extends DefaultActor {

    private id
    private Sampler sampler
    private Validator validator
    private Closure tempo
    private Test<T> test
    private long iteration = 1
    private T objUnderTest

    protected void act() {
        loop {
            react { Collection<Object> data ->
                tempo(measure(test, data))
                reply id
            }
        }
    }

    @CompileStatic
    private long measure(Test<T> test, Collection<Object> data) {
        def result = null
        // init
        long start = nanoTime()
        // call
        //for (int i = 0; i < iteration; i++) result =
            test.call(objUnderTest, data)
        // measure
        long elapse = nanoTime() - start
        sampler.send new Measure(start, elapse)
        validator?.send(["data": data, "result": result])
        elapse
    }

}
