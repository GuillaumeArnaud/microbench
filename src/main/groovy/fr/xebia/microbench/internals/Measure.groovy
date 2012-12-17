package fr.xebia.microbench.internals

class Measure {
    protected long start
    protected long elapse

    Measure(long start, long elapse) {
        this.start = start
        this.elapse = elapse
    }
}
