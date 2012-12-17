package fr.xebia.microbench

import fr.xebia.microbench.actors.Data
import fr.xebia.microbench.actors.Feeder
import fr.xebia.microbench.actors.Sampler
import fr.xebia.microbench.actors.User
import fr.xebia.microbench.internals.WarmUp
import groovyx.gpars.actor.Actor
import groovyx.gpars.group.DefaultPGroup

import static fr.xebia.microbench.internals.Utils.prettyBytes
import static java.lang.Runtime.getRuntime
import static java.lang.System.currentTimeMillis
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench<T> {
    long sampleSec = 1
    long sampleCollectorSec = 5
    int vusers = 1
    long durationMs = 10 * 1000
    long iteration = 1

    Actor sampler
    Collection<Test<T>> tests
    T objectUnderTest
    Data data = new Data([])
    long warmupMs = 1000
    Timer timer = new Timer()

    // warmup the test method before the measurements
    WarmUp<T> warmup

    Closure<Void> tempo = { long elapseInMs -> return }  // nothing to do

    Closure<Map<String, Object>> collector = null

    def start() {
        println "JVM: ${System.getProperty('java.vm.name')} - ${System.getProperty('java.vm.vendor')} - ${System.getProperty('java.version')} - max heap size = ${prettyBytes(getRuntime().maxMemory())}"
        println "OS: ${System.getProperty('os.name')} - ${System.getProperty('os.arch')} - ${getRuntime().availableProcessors()} processors"
        if (collector) timer.schedule({ println collector.call() } as TimerTask, 0, SECONDS.toMillis(sampleCollectorSec))

        warmup = new WarmUp<>(objectUnderTest: objectUnderTest, data: data, duration: warmupMs, collector: collector)

        int i = 1
        for (Test<T> test : tests) {
            warmup.run(test)
            println "test $i:"
            call(test)
        }

        timer.cancel()
    }

    def call(Test<T> test) {
        warmup.run(test)

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
