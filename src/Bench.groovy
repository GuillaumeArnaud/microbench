import groovyx.gpars.actor.Actor

import static java.lang.System.currentTimeMillis
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench {
    long sampleSec = 1
    int nbUser = 10
    long durationMs = 10 * 1000
    long iteration = 1

    Actor sampler
    Closure test

    // warmup the test method before the measurements
    def warmup = { duration, test ->
        println "[${new Date(currentTimeMillis())}] start warmup during $duration s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration * 1000) test()
        println "[${new Date(currentTimeMillis())}] end of warmup"
        test
    }

    def tempo = { test -> test() }

    def static pause = { long pauseInMillis, Closure test ->
        test()
        sleep(pauseInMillis)
    }

    def static pacing = { long pacingInMs, Closure measure ->
        long elapsed = NANOSECONDS.toMillis(measure())
        if (elapsed < pacingInMs) sleep(Math.max(1l, pacingInMs - elapsed))
    }

    def start() {
        // initialisation of the sampler
        sampler = new Sampler(sampleNs: SECONDS.toNanos(sampleSec), iteration: iteration)
        sampler.start()

        // initialisation of users
        def users = new Actor[nbUser]
        nbUser.times {
            int id ->
                users[id] = new User(tempo: tempo, sampler: sampler, iteration: iteration, test: test).start()
        }

        // initialisation of feeders
        Actor feeder = new Feeder(users: users, durationMs: durationMs).start()



        println "[${ new Date(currentTimeMillis()) }] start the bench"
        warmup(1, test)

        nbUser.times { id -> feeder.send id }

        feeder.join()

        println "[${ new Date(currentTimeMillis()) }] stop the bench"

    }

}
