import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.Actors

import static java.lang.System.currentTimeMillis
import static java.lang.System.nanoTime
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS

long sampleSec = 1
long sampleNano = SECONDS.toNanos(sampleSec)
int nbUser = 10
Actor[] users = new Actor[nbUser]

Actor sampler = Actors.actor {
    Collection<Measure> measures = []
    Measure firstSample = null
    loop {
        react { Measure m ->
            if (measures.size() == 0) {
                measures.add(m)
                firstSample = measures[0]
            } else if (m.start - firstSample.start > sampleNano) {
                Sample sample = new Sample(measures)
                println sample
                firstSample = m
                measures = [m]
            } else {
                measures << m
            }
        }
    }
}

def measure = { test ->
    {->
        // init
        long start = nanoTime()
        // call
        test.call()
        // measure
        long elapse = nanoTime() - start
        sampler.send new Measure(start: start, elapse: elapse)
        elapse
    }
}

// warmup the test method before the measurements
def warmup = { duration, test ->
    println "[${new Date(currentTimeMillis())}] start warmup during $duration s"
    long start = currentTimeMillis()
    while (currentTimeMillis() - start < duration * 1000) test()
    println "[${new Date(currentTimeMillis())}] end of warmup"
    test
}

def pause = { long pauseInMillis, Closure test ->
    test()
    sleep(pauseInMillis)
}

def pacing = { long pacingInMillis, Closure test ->
    long elapsed = test()
    if (NANOSECONDS.toMillis(elapsed) < pacingInMillis) {
        sleep(pacingInMillis - NANOSECONDS.toMillis(elapsed))
    }
}

long devnull = 0
def test = {-> devnull = nanoTime(); sleep(1); }


Actor feeder = Actors.actor {
    long start = currentTimeMillis()
    loop {
        react { int id ->
            if (currentTimeMillis() - start > 10 * 1000) {
                users.each { Actor user -> user.send(-1) }
                println "[${new Date(currentTimeMillis())}] terminate";
                terminate();
            } else users[id].send id
        }
    }
}

def user = {
    loop {
        react { idsnd ->
            if (idsnd < 0) terminate()
            else {
                pacing(0, measure(test))
                feeder.send(id)
            }
        }
    }
}

// init users
nbUser.times { int id ->
    users[id] = Actors.actor user
}


println "[${new Date(currentTimeMillis())}] start the bench"
warmup(1, test)

nbUser.times { id -> feeder.send id }

feeder.join()

println "[${new Date(currentTimeMillis())}] ${new Date(currentTimeMillis())} stop the bench"

private class Measure {
    protected long start
    protected long elapse
}

protected class Sample {
    protected float mean, min, max, count
    public long NANO = 1000000

    protected Sample(Collection<Measure> measures) {
        def elapsed = measures.collect { Measure m -> m.elapse / NANO }
        count = measures.size()
        mean = elapsed.sum() / count
        min = elapsed.min()
        max = elapsed.max()
    }

    public String toString() { "mean=$mean ms, min=$min ms, max=$max ms, count=$count" }
}

