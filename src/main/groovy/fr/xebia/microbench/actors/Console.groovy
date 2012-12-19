package fr.xebia.microbench.actors

import fr.xebia.microbench.internals.Level
import groovyx.gpars.actor.DefaultActor


class Console extends DefaultActor {

    public Level level
    public static Level currentLevel

    @Override
    protected void act() {
        loop {
            react { msg -> if (currentLevel <= level) println msg }
        }
    }
}
