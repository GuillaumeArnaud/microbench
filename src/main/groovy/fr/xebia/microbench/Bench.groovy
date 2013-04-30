/*
 * Copyright 2012-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package fr.xebia.microbench

import fr.xebia.microbench.actors.*
import groovy.transform.TypeChecked
import groovyx.gpars.actor.Actor
import groovyx.gpars.group.DefaultPGroup

import java.lang.management.CompilationMXBean
import java.lang.management.GarbageCollectorMXBean

import static Level.*
import static Logger.currentLevel
import static fr.xebia.microbench.internals.Utils.prettyBytes
import static java.lang.Math.round
import static java.lang.Runtime.getRuntime
import static java.lang.System.currentTimeMillis
import static java.lang.management.ManagementFactory.*
import static java.util.concurrent.TimeUnit.NANOSECONDS
import static java.util.concurrent.TimeUnit.SECONDS

public class Bench<T> {
    int sampleIntervalSec = 1
    int collectorIntervalSec = 5
    int vusers = 1
    long durationMs = 10 * 1000
    long iteration = 1
    int threads = getRuntime().availableProcessors() * 2 + 1
    T objectUnderTest
    private Timer timer = new Timer()
    long warmupSec = 5

    ////////////////////////////////////////////////////
    //                     Run                        //
    ////////////////////////////////////////////////////

    /**
     * Start the whole bench.
     */
    public void start() {
        // display context
        context()
        // schedule the collector of JVM environment
        if (collector) timer.schedule({ flow.send collector.call() } as TimerTask, 0, SECONDS.toMillis(collectorIntervalSec))

        int i = 1
        for (Test<T> test : tests) {
            info.send ">>> Test ${i++}"
            run(test)
        }

        timer.cancel()
    }

    /**
     * Run a test defined in the bench.
     *
     * @param test the test to run.
     */
    private void run(Test<T> test) {
        warmup.call(test, objectUnderTest, data, collector)

        // initialisation of the summary
        summarizer = new Summarizer().start() as Summarizer

        // initialisation of the sampler
        sampler = new Sampler(sampleIntervalNs: SECONDS.toNanos(sampleIntervalSec), iteration: iteration, summary: summarizer).start() as Sampler

        // initialisation of users
        def group = new DefaultPGroup(threads)
        def users = new Actor[vusers]
        vusers.times {
            int id ->
                def user = new User<T>(id: id, tempo: tempo, sampler: sampler, iteration: iteration, test: test, objUnderTest: objectUnderTest, validator: validator)
                user.setParallelGroup(group)
                users[id] = user.start()
        }

        // initialisation of feeders
        Actor feeder = new Feeder(users: users, durationMs: durationMs, data: data).start()

        info.send "start the bench"

        vusers.times { id -> feeder.send id }

        feeder.join()

        info.send "stop the bench"
        sleep(100)
        info.send "error(s): ${validator?.errors?.get()}"
        sleep(100)
        info.send summarizer
    }

    ///////////////////////////////////////////////////////
    //         Technical variables and methods           //
    ///////////////////////////////////////////////////////

    /**
     * Set the interval between two samples.
     * @param sampleIntervalSec interval in second
     */
    public void sampleInterval(int sampleIntervalSec = 1) { this.sampleIntervalSec = sampleIntervalSec }

    /**
     * Set the interval between two collects.
     * @param collectorIntervalSec interval in second
     */
    public void collectorInterval(int collectorIntervalSec = 5) { this.collectorIntervalSec = collectorIntervalSec }

    /**
     * Number of parallel virtual users.
     * @param vusers number of users
     */
    public void vusers(int vusers = 1) { this.vusers = vusers }

    /**
     * Duration of the test.
     * @param durationSec duration in second
     */
    public void duration(long durationSec = 10) { this.durationMs = SECONDS.toMillis(durationSec) }

    /**
     * Number of iteration at each run. By default the value is 1, if this number increases, a risk of inlining exists.
     * @param iteration number of iteration
     */
    public void iteration(long iteration = 1) { this.iteration = iteration }

    /**
     * Number of thread available in the pool.
     * @param threads number of thread in the pool
     */
    public void threads(int threads = 3) { this.threads = threads }

    /**
     * The warump duration before the test.
     * @param warmupSec the warmup in second
     */
    public void warmupDuration(int warmupSec = 1) { this.warmupSec = SECONDS.toMillis(warmupSec) }

    private Sampler sampler
    private Summarizer summarizer

    static Logger info = new Logger(level: INFO).start() as Logger
    static Logger debug = new Logger(level: DEBUG).start() as Logger
    static Logger error = new Logger(level: ERROR).start() as Logger
    static Logger flow = new Logger(level: FLOW).start() as Logger

    public static void info() { currentLevel = Level.INFO }

    public static void debug() { currentLevel = Level.DEBUG }

    public static void error() { currentLevel = Level.ERROR }

    public static void flow() { currentLevel = Level.FLOW }

    ////////////////////////////////////////////////////
    //                      Warmup                    //
    ////////////////////////////////////////////////////
    // warmup the test method before the measurements
    private Closure<Void> defaultWarmup = { long warmupMs, Test<T> test, T objectUnderTest, Data data, Closure<Map<String, Object>> collector ->
        info.send "start warmup during ${warmupMs} ms"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < warmupMs) {
            test.call(objectUnderTest, data.next())
        }
        data.reset()
        info.send "end of warmup"
        return
    }

    public Closure<Void> warmup = defaultWarmup.curry(SECONDS.toMillis(warmupSec))

    public void warmup(int warmupSec) { warmup = defaultWarmup.curry(SECONDS.toMillis(warmupSec)); this.warmupSec = warmupSec }

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
    private Data data = new Data([])

    public void data(Collection<Object>... data) {
        this.data = new Data(data)
    }

    ////////////////////////////////////////////////////
    //                    Validator                   //
    ////////////////////////////////////////////////////
    private Validator validator = null

    public void validate(Validation validation) {
        if (validation) validator = new Validator(validation: validation).start() as Validator
    }

    ////////////////////////////////////////////////////
    //                      Test                      //
    ////////////////////////////////////////////////////
    private Test<T>[] tests = []

    @TypeChecked
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



    private void context() {
        info.send """
            Context

            JVM: ${System.getProperty('java.vm.name')} - ${System.getProperty('java.vm.vendor')} - ${System.getProperty('java.version')} - max heap size = ${prettyBytes(getRuntime().maxMemory())}
            OS: ${System.getProperty('os.name')} - ${System.getProperty('os.arch')} - ${getRuntime().availableProcessors()} processors
            User: ${durationMs}ms - ${vusers} vuser(s) - sample of ${sampleIntervalSec}s - warmup of ${warmupSec}ms - $threads thread(s) for vusers
        """
    }

}
