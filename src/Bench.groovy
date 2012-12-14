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
    Collection<Test<T>> tests
    T objectUnderTest
    Data data = new Data([])
    long warmupMs = 1000

    // warmup the test method before the measurements
    def warmup = { long duration, Test<T> test ->
        println "[${new Date(currentTimeMillis())}] start warmup during $duration s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration) test.call(objectUnderTest, data.next())
        data.reset()
        println "[${new Date(currentTimeMillis())}] end of warmup"
    }

    Closure<Void> tempo = { long elapseInMs -> return }  // nothing to do

    static Closure<Void> pause = { long pauseInMs, elapseInMs ->
        sleep(pauseInMs)
        return
    }

    static Closure<Void> pacing = { long pacingInMs, long elapseInMs ->
        sleep(Math.max(1l, pacingInMs - elapseInMs))
        return
    }

    def start() {
        if (tests != null) {
            for(Test<T> test:tests) { call(test) }
        } else call(test)
    }

    def call(Test<T> test) {
        warmup(warmupMs, test)

        // initialisation of the sampler
        sampler = new Sampler(sampleNs: SECONDS.toNanos(sampleSec), iteration: iteration)
        sampler.start()

        // initialisation of users
        def group = new DefaultPGroup(vusers)
        def users = new Actor[vusers]
        vusers.times {
            int id ->
                def user = new User<T>(id: id, tempo: tempo, sampler: sampler, iteration: iteration, test: test, objUnderTest: objectUnderTest)
                user.setParallelGroup(group)
                users[id] = user.start()
        }

        // initialisation of feeders
        Actor feeder = new Feeder(users: users, durationMs: durationMs, data: data).start()

        println "[${ new Date(currentTimeMillis()) }] start the bench"

        vusers.times { id -> feeder.send id }

        feeder.join()

        println "[${ new Date(currentTimeMillis()) }] stop the bench"

    }

}
