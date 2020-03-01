package com.hur.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.PmsSkuAttrValue;
import com.hur.gmall.bean.PmsSkuImage;
import com.hur.gmall.bean.PmsSkuInfo;
import com.hur.gmall.bean.PmsSkuSaleAttrValue;
import com.hur.gmall.manage.mapper.PmsSkuAttrValueMapper;
import com.hur.gmall.manage.mapper.PmsSkuImageMapper;
import com.hur.gmall.manage.mapper.PmsSkuInfoMapper;
import com.hur.gmall.manage.mapper.PmsSkuSaleAttrValueMapper;
import com.hur.gmall.service.SkuService;
import com.hur.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class SkuServiceImpl implements SkuService {

    @Autowired
    PmsSkuInfoMapper pmsSkuInfoMapper;
    @Autowired
    PmsSkuImageMapper pmsSkuImageMapper;
    @Autowired
    PmsSkuAttrValueMapper pmsSkuAttrValueMapper;
    @Autowired
    PmsSkuSaleAttrValueMapper pmsSkuSaleAttrValueMapper;
    @Autowired
    RedisUtil redisUtil;


    @Override
    public String saveSkuInfo(PmsSkuInfo pmsSkuInfo) {

        System.out.println(pmsSkuInfo.getId());
        //保存sku属性
        int i = pmsSkuInfoMapper.insertSelective(pmsSkuInfo);
        System.out.println("i="+i);
        String skuId = pmsSkuInfo.getId();
        System.out.println("skuId="+skuId);

        List<PmsSkuAttrValue> skuAttrValueList = pmsSkuInfo.getSkuAttrValueList();
        List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

        //保存图片信息
        List<PmsSkuImage> skuImageList = pmsSkuInfo.getSkuImageList();
        for (PmsSkuImage pmsSkuImage:skuImageList){
            pmsSkuImage.setSkuId(skuId);
            pmsSkuImageMapper.insert(pmsSkuImage);
        }
        //保存平台属性关联 skuAttrValue
        for (PmsSkuAttrValue pmsSkuAttrValue:skuAttrValueList){
            pmsSkuAttrValue.setSkuId(skuId);
            pmsSkuAttrValueMapper.insertSelective(pmsSkuAttrValue);
        }
        //保存销售属性关联 SaleAttrValue
        for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue:skuSaleAttrValueList){
            pmsSkuSaleAttrValue.setSkuId(skuId);
            pmsSkuSaleAttrValueMapper.insertSelective(pmsSkuSaleAttrValue);
        }

        return null;
    }


    @Override
    public PmsSkuInfo getSkuByIdFromDb(String skuId) {

        //sku商品对象
        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(skuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);

        //sku图片集合
        PmsSkuImage pmsSkuImage = new PmsSkuImage();
        pmsSkuImage.setSkuId(skuId);
        List<PmsSkuImage> imageList = pmsSkuImageMapper.select(pmsSkuImage);

        skuInfo.setSkuImageList(imageList);

        return skuInfo;
    }

    @Override
    public List<PmsSkuInfo> getAllSkuInfo() {
        List<PmsSkuInfo> skuInfoList = pmsSkuInfoMapper.selectAll();

        for (PmsSkuInfo pmsSkuInfo:skuInfoList){
            String skuId = pmsSkuInfo.getId();

            PmsSkuAttrValue pmsSkuAttrValue = new PmsSkuAttrValue();
            pmsSkuAttrValue.setSkuId(skuId);
            List<PmsSkuAttrValue> pmsSkuAttrValueList = pmsSkuAttrValueMapper.select(pmsSkuAttrValue);

            pmsSkuInfo.setSkuAttrValueList(pmsSkuAttrValueList);
        }

        return skuInfoList;
    }

    @Override
    public boolean checkPrices(String productSkuId, BigDecimal price) {
        boolean check = false;

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();
        pmsSkuInfo.setId(productSkuId);
        PmsSkuInfo skuInfo = pmsSkuInfoMapper.selectOne(pmsSkuInfo);
        BigDecimal price1 = skuInfo.getPrice();
        if (skuInfo.getPrice().compareTo(price)==0){
            check = true;
        }

        return check;
    }

    @Override
    public PmsSkuInfo getSkuById(String skuId) {
        System.out.println("线程："+Thread.currentThread().getName()+"开始");

        PmsSkuInfo pmsSkuInfo = new PmsSkuInfo();

        //连接缓存
        Jedis jedis = redisUtil.getJedis();

        //查询缓存
        String skuKey = "sku:" + skuId + ":info";
        String skuJsonStr = jedis.get(skuKey);

        if (StringUtils.isNotBlank(skuJsonStr)) {
            //redis中查询到数据
            pmsSkuInfo = JSON.parseObject(skuJsonStr, PmsSkuInfo.class);

            System.out.println("线程："+Thread.currentThread().getName()+"从redis中得到数据");
        }else {
            //如果缓存中没有 查询Mysql

            //设置分布式锁
            String token = UUID.randomUUID().toString();
            String OK = jedis.set("sku:" + skuId + ":lock", token, "nx", "px", 10*1000);
            System.out.println("线程："+Thread.currentThread().getName()+"设置分布式锁");
            if (StringUtils.isNotBlank(OK)&&OK.equals("OK")) {
                //设置成功可以在10s内访问数据库
                pmsSkuInfo = getSkuByIdFromDb(skuId);
                System.out.println("线程："+Thread.currentThread().getName()+"从Mysql中得到数据");

//                try {
//                    Thread.sleep(8000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }

                if (pmsSkuInfo!=null){
                    //Mysql查询结果存入Redis
                    jedis.set(skuKey,JSON.toJSONString(pmsSkuInfo));
                    System.out.println("线程："+Thread.currentThread().getName()+"数据存入redis");
                }else {
                    //Mysql中没有数据
                    //将结果设置为空或null存入redis 防止缓存穿透
                    jedis.setex(skuKey,60*3,"");
                }
                //在访问mysql后 释放mysql分布式锁
                System.out.println("线程："+Thread.currentThread().getName()+"释放分布式锁");
                String lockToken = jedis.get("sku:" + skuId + ":lock");

                if (StringUtils.isNotBlank(lockToken)&&lockToken.equals(token)){
                    //jedis.eval("lua) 可用lua脚本 在查询到key的同时删除该key 防止高并发下的意外发生
                    //确认删除的是自己的锁
                    jedis.del("sku:"+skuId+":lock");
                }
            }else {
                //设置失败
                //自旋（该线程在睡眠几秒后，重新访问本方法）
                System.out.println("设置分布式锁失败");
                System.out.println("线程："+Thread.currentThread().getName()+"睡眠三秒后自旋");
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return getSkuById(skuId);
            }

        }

        jedis.close();

        return pmsSkuInfo;
    }

    @Override
    public List<PmsSkuInfo> getSkuSaleAttrValueBySpu(String productId) {
        List<PmsSkuInfo> skuInfoList = pmsSkuInfoMapper.selectSkuSaleAttrValueBySpu(productId);
        return skuInfoList;
    }
}
