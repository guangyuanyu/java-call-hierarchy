package org.joker.java.call.hierarchy.test;

public class A {

    private void methodA1() {
        System.out.println("methodA1");
    }

    public void methodA2() {
        methodA1();
    }

    public void methodA3(B b1, B b2) {
        methodA2();
    }

}
