import fr.xebia.gpressure.Bench
import fr.xebia.gpressure.Test
import fr.xebia.gpressure.Validation


new Bench().with {
    flow()
    detailedCollector()
    data(["abcdefghij".toCharArray()])
    duration(30)
    //vusers(1000)
    threads(30)
    //iteration(1000)
    tests({ o, d ->
        def builder=new StringBuilder()
        for (l in d) {
            builder.append(l);
        }
        builder
    } as Test,
            { o,d ->
                def buffer = new StringBuffer()
                for(l in d) {
                    buffer.append(d)
                }
            } as Test
    )

    validate({o,r -> r.toString()=="abcdefghij"} as Validation)
    start()
}

