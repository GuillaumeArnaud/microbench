package fr.xebia.microbench

import fr.xebia.microbench.actors.Data
import fr.xebia.microbench.internals.Tempo
import groovy.transform.CompileStatic

import static fr.xebia.microbench.internals.Collectors.getDefaultCollector

// Test about Math.round
new Bench<Math>(
        tests: [{ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test, new MyRound()],
        data: new Data([[0.1f, 0.5f, 0.6f, 0.13431515313413145f]]),
        collector: defaultCollector,
        durationMs: 10000,
        tempo: Tempo.pacing.curry(1),
        vusers: 10,
        threads: 8
).start()

@CompileStatic
class MyRound implements Test<Math> {

    @Override
    void call(Math objectUnderTest, Collection<Object> data) {
        Math.round((Float) data.iterator().next())
    }
}