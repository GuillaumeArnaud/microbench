import org.apache.log4j.Logger;

public class Test {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("logger");
        for(int i= 1 ; i<1000; i++){
            long start=System.nanoTime();
            logger.isDebugEnabled();
            long elapse=System.nanoTime()-start;
            System.out.println(elapse);
        }
    }
}
