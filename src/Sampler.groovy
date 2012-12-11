import groovyx.gpars.actor.DefaultActor

class Sampler extends DefaultActor {
    private Collection<Measure> measures = []
    private Measure firstSample = null
    private long sampleNs = 0
    private long iteration = 1

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
                } else {
                    measures << measure
                }
            }
        }
    }
}
