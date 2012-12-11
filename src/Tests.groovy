import groovy.transform.CompileStatic
@groovy.lang.Grapes(
@Grab(group = "log4j", module = "log4j", version = "1.2.17")
)
import org.apache.log4j.Logger

import static Bench.pacing
import static java.util.concurrent.TimeUnit.SECONDS

class Tests {

    public static void testRound() {
        Random rnd = new Random()

        def test = { Math.round(rnd.nextFloat()); } as Test

        new Bench(
                test: test,
                vusers: 100,
                durationMs: SECONDS.toMillis(10),
                tempo: pacing.curry(10)
        ).start()
    }

    @CompileStatic
    public static void testLog4j() {

        Test<Logger> test = { Logger logger -> logger.isDebugEnabled() } as Test<Logger>
        //Test<Logger> test=new MyTest()
        new Bench<Logger>(
                test: test,
                vusers: 50,
                durationMs: SECONDS.toMillis(10),
                sampleSec: 1,
                iteration: 1,
                tempo: pacing.curry(5),
                objectUnderTest: Logger.getLogger("test")
                //tempo: { Closure closure -> pause(0, closure) }
        ).start()
    }

    public static void main(String[] args) {
        testLog4j()
        //testRound()
    }
}


@CompileStatic
class MyTest implements Test<Logger> {

    @Override
    void call(Logger logger) {
        logger.isDebugEnabled();

    }
}
