package fr.xebia.microbench.actors

import groovyx.gpars.actor.Actor
import groovyx.gpars.actor.DefaultActor

import static java.lang.System.currentTimeMillis

class Feeder extends DefaultActor {

    private long startMs
    private Actor[] users = null
    private durationMs = 10 * 1000
    private Data data

    protected void act() {
        startMs = currentTimeMillis()
        loop {
            react { int id ->
                if (currentTimeMillis() - startMs > durationMs) {
                    // end of the benchmark.
                    // terminate all user actors
                    users.each { Actor user -> user.terminate();}
                    users*.join()
                    println "[${new Date(currentTimeMillis())}] terminate";
                    terminate();
                } else users[id].send data.next()
            }
        }
    }

    @Override
    protected void handleException(Throwable exception) {
        exception.printStackTrace()
    }

}
