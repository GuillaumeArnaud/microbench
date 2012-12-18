package fr.xebia.microbench.actors

import fr.xebia.microbench.internals.Sample
import fr.xebia.microbench.internals.Utils
import groovyx.gpars.actor.DefaultActor

import static fr.xebia.microbench.internals.Utils.unit

class Summary extends DefaultActor {
    protected float mean = 0, min = Float.MAX_VALUE, max = Float.MIN_VALUE
    protected long count = 0

    protected void act() {
        loop {
            react { Sample sample ->
                mean = (mean * count + sample.mean * sample.count) / (count + sample.count)
                min = Math.min(min, sample.min)
                max = Math.max(max, sample.max)
                count += sample.count
            }
        }
    }

    @Override
    public String toString() {
        return """
            Summary:
            mean = ${unit mean}
            count = ${count}
            min = ${unit min}
            max = ${unit max}
        """
    }
}

