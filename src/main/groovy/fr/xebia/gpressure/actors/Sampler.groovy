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

import fr.xebia.gpressure.internals.Measure
import fr.xebia.gpressure.internals.Sample
import groovyx.gpars.actor.DefaultActor

class Sampler extends DefaultActor {
    private Collection<Measure> measures = []
    private Measure firstSample = null
    private long sampleIntervalNs = 0
    private long iteration = 1
    public Summarizer summary

    protected void act() {
        loop {
            react { Measure measure ->
                if (measures.size() == 0) {
                    measures.add(measure)
                    firstSample = measures[0]
                } else if (measure.start - firstSample.start > sampleIntervalNs) {
                    Sample sample = new Sample(measures, iteration)
                    firstSample = measure
                    measures = [measure]
                    summary.send sample
                } else {
                    measures << measure
                }
            }
        }
    }
}
