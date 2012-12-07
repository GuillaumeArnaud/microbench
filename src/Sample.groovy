import static java.util.concurrent.TimeUnit.NANOSECONDS

protected class Sample {
    protected float mean, min, max, count

    protected Sample(Collection<Measure> measures) {
        def elapsed = measures.collect { Measure measure -> NANOSECONDS.toMillis(measure.elapse)  }
        count = measures.size()
        mean = elapsed.sum() / count
        min = elapsed.min()
        max = elapsed.max()
    }

    public String toString() { "mean=$mean ms, min=$min ms, max=$max ms, count=$count" }
}
