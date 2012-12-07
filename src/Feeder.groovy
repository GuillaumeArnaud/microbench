import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

import static java.lang.System.currentTimeMillis

class Feeder extends DefaultActor {

    private long startMs
    private Actor[] users = null
    private durationMs = 10 * 1000


    protected void act() {
        startMs = currentTimeMillis()
        loop {
            react { int id ->
                if (currentTimeMillis() - startMs > durationMs) {
                    // end of the benchmark.
                    // terminate all user actors
                    users.each { Actor user -> user.send(-1) }
                    println "[${new Date(currentTimeMillis())}] terminate";
                    terminate();
                } else users[id].send id
            }
        }
    }

    @Override
    protected void handleException(Throwable exception) {
        exception.printStackTrace()
    }

}
