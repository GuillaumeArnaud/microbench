import groovy.transform.CompileStatic
import groovy.transform.TypeChecked

@CompileStatic
interface Test<T> {
    @TypeChecked
    public void call(T objectUnderTest, Collection<Object> data);
}
