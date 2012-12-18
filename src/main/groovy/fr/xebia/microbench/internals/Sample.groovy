package fr.xebia.microbench.internals

import static fr.xebia.microbench.internals.Utils.unit

class Sample {
    public float mean, min, max
    public long count

    public Sample(Collection<Measure> measures, long iteration) {
        def elapsed = measures.collect { measure -> measure.elapse }
        count = iteration * measures.size()
        mean = elapsed.sum() / count
        min = elapsed.min() / iteration
        max = elapsed.max() / iteration
    }



    public String toString() { "mean=${unit mean}, min=${unit min}, max=${unit max}, count=$count" }
}
