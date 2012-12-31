package fr.xebia.microbench.actors

import fr.xebia.microbench.Validation
import groovyx.gpars.actor.DefaultActor

import java.util.concurrent.atomic.AtomicLong

import static fr.xebia.microbench.Bench.error
import static fr.xebia.microbench.Bench.flow

class Validator extends DefaultActor {

    private Validation validation
    public AtomicLong errors

    protected void act() {
        loop {
            react { Map<String, Object> dataAndResult ->
                if (dataAndResult.containsKey("data") && dataAndResult.containsKey("result")) {
                    if (!validation.valid(dataAndResult.data as Collection<Object>, dataAndResult.result)) {
                        flow "the result is not valid with the given data: $dataAndResult"
                    }

                } else {
                    error "cant't get result from $dataAndResult"
                    errors.incrementAndGet()
                }
            }
        }
    }

}

