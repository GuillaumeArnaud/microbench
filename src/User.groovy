import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

import static java.lang.System.nanoTime

/**
 * The User actor run a test iteration with tempo and measure (may be the user is a musician ?).
 */
class User extends DefaultActor {

    private Actor sampler
    private Closure tempo, test
    private long iteration = 1

    protected void act() {
        loop {
            react { userid ->
                if (userid < 0) terminate()
                else {
                    tempo(measure(test))
                    reply userid
                }
            }
        }
    }

    def measure = { test ->
        {->
            def result
            // init
            long start = nanoTime()
            // call
            iteration.times { result = test.call() }
            // measure
            long elapse = nanoTime() - start
            sampler.send new Measure(start: start, elapse: elapse)
            elapse
        }
    }

}
