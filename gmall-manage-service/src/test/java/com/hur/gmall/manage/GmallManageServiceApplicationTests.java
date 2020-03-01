package com.hur.gmall.manage;
//import com.hur.gmall.util.RedisUtil;
//import org.junit.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import redis.clients.jedis.Jedis;
//
//@SpringBootTest
//class GmallManageServiceApplicationTests {
//
//
//    @Autowired
//    RedisUtil redisUtil;
//
//    @Test
//    public void contextLoad(){
//        Jedis jedis = redisUtil.getJedis();
//
//        System.out.println(jedis);
//    }
//
//}

import com.hur.gmall.util.RedisUtil;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import redis.clients.jedis.Jedis;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageServiceApplicationTests {

    @Autowired
    RedisUtil redisUtil;

    @Test
    public void contextLoad(){
        Jedis jedis = redisUtil.getJedis();

        System.out.println(jedis);
    }
}

