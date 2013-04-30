microbench
==========

groovy application for easy microbenchmark:

    new Bench({tests: [{o,d -> new Random().nextInt(1000)} as Test]}).start()

a more complex example:

    new Bench<Math>().with {
            duration(30) // duration in seconds
            tests({ objUnderTest, data -> Math.round((Float) data[0]) } as Test) // the tested method
            data([0.1f], [0.5f], [0.6f]) // injected data to the test
            defaultCollector() // collect some information about the jvm (compilation, memory, loaded classes...)
            pacing(5) // the pacing in seconds
            vusers(10) // number of parallel vusers
            warmup(5) // warmup in secondes before the test
            start() // start the test
    )

# goal

*microbench* has the ambition to:

+   avoid some common pitfalls during the benchmark of java method (inlining, warmup, data input ...)
+   facilitate the data input injection
+   control the throughput of the test through the number of users, iterations, threads ...
+   validate the results of each test
+   report the state of JVM during the test (memory, threads, gc ...)
+   report the test result with maximum of information (mean, min, max, standard deviation, context ...)

# how that's work

The inside process uses the actor pattern thank to the gpars library. The several steps are :

1.  create users
2.  the feeder actor feeds the user actor with the input data
3.  user actor calls the tested method and sends the result and the elapsed time to the sampler actor
4.  user actor sends the result optionally to the validator actor, failed validations will be logged
5.  the sampler actor aggregates results depending on the sample interval and sends each samples to the summarizer actor
6.  the summarizer actor computes global results (mean, min, max ...)
7.  all logs are sent to the logger actor

# how to build

You can build the project by calling gradle:

    ?> graddle package

# how to use it

For calling the bench, add the microbench jar in the groovy classpath in addition to your script. For instance:

    ?> groovy -cp microbench.jar my_script.groovy

See examples directory.


# Methods

Basically the best way to define the benchmark is to follow this pattern:

    new Bench() {
        tests({objectUnderTest, data -> my_test} as Test)
        duration(60)
        start()
    }

You can define Test classes inside other files of course, that can be useful for adding @CompileStatic annotations or
call java classes.

## test

The tests are defined by the method:

    tests(Test<T>... tests): each test will be called sequentially. The warmup is call at the begin of each test and
    a summary is displayed at the end of each test.

A quick way to define the tests methods:

    tests(
        {objectUnderTest, data -> my_first_test_here} as Test,
        {objectUnderTest, data -> my_second_test_here} as Test
    )

## technical parameters

A lot of parameters can be configured:

    duration(long durationSec = 10): Duration of the test in second.

    vusers(int vusers = 1): Number of parallel virtual users.

    threads(int threads = 3): Number of thread available in the pool.

    sampleInterval(int sampleIntervalSec = 1): Set the interval in second between two samples.

    collectorInterval(int collectorIntervalSec = 5) : Set the interval in second between two collects
    iteration(long iteration = 1): Number of iteration at each run. By default the value is 1, if this
    number increases, a risk of inlining exists.


## warmup

    warmup(int warmupSec): the tested method will be called during the given duration. The elapsed time won't take into
    account these calls.

You can override the method:

    public Closure<Void> warmup = { long warmupMs, Test<T> test, T objectUnderTest, Data data, Closure<Map<String, Object>> collector -> return }

By default the value of the warmup is 1 second.

## tempo

You can control the throughput of the test thanks to two methods:

    pause(long pauseMs): after the call of the tested method, each user does a pause of of the given duration

Or:

    pacing(long pacingMs): after the call of the tested method, each user does a pause in order to reach the total pacing
    duration. For instance if the pacing is 5000ms, the total time of the user before another test call will be 5000ms.
    If we have 10 vusers, we know that the throughput will be 10 calls/5000ms, so 2 calls/s. This method is helpful for
    controlling the throughput.

The two methods are exclusives, only the last method call wins.

You can override the method:

    public Closure<Void> tempo = { long elapseMs -> return }

## data

You can inject data into the test:

    data(Collection<Object>... data): the data will be iterated at each new user call. When the collection ends, the
    iterator is reinitialized.

## validate

You can check that the test return a correct result:

    validate(Validation validation): Validation is an interface which should be implemented by you.

 A quick definition could be like this:

    validate({data, result -> make_your_validation_here} as Validation)

## collectors

You can collect information about JVM thanks to the collector process. To define it you can call one of these methods:

    defaultCollector(): collect information about memory, compilation and threads
    detailedCollector(): collect information about garbage collection additionnally to the default collection

You can override the method:

    public Closure<Map<String, Object>> collector = { ... }

## logs

You can choose the log level by calling one of these methods:

    error(): log nothing except the errors
    flow(): log info level plus all information collected during the test like the sample values and collector
    information (see collector methods)
    info(): log only useful information like the warmup time, test parameters, global result (mean, max, min ...)
    debug(): log a maximum of information in order to fixing a bug

# Roadmap

* more unit tests (shame on me, it's not Test Driven designed)
* give configuration abilities for reporting (html, csv ...). Currently there is only the standard output.
* improve the collectors with more information
* improve the naming of tests
* implements warmup stopping in correlation with compilation or loaded classes