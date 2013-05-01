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

import fr.xebia.gpressure.internals.Sample
import groovyx.gpars.actor.DefaultActor

import static fr.xebia.gpressure.Bench.getFlow
import static fr.xebia.gpressure.internals.Utils.unit

class Summarizer extends DefaultActor {
    protected float mean = 0, min = Float.MAX_VALUE, max = Float.MIN_VALUE
    protected long count = 0

    protected void act() {
        loop {
            react { Sample sample ->
                flow sample
                mean = (mean * count + sample.mean * sample.count) / (count + sample.count)
                min = Math.min(min, sample.min)
                max = Math.max(max, sample.max)
                count += sample.count
            }
        }
    }

    @Override
    public String toString() {
        return """
            Summary:
            mean = ${unit mean}
            count = ${count}
            min = ${unit min}
            max = ${unit max}
        """
    }
}

