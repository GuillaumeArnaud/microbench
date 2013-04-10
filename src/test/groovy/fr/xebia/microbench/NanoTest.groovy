package fr.xebia.microbench

import groovy.transform.CompileStatic

new Bench<Object>().with {
    tests(new Nano(),{o,data-> System.nanoTime()} as Test<System>)
    durationMs = 60000
    flow()
    start()
}

@CompileStatic
class Nano implements Test<Object> {


    @Override
    Object call(Object objectUnderTest, Collection<Object> data) {
        System.nanoTime()
        return null
    }
}

