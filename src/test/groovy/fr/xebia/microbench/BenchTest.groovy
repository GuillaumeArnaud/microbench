package fr.xebia.microbench

import fr.xebia.microbench.actors.Data

import static fr.xebia.microbench.internals.Collectors.getDefaultCollector

// Test about Math.round
new Bench<Math>(
        tests: [{ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test],
        data: new Data([[0.1f, 0.5f, 0.6f]]),
        collector: defaultCollector,
        durationMs: 100000
).start()