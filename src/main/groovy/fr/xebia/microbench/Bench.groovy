package fr.xebia.microbench

import groovy.transform.CompileStatic
import groovyx.gpars.actor.Actor
import groovyx.gpars.group.DefaultPGroup
import sun.management.MemoryImpl

import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean
import java.lang.management.ManagementFactory
import java.lang.management.MemoryMXBean
import java.util.concurrent.Executors

import static java.lang.Math.round
import static java.lang.Runtime.getRuntime
import static java.lang.System.currentTimeMillis
import static java.lang.System.getProperty
import static java.lang.System.getenv
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench<T> {
    long sampleSec = 1
    long sampleCollectorSec = 5
    int vusers = 10
    long durationMs = 10 * 1000
    long iteration = 1

    Actor sampler
    Collection<Test<T>> tests
    T objectUnderTest
    Data data = new Data([])
    long warmupMs = 1000
    Timer timer = new Timer()

    // warmup the test method before the measurements
    def warmup = { long duration, Test<T> test ->
        println "[${new Date(currentTimeMillis())}] start warmup during $duration s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration) test.call(objectUnderTest, data.next())
        data.reset()
        println "[${new Date(currentTimeMillis())}] end of warmup"
    }

    public static String prettyBytes(Long bytes) {
        if (bytes > 1024 * 1024 * 1024) "${round(bytes / (1024 * 1024 * 1024))}GB"
        else if (bytes > 1024 * 1024) "${round(bytes / (1024 * 1024))}MB"
        else if (bytes > 1024) "${round(bytes / (1024))}KB"
        else "${round(bytes / (1024))}B"
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

    static Closure<HashMap<String, Object>> defaultCollector = {
        Map<String, Object> collectedData = new HashMap<String, Object>()
        collectedData["memory - used (%)"] = round(100 * (getRuntime().totalMemory() - getRuntime().freeMemory()) / getRuntime().totalMemory())
        collectedData["memory - used"] = prettyBytes(getRuntime().totalMemory() - getRuntime().freeMemory())
        collectedData["memory - total"] = prettyBytes(getRuntime().totalMemory())
        collectedData["thread - active"] = Thread.activeCount()
        collectedData
    }

    static Closure<HashMap<String, Object>> detailledCollector = {
        def collectedData = defaultCollector.call()
        ManagementFactory.garbageCollectorMXBeans.each { GarbageCollectorMXBean bean ->
            collectedData[bean.getName() + "-count"] = bean.collectionCount
            collectedData[bean.getName() + "-time"] = bean.collectionTime
            if (bean instanceof com.sun.management.GarbageCollectorMXBean) {
                collectedData[bean.getName() + "-duration"] = ((com.sun.management.GarbageCollectorMXBean) bean).lastGcInfo.duration
            }
        }
        ManagementFactory.compilationMXBean.each {CompilationMXBean bean ->
             collectedData[bean.getName()+"-compilation time"] = bean.totalCompilationTime
        }
        collectedData["class - loaded"] = ManagementFactory.classLoadingMXBean.loadedClassCount
        collectedData["class - total loaded"] = ManagementFactory.classLoadingMXBean.totalLoadedClassCount
        collectedData
    }

    def collector = defaultCollector

    def start() {
        println "JVM: ${System.getProperty('java.vm.name')} - ${System.getProperty('java.vm.vendor')} - ${System.getProperty('java.version')} - max heap size = ${prettyBytes(getRuntime().maxMemory())}"
        println "OS: ${System.getProperty('os.name')} - ${System.getProperty('os.arch')} - ${getRuntime().availableProcessors()} processors"
        timer.schedule({ println collector() } as TimerTask, 0, SECONDS.toMillis(sampleCollectorSec))

        int i = 1
        for (Test<T> test : tests) {
            println "test $i:"
            call(test)
        }

        timer.cancel()
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
