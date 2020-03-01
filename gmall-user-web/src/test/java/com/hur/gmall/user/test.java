package com.hur.gmall.user;

import java.util.HashMap;
import java.util.Map;

public class test {
    public static void main(String[] args) {
        System.out.println("123".hashCode());
        Map map = new HashMap();
        map.put(1,"123");
        System.out.println(map.get(1).hashCode());
        String s = "123";
        System.out.println(s.hashCode());
    }
}
