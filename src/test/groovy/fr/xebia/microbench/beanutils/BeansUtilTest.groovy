package fr.xebia.microbench.beanutils

import fr.xebia.microbench.Bench
import fr.xebia.microbench.Test
import groovy.transform.CompileStatic
@Grapes(
@Grab(group='commons-beanutils', module='commons-beanutils', version='1.8.3')
)
import org.apache.commons.beanutils.BeanUtils
import org.apache.commons.beanutils.PropertyUtils


new Bench().with {
    vusers = 1
    durationMs = 10000
    warmup(1000)
    tests(
            {obj, data -> BeanUtils.copyProperties(data[0],data[1]); println "${data[0]}  -  ${data[1]}"} as Test ,
            {obj, data -> PropertyUtils.copyProperties(data[0],data[1]);println "${data[0]}  -  ${data[1]}"} as Test
    )
    data(
            [new MyObject(),new MyObject(prop1: 10,prop2: "prop4")],
            [new MyObject(),new MyObject(prop1: 11,prop2: "prop5")],
            [new MyObject(),new MyObject(prop1: 12,prop2: "prop4")]
    )
    defaultCollector()
    start()
}

@CompileStatic
class MyDestObject {
    private String prop1
    private String prop2

    private Object nested

    Object getNested() {
        return nested
    }

    void setNested(Object nested) {
        this.nested = nested
    }

    void setProp1(String prop1) {
        this.prop1 = prop1
    }


    void setProp2(String prop2) {
        this.prop2 = prop2
    }

    String getProp1() {
        return prop1
    }

    String getProp2() {
        return prop2
    }

    @Override
    public String toString() {
        return "MyDestObject{" +
                "prop1='" + prop1 + '\'' +
                ", prop2='" + prop2 + '\'' +
                ", nested=" + nested +
                '}';
    }


}