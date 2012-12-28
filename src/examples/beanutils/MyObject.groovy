import groovy.transform.CompileStatic
@CompileStatic
class MyObject {
    public Integer prop1;
    private String prop2;
    private Map<String, Integer> map;
    private MyNestedObject nested = new MyNestedObject();

    MyNestedObject getNested() {
        return nested;
    }

    void setNested(MyNestedObject nested) {
        this.nested = nested;
    }

    void setProp1(Integer prop1) {
        this.prop1 = prop1;
    }


    void setProp2(String prop2) {
        this.prop2 = prop2;
    }

    Integer getProp1() {
        return prop1;
    }

    String getProp2() {
        return prop2;
    }

    @Override
    public String toString() {
        return "MyObject{" +
                "prop1='" + prop1 + '\'' +
                ", prop2='" + prop2 + '\'' +
                ", nested=" + nested +
                '}';
    }

    static class MyNestedObject {
        private String prop3 = "prop3";

        String getProp3() {
            return prop3;
        }

        void setProp3(String prop3) {
            this.prop3 = prop3 ;
        }

        @Override
        public String toString() {
            return "MyNestedObject{" +
                    "prop3='" + prop3 + '\'' +
                    '}';
        }
    }
}
