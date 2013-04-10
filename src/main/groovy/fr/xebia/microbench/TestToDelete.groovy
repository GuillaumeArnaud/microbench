package fr.xebia.microbench

/**
 * Created with IntelliJ IDEA.
 * User: guillaume
 * Date: 1/22/13
 * Time: 12:01 AM
 * To change this template use File | Settings | File Templates.
 */
class TestToDelete {
    public static void main(String[] args){
        new Bench<Math>().with {
            tests({ objUnderTest, Collection<Object> data -> Math.round((Float) data[0]) } as Test)
            //tests(new MyRoundJava(), new MyRound(),new MyRoundJava(),new MyRound())
            data([0.1f], [0.5f], [0.6f], [0.13431515313413145f])
            defaultCollector()
            durationMs = 10000
            pause(2)
            vusers = 10
            threads = 8
            validate({ data, result -> if (data[0] == 0.1f) return result == 0 else true } as Validation)
            warmup(10000)
            start()
        }
    }
}
