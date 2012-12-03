import com.thoughtworks.xstream.XStream

import static java.lang.System.nanoTime

long NANO = 1000000
long SAMPLE_SEC = 5
Collection<Measure> measures = []
Measure firstSample = null

def timer = { closure ->
    // init
    long start = nanoTime()
    // call
    closure.call()
    // measure
    long elapse = nanoTime() - start
    if (measures.size() == 0) {
        measures << new Measure(start: start, elapse: elapse)
        firstSample = measures[0]
    } else if (start - firstSample.start > SAMPLE_SEC * 1000 * NANO) {
        println measures.collect { Measure m -> m.elapse }.sum() / (measures.size() * NANO) + "ms"
        measures = []
        firstSample = new Measure(start: start, elapse: elapse)
        measures << firstSample
    } else {
        measures << new Measure(start: start, elapse: elapse)
    }
}

1000000.times {
    //timer { new XStream() }
    timer { thisObject.getClass().getClassLoader().loadClass("java.math.BigDecimal") }
}

private class Measure {
    protected long start
    protected long elapse
}

