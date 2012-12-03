import com.thoughtworks.xstream.XStream

import static java.lang.System.currentTimeMillis
import static java.lang.System.nanoTime

public long NANO = 1000000
long SAMPLE_SEC = 1
Collection<Measure> measures = []
Collection<Sample> samples = []
Measure firstSample = null


def measure = { closure ->
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
        Sample sample = new Sample(measures)
        println sample
        measures = []
        firstSample = new Measure(start: start, elapse: elapse)
        measures << firstSample
    } else {
        measures << new Measure(start: start, elapse: elapse)
    }
}

// keyword for DSL
def after = {closure -> closure}

// DSL for how to stop the tests
def end = { closure -> closure() }

// set the number of iteration by user (argument of end method)
def iterations = {iter, closure -> {-> iter.times{ measure closure }} }

// fix a test duration for a user (argument of end method)
def during = { duration, closure ->
    {->
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration * 1000) measure closure
    }
}

// warmup the test method before the measurements
def warmup = { duration, closure ->
    println "start warmup during $duration s"
    long start = currentTimeMillis()
    while (currentTimeMillis() - start < duration * 1000) closure
    println "end of warmup"
    closure
}

long devnull = 0
def test = {-> devnull = nanoTime(); sleep(1) }

println "${currentTimeMillis()} start the bench"
end iterations(10,after(warmup(3, test)))
println "${currentTimeMillis()} stop the bench"

private class Measure {
    protected long start
    protected long elapse
}

private class Sample {
    protected long mean,min,max,count
    public long NANO = 1000000

    private Sample(Collection<Measure> measures){
        def elapsed=measures.collect { Measure m -> m.elapse/NANO }
        count=measures.size()
        mean=elapsed.sum() /count
        min=elapsed.min()
        max=elapsed.max()
    }

   public String toString(){ "mean=$mean ms, min=$min ms, max=$max ms, count=$count" }
}

