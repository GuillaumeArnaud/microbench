package fr.xebia.microbench

import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
interface Test<T> {
    @TypeChecked
    public Object call(T objectUnderTest, Collection<Object> data);
}
