package com.hur.gmall.search;

import org.junit.Test;

public class test {
    @Test
    public void test(){
        String a = "123";
        String c = "123";
        String b = new String("123");
        String d1 = a+c;
        String d2 = a+c;
        System.out.println(d1==d2);
        System.out.println(a==c);
        System.out.println(a.equals(b));
    }

}
