import groovyx.gpars.actor.DefaultActor

protected class Sampler extends DefaultActor {
    private Collection<Measure> measures = []
    private Measure firstSample = null
    private long sampleNs = 0
    private long iteration = 1

    protected void act() {
        loop {
            react { Measure m ->
                if (measures.size() == 0) {
                    measures.add(m)
                    firstSample = measures[0]
                } else if (m.start - firstSample.start > sampleNs) {
                    Sample sample = new Sample(measures, iteration)
                    println sample
                    firstSample = m
                    measures = [m]
                } else {
                    measures << m
                }
            }
        }
    }
}
