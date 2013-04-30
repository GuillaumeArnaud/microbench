package fr.xebia.microbench

import groovy.transform.CompileStatic

// Test about Math.round

new Bench<Math>().with {
    //tests({ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test, new MyRound())
    tests(new MyRoundJava(), new MyRound(),new MyRoundJava(),new MyRound())
    data([0.1f], [0.5f], [0.6f], [0.13431515313413145f])
    defaultCollector()
    durationSec = 10000
    pause(2)
    vusers = 10
    threads = 8
    validate({ data, result -> if (data[0] == 0.1f) return result == 0 else true } as Validation)
    warmup(10000)
    start()
}

@CompileStatic
class MyRound implements Test<Math> {

    @Override
    Object call(Math objectUnderTest, Collection<Object> data) {
        Math.round((Float) data.iterator().next())
    }
}