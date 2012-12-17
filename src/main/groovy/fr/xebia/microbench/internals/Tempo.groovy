package fr.xebia.microbench.internals

class Tempo {

    static Closure<Void> pause = { long pauseInMs, elapseInMs ->
        sleep(pauseInMs)
        return
    }

    static Closure<Void> pacing = { long pacingInMs, long elapseInMs ->
        sleep(Math.max(1l, pacingInMs - elapseInMs))
        return
    }
}
