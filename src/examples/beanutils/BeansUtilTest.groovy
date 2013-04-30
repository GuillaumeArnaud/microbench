import fr.xebia.microbench.Bench
import fr.xebia.microbench.Test
import fr.xebia.microbench.Validation
import groovy.transform.CompileStatic
@Grapes(
@Grab(group = 'commons-beanutils', module = 'commons-beanutils', version = '1.8.3')
)
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.PropertyUtils

new Bench().with {
    vusers = 1
    durationSec = 10000
    warmup(1000)
    tests(
            new MyTest(),
            { obj, data -> BeanUtils.copyProperties(data[0], data[1]) } as Test,
            { obj, data -> PropertyUtils.copyProperties(data[0], data[1]) } as Test
    )
    data(
            [new MyObject(), new MyObject(prop1: 10, prop2: "prop10", nested: new MyObject.MyNestedObject(prop3: "prop31"), map: ["key": 1])],
            [new MyObject(), new MyObject(prop1: 20, prop2: "prop20", nested: new MyObject.MyNestedObject(prop3: "prop32"), map: ["key": 2])],
            [new MyObject(), new MyObject(prop1: 30, prop2: "prop30", nested: new MyObject.MyNestedObject(prop3: "prop33"), map: ["key": 3])],
    )
    validate({ data, result -> data[0].prop1 != null } as Validation)
    defaultCollector()
    start()
}

@CompileStatic
class MyTest implements Test<BeanUtils> {

    @Override
    Object call(BeanUtils objectUnderTest, Collection<Object> data) {
        BeanUtils.copyProperties(data.iterator().next(), data.iterator().next())
        true
    }
}