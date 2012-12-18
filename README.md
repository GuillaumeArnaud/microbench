microbench
==========

groovy application for easy microbenchmark:

    new Bench(tests: [{new Random().nextInt(1000)} as Test]).start

a more complex example:

    new Bench<Math>(
            tests: [{ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test],
            data: new Data([[0.1f, 0.5f, 0.6f]]),
            collector: defaultCollector,
            durationMs: 10000,
            tempo: Tempo.pacing.curry(1),
            vusers: 100,
            warmupMs: 10000
    ).start()