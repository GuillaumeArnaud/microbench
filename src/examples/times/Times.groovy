import fr.xebia.microbench.Bench
import fr.xebia.microbench.Test
import groovy.transform.CompileStatic

new Bench().with {
    tests({ o, d -> System.nanoTime() } as Test, new NanoTime(), new CurrentTimeInMillis())
    start()
}

new Bench().with {
    tests({ o,d -> System.nanoTime() } as Test)
    vusers = 5
    warmup(2000)
    pacing(5)
    defaultCollector()
    start()
}

@CompileStatic
class CurrentTimeInMillis implements Test<Object> {
    @Override
    Object call(Object objectUnderTest, Collection<Object> data) {
        return System.nanoTime()
    }
}

@CompileStatic
class NanoTime implements Test<Object> {
    @Override
    Object call(Object objectUnderTest, Collection<Object> data) {
        return System.nanoTime()
    }
}