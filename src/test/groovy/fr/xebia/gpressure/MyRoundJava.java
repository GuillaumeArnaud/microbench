package fr.xebia.gpressure;

import java.util.Collection;

public class MyRoundJava implements Test<Math> {
    @Override
    public Object call(Math objectUnderTest, Collection<Object> data) {
        return Math.round((Float) data.iterator().next());
    }

}
