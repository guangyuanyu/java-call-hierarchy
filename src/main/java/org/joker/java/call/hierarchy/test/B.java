package org.joker.java.call.hierarchy.test;

public class B {

    private A a = new A();

    public void methodB1() {
        a.methodA2();
    }

}
