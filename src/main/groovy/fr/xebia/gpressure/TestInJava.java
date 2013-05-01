package fr.xebia.gpressure;

public class TestInJava {
    public static void main(String[] args) {
        long total=0;
        long dump=0;
        long iter = 10000000;
        for(long l=1;l< iter;l++){
            long start=System.nanoTime();
            long result=System.nanoTime();
            total+=System.nanoTime()-start;
            dump=dump+result;
        }
        System.out.println("time="+(total/iter));
        System.out.println(dump);

    }


}
