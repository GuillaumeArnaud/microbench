package fr.xebia.microbench.internals

import static java.util.concurrent.TimeUnit.NANOSECONDS

class Tempo {

    static Closure<Void> pause = { long pauseInMs, elapseInMs ->
        sleep(pauseInMs)
        return
    }

    static Closure<Void> pacing = { long pacingInMs, long elapseNs ->
        sleep(Math.max(1l, pacingInMs - NANOSECONDS.toMillis(elapseNs)))
        return
    }
}
