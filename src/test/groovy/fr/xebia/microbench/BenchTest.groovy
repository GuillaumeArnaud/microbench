package fr.xebia.microbench

import fr.xebia.microbench.internals.Level
import groovy.transform.CompileStatic

// Test about Math.round

new Bench<Math>().with {
    tests({ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test, new MyRound())
    data([0.1f], [0.5f], [0.6f], [0.13431515313413145f])
    defaultCollector()
    durationMs = 10000
    pause(2)
    vusers = 10
    threads = 8
    flow()
    warmup(1000)
    start()
}

@CompileStatic
class MyRound implements Test<Math> {

    @Override
    void call(Math objectUnderTest, Collection<Object> data) {
        Math.round((Float) data.iterator().next())
    }
}