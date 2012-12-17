package fr.xebia.microbench.internals

import fr.xebia.microbench.Test
import fr.xebia.microbench.actors.Data

import static java.lang.System.currentTimeMillis

class WarmUp<T> {
    T objectUnderTest
    Data data = new Data([])
    long duration
    Closure<Map<String, Object>> collector = null

    public void run(Test<T> test) {
        println "[${new Date(currentTimeMillis())}] start warmup during ${duration}s"
        long start = currentTimeMillis()
        while (currentTimeMillis() - start < duration) {
            test.call(objectUnderTest, data.next())
        }
        data.reset()
        println "[${new Date(currentTimeMillis())}] end of warmup"
    }
}
