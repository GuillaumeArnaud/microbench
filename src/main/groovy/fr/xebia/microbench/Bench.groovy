package fr.xebia.microbench

import fr.xebia.microbench.actors.*
import groovyx.gpars.actor.Actor
import groovyx.gpars.group.DefaultPGroup

import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean

import static fr.xebia.microbench.internals.Utils.prettyBytes
import static java.lang.Math.round
import static java.lang.Runtime.getRuntime
import static java.lang.System.currentTimeMillis
import static java.lang.management.ManagementFactory.getClassLoadingMXBean
import static java.lang.management.ManagementFactory.getClassLoadingMXBean
import static java.lang.management.ManagementFactory.getCompilationMXBean
import static java.lang.management.ManagementFactory.getGarbageCollectorMXBeans
import static java.util.concurrent.TimeUnit.NANOSECONDS
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
    T objectUnderTest
    long warmupMs = 1000
    Timer timer = new Timer()

    ////////////////////////////////////////////////////
    //                      Warmup                    //
    ////////////////////////////////////////////////////
    // warmup the test method before the measurements
    private Closure<Void> defaultWarmup = { long warmupMs, Test<T> test, T objectUnderTest, Data data, Closure<Map<String, Object>> collector ->
        println "[${new Date(currentTimeMillis())}] start warmup during ${warmupMs}ms"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < warmupMs) {
            test.call(objectUnderTest, data.next())
        }
        data.reset()
        println "[${new Date(currentTimeMillis())}] end of warmup"
        return
    }

    public Closure<Void> warmup = defaultWarmup.curry(warmupMs)

    public void warmup(long warmupMs) { warmup = defaultWarmup.curry(warmupMs) }

    ////////////////////////////////////////////////////
    //                      Tempo                     //
    ////////////////////////////////////////////////////
    public Closure<Void> tempo = { long elapseMs -> return }  // nothing to do

    private Closure<Void> pause = { long pauseMs, elapseNs ->
        sleep(pauseMs)
        return
    }

    public Closure<Void> pause(long pauseMs) {
        tempo = pause.curry(pauseMs)
    }

    private Closure<Void> pacing = { long pacingMs, long elapseNs ->
        sleep(Math.max(1l, pacingMs - NANOSECONDS.toMillis(elapseNs)))
        return
    }

    public Closure<Void> pacing(long pacingMs) {
        tempo = pacing.curry(pacingMs)
    }

    ////////////////////////////////////////////////////
    //                      Data                      //
    ////////////////////////////////////////////////////
    Data data = new Data([])

    public void data(Collection<Object>... data) {
        this.data = new Data(data)
    }

    ////////////////////////////////////////////////////
    //                      Test                      //
    ////////////////////////////////////////////////////
    private Test<T>[] tests = []

    public void tests(Test<T>... tests) {
        this.tests = tests
    }

    ////////////////////////////////////////////////////
    //                 Collector                      //
    ////////////////////////////////////////////////////
    public Closure<Map<String, Object>> collector = null

    private Closure<Map<String, Object>> detailedCollector = {
        def collectedData = defaultCollector.call()
        collectedData["gc"] = [:]
        garbageCollectorMXBeans.each { GarbageCollectorMXBean bean ->
            collectedData["gc"][bean.name] = [:]
            collectedData["gc"][bean.name]["count"] = bean.collectionCount
            collectedData["gc"][bean.name]["time"] = bean.collectionTime
            if (bean instanceof com.sun.management.GarbageCollectorMXBean) {
                collectedData["gc"][bean.name]["duration"] = ((com.sun.management.GarbageCollectorMXBean) bean).lastGcInfo?.duration
            }
        }
        collectedData
    }

    private Closure<Map<String, Object>> defaultCollector = {
        Map<String, Object> collectedData = new HashMap<String, Object>()
        collectedData["memory"] = [
                "used (%)": round(100 * (getRuntime().totalMemory() - getRuntime().freeMemory()) / getRuntime().totalMemory()),
                "total (Byte)": getRuntime().totalMemory() - getRuntime().freeMemory(),
                "used (Byte)": getRuntime().totalMemory() - getRuntime().freeMemory()
        ]
        collectedData["thread"] = ["thread": Thread.activeCount()]
        collectedData["class"] = [
                "loaded (current)": classLoadingMXBean.loadedClassCount,
                "loaded (total)": classLoadingMXBean.totalLoadedClassCount
        ]
        collectedData["compilation"] = [:]
        compilationMXBean.each { CompilationMXBean compilationMXBean ->
            collectedData["compilation"][compilationMXBean.name] = ["total (ms)": compilationMXBean.totalCompilationTime]
        }
        collectedData
    } as Closure<Map<String, Object>>;

    public void defaultCollector() { collector = defaultCollector }

    public void detailedCollector() { collector = detailedCollector }

    ////////////////////////////////////////////////////
    //                     Run                        //
    ////////////////////////////////////////////////////
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
        warmup.call(test, objectUnderTest, data, collector)

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
