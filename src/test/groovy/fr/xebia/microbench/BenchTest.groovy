package fr.xebia.microbench

// Test about Math.round
new Bench<Math>(
    tests:[{ objUnderTest, Collection<Object> data -> Math.round((Float)data[0]) } as Test],
    data: new Data([[0.1f,0.5f,0.6f]]),
    collector: Bench.detailledCollector,
    durationMs: 100000
).start()