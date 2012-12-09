
@Grapes([
@Grab(group = 'log4j', module = 'log4j', version = '1.2.17'),
@Grab(group = 'ch.qos.logback', module = 'logback-classic', version = '1.0.7'),
@Grab(group = 'ch.qos.logback', module = 'logback-core', version = '1.0.7')]
)

import ch.qos.logback.classic.Logger
import ch.qos.logback.classic.LoggerContext

//import org.apache.log4j.Logger

import static Bench.getPacing
import static Bench.getPause
import static java.util.concurrent.TimeUnit.MILLISECONDS
import static java.util.concurrent.TimeUnit.SECONDS


class Tests {

    private static void testRound() {
        Random rnd = new Random()

        def test = {-> Math.round(rnd.nextFloat()); }

        new Bench(
                test: test,
                nbUser: 100,
                durationMs: SECONDS.toMillis(100),
                tempo: { Closure closure -> pacing(10, closure) }
        ).start()
    }

    private static void testLog4j() {

        def logger = org.apache.log4j.Logger.getLogger(this.class)
        def test = {-> logger.isDebugEnabled() }

        new Bench(
                test: test,
                nbUser: 10,
                durationMs: SECONDS.toMillis(100),
                sampleSec: 1,
                iteration: 1,
                tempo: { Closure closure -> pacing(5, closure) }
                //tempo: { Closure closure -> pause(0, closure) }
        ).start()
    }

    private static void testLogback() {

        def logger = new Logger("logger", null, new LoggerContext())
        def test = {-> logger.isDebugEnabled() }

        new Bench(
                test: test,
                nbUser: 10,
                durationMs: SECONDS.toMillis(100),
                sampleSec: 1,
                iteration: 1,
                tempo: { Closure closure -> pacing(5, closure) }
                //tempo: { Closure closure -> pause(0, closure) }
        ).start()
    }

    public static void main(String[] args) {
        testLog4j()
    }
}
