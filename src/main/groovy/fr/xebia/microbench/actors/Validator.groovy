package fr.xebia.microbench.actors

import fr.xebia.microbench.Bench
import fr.xebia.microbench.Validation
import groovyx.gpars.actor.DefaultActor

import java.util.concurrent.atomic.AtomicLong

class Validator extends DefaultActor {

    private Validation validation
    private AtomicLong errors

    protected void act() {
        loop {
            react { Map<String, Object> dataAndResult ->
                if (dataAndResult.containsKey("data") && dataAndResult.containsKey("result")) {
                    validation.valid(dataAndResult.data as Collection<Object>, dataAndResult.result)
                } else {
                    Bench.error "cant't get result from $dataAndResult"
                    errors.incrementAndGet()
                }
            }
        }
    }

}

