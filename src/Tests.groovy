@groovy.lang.Grab(group = "log4j", module="log4j", version = "1.2.17")

import static Bench.pacing
import static java.util.concurrent.TimeUnit.SECONDS

class Tests {

    public static void testRound() {
        Random rnd = new Random()

        def test = {-> Math.round(rnd.nextFloat()); }

        new Bench(
                test: test,
                nbUser: 100,
                durationMs: SECONDS.toMillis(10),
                tempo: pacing.curry(10)
        ).start()
    }

    public static void testLog4j() {

        def logger = org.apache.log4j.Logger.getLogger(this.class)
        Closure<Boolean> test = {-> logger.isDebugEnabled() }

        new Bench(
                test: test,
                nbUser: 50,
                durationMs: SECONDS.toMillis(10),
                sampleSec: 1,
                iteration: 1,
                tempo:  pacing.curry(5)
                //tempo: { Closure closure -> pause(0, closure) }
        ).start()
    }

    public static void main(String[] args) {
        testLog4j()
        testRound()
    }
}
