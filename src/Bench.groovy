import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import groovyx.gpars.actor.Actor
import groovyx.gpars.group.DefaultPGroup

import static java.lang.System.currentTimeMillis
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench<T> {
    long sampleSec = 1
    int vusers = 10
    long durationMs = 10 * 1000
    long iteration = 1

    Actor sampler
    Test<T> test
    T objectUnderTest

    // warmup the test method before the measurements
    def warmup = { duration, test ->
        println "[${new Date(currentTimeMillis())}] start warmup during $duration s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration * 1000) test(objectUnderTest)
        println "[${new Date(currentTimeMillis())}] end of warmup"
        test
    }

    Closure<Void> tempo = { long elapseInMs ->  }  // nothing to do

    static Closure<Void> pause = { long pauseInMs, elapseInMs ->
        sleep(pauseInMs)
    }

    static Closure<Void> pacing = { long pacingInMs, long elapseInMs ->
        sleep(Math.max(1l, pacingInMs - elapseInMs))
        return
    }

    def start() {
        // initialisation of the sampler
        sampler = new Sampler(sampleNs: SECONDS.toNanos(sampleSec), iteration: iteration)
        sampler.start()

        // initialisation of users
        def group = new DefaultPGroup(vusers)
        def users = new Actor[vusers]
        vusers.times {
            int id ->
                def user = new User<T>(tempo: tempo, sampler: sampler, iteration: iteration, test: test, objUnderTest: objectUnderTest)
                user.setParallelGroup(group)
                users[id] =  user.start()
        }

        // initialisation of feeders
        Actor feeder = new Feeder(users: users, durationMs: durationMs).start()



        println "[${ new Date(currentTimeMillis()) }] start the bench"
        warmup(1, test)

        vusers.times { id -> feeder.send id }

        feeder.join()

        println "[${ new Date(currentTimeMillis()) }] stop the bench"

    }

}
