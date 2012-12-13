import groovy.transform.CompileStatic
import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

import static java.lang.System.nanoTime

/**
 * The User actor run a test iteration with tempo and measure (may be the user is a musician ?).
 */
class User<T> extends DefaultActor {

    private id
    private Actor sampler
    private Closure tempo
    private Test<T> test
    private long iteration = 1
    private T objUnderTest

    protected void act() {
        loop {
            react { Collection<Object> data ->
                tempo(measure(test, data))
                reply id
            }
        }
    }

    @CompileStatic
    private long measure(Test<T> test, Collection<Object> data) {
        def result
        // init
        long start = nanoTime()
        // call
        for (int i = 0; i < iteration; i++) test.call(objUnderTest, data)
        // measure
        long elapse = nanoTime() - start
        sampler.send new Measure(start, elapse)
        elapse
    }

}
