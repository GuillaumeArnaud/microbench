import groovyx.gpars.actor.Actor

import static Bench.pacing
import static java.lang.System.currentTimeMillis
import static java.lang.System.nanoTime
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench {
    long sampleSec = 1
    long sampleNs = SECONDS.toNanos(sampleSec)
    int nbUser = 10
    long durationMs = 10 * 1000

    Actor sampler = new Sampler(sampleNs: sampleNs)
    Closure test, tempo

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

    def static pacing = { long pacingInMillis, Closure test ->
        long elapsed = test()
        if (NANOSECONDS.toMillis(elapsed) < pacingInMillis) {
            sleep(pacingInMillis - NANOSECONDS.toMillis(elapsed))
        }
    }

    def start() {

        // initialisation of users
        def users = new Actor[nbUser]
        nbUser.times {
            int id ->
                users[id] = new User(tempo: tempo, measure: measure, test: test).start()
        }

        Actor feeder = new Feeder(users: users, durationMs: durationMs).start()

        sampler.start()

        println "[${ new Date(currentTimeMillis()) }] start the bench"
        warmup(1, test)

        nbUser.times { id -> feeder.send id }

        feeder.join()

        println "[${ new Date(currentTimeMillis()) }] stop the bench"

    }

    public static void main(String[] args) {
        Random rnd=new Random()

        def test = {-> Math.round(rnd.nextFloat()); }

        new Bench(
                test: test,
                nbUser: 1000,
                durationMs: SECONDS.toMillis(100),
                tempo: { Closure closure -> pacing(10, closure) }
        ).start()
    }
}
