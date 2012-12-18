package fr.xebia.microbench.actors

import fr.xebia.microbench.internals.Measure
import fr.xebia.microbench.internals.Sample
import groovyx.gpars.actor.DefaultActor

class Sampler extends DefaultActor {
    private Collection<Measure> measures = []
    private Measure firstSample = null
    private long sampleNs = 0
    private long iteration = 1
    public Summary summary

    protected void act() {
        loop {
            react { Measure measure ->
                if (measures.size() == 0) {
                    measures.add(measure)
                    firstSample = measures[0]
                } else if (measure.start - firstSample.start > sampleNs) {
                    Sample sample = new Sample(measures, iteration)
                    println sample
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
