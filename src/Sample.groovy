protected class Sample {
    protected float mean, min, max
    protected long count

    protected Sample(Collection<Measure> measures, long iteration) {
        def elapsed = measures.collect { measure -> measure.elapse }
        count = iteration * measures.size()
        mean = elapsed.sum() / count
        min = elapsed.min() / iteration
        max = elapsed.max() / iteration
    }

    private static String unit(float timeInNs) {
        if (timeInNs < 1000000f) {
            return "$timeInNs ns"
        } else return "${timeInNs / 1000000} ms"
    }

    public String toString() { "mean=${unit mean}, min=${unit min}, max=${unit max}, count=$count" }
}
