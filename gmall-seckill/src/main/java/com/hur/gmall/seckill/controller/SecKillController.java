package com.hur.gmall.seckill.controller;

import com.hur.gmall.util.RedisUtil;
import jdk.nashorn.internal.ir.annotations.Reference;
import org.redisson.api.RSemaphore;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.List;

@Controller
public class SecKillController {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    RedissonClient redissonClient;

    @ResponseBody
    @RequestMapping("kill2")
    public String kill2(){
        Jedis jedis = redisUtil.getJedis();
        RSemaphore semaphore = redissonClient.getSemaphore("116");
        boolean b = semaphore.tryAcquire();
        int i = Integer.parseInt(jedis.get("116"));

        if (b){
            System.out.println("当前库存量："+i+"xxx抢购成功success");
        }else {
            System.out.println("当前库存量："+i+"xxx抢购失败");

        }

        return "ok";
    }

    @ResponseBody
    @RequestMapping("kill")
    public String kill(){
        String member = "xxx";
        Jedis jedis = redisUtil.getJedis();
        jedis.watch("116");
        int i = Integer.parseInt(jedis.get("116"));
        if (i>0){
            Transaction multi = jedis.multi();
            multi.incrBy("116",-1);
            List<Object> exec = multi.exec();
            if (exec!=null&&exec.size()>0){
                System.out.println("当前库存量："+i+"xxx抢购成功success");
            }else {
                System.out.println("当前库存量："+i+"xxx抢购失败");

            }

        }
        jedis.close();
        return "ok";
    }
}
