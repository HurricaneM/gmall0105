package com.hur.gmall.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtTest {
    public static void main(String[] args) {
        Map<String,Object> map = new HashMap<>();

        map.put("memberID","1");
        map.put("nickName","zhangsan");
        String ip = "127.0.0.1";
        String time = new SimpleDateFormat("yyyyMMdd HHmm").format(new Date());
        String encode = JwtUtil.encode("2020gall0105",map,ip+time);

        System.out.println(encode );
    }
}
