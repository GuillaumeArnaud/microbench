package fr.xebia.microbench.actors

import fr.xebia.microbench.Level
import groovyx.gpars.actor.DefaultActor


class Logger extends DefaultActor {

    public Level level
    public static Level currentLevel

    @Override
    protected void act() {
        loop {
            react { msg -> if (currentLevel <= level) println msg }
        }
    }
}
