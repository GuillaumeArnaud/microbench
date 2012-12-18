package fr.xebia.microbench

import fr.xebia.microbench.actors.Data
import fr.xebia.microbench.actors.Feeder
import fr.xebia.microbench.actors.Sampler
import fr.xebia.microbench.actors.Summary
import fr.xebia.microbench.actors.User

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
    int threads = getRuntime().availableProcessors() * 2 + 1

    Sampler sampler
    Summary summary
    Collection<Test<T>> tests
    T objectUnderTest
    Data data = new Data([])
    long warmupMs = 1000
    Timer timer = new Timer()

    // warmup the test method before the measurements
    Closure<Void> warmup = { long warmupMs, Test<T> test, T objectUnderTest, Data data, Closure<Map<String, Object>> collector ->
        println "[${new Date(currentTimeMillis())}] start warmup during ${warmupMs}s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < warmupMs) {
            test.call(objectUnderTest, data.next())
        }
        data.reset()
        println "[${new Date(currentTimeMillis())}] end of warmup"
        return
    }

    Closure<Void> tempo = { long elapseInMs -> return }  // nothing to do

    Closure<Map<String, Object>> collector = null

    public void start() {
        // display context
        context()
        // schedule the collector of JVM environment
        if (collector) timer.schedule({ println collector.call() } as TimerTask, 0, SECONDS.toMillis(sampleCollectorSec))

        int i = 1
        for (Test<T> test : tests) {
            println "test ${i++}"
            call(test)
        }

        timer.cancel()
    }

    public void call(Test<T> test) {
        warmup.call(warmupMs, test, objectUnderTest, data, collector)

        // initialisation of the summary
        summary = new Summary().start() as Summary

        // initialisation of the sampler
        sampler = new Sampler(sampleNs: SECONDS.toNanos(sampleSec), iteration: iteration, summary: summary).start() as Sampler

        // initialisation of users
        def group = new DefaultPGroup(threads)
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
        println summary
    }

    public void context() {
        println """
            Context

            JVM: ${System.getProperty('java.vm.name')} - ${System.getProperty('java.vm.vendor')} - ${System.getProperty('java.version')} - max heap size = ${prettyBytes(getRuntime().maxMemory())}
            OS: ${System.getProperty('os.name')} - ${System.getProperty('os.arch')} - ${getRuntime().availableProcessors()} processors
            User: ${durationMs}ms - ${vusers} vuser(s) - sample of ${sampleSec}s - warmup of ${warmupMs}ms - $threads thread(s) for vusers
        """
    }

}
