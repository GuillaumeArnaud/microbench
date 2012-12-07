import groovyx.gpars.actor.DefaultActor

/**
 * The User actor run a test iteration with tempo and measure (may be the user is a musician ?).
 */
class User extends DefaultActor {

    private Closure tempo, measure, test

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

}
