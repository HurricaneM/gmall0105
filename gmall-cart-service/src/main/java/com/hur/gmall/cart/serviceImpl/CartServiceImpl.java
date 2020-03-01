package com.hur.gmall.cart.serviceImpl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.OmsCartItem;
import com.hur.gmall.cart.mapper.OmsCartItemMapper;
import com.hur.gmall.service.CartService;
import com.hur.gmall.util.RedisUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    OmsCartItemMapper omsCartItemMapper;
    @Autowired
    RedisUtil redisUtil;

    @Override
    public OmsCartItem getCartByUserAndSkuId(String memberId, String skuId) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setMemberId(memberId);
        cartItem.setProductSkuId(skuId);
        OmsCartItem cartItems = omsCartItemMapper.selectOne(cartItem);
        return cartItems;
    }

    @Override
    public void addCart(OmsCartItem omsCartItem) {
        if (StringUtils.isNotBlank(omsCartItem.getMemberId())){
            omsCartItemMapper.insertSelective(omsCartItem);
        }
    }

    @Override
    public void updateCart(OmsCartItem cartItemByUser) {
        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("id",cartItemByUser.getId());

        omsCartItemMapper.updateByExample(cartItemByUser,example);

    }

    @Override
    public void flushCartCache(String memberId) {
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setMemberId(memberId);
        List<OmsCartItem> omsCartItemList = omsCartItemMapper.select(cartItem);
        Map<String,String> omsCartItemMap = new HashMap<>();
        for (OmsCartItem omsCartItem : omsCartItemList) {
            omsCartItemMap.put(omsCartItem.getProductSkuId(),JSON.toJSONString(omsCartItem));
        }


        //同步到redis中
        Jedis jedis = redisUtil.getJedis();

        jedis.del("user:"+memberId+":cart");
        jedis.hmset("user:"+memberId+":cart",omsCartItemMap);

        jedis.close();
    }

    @Override
    public List<OmsCartItem> cartList(String userId) {
        Jedis jedis = null;
        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        try {

            jedis = redisUtil.getJedis();
            List<String> hvals = jedis.hvals("user:" + userId + ":cart");

            for (String hval : hvals) {
                OmsCartItem cartItem = JSON.parseObject(hval, OmsCartItem.class);
                omsCartItemList.add(cartItem);
            }
        }catch (Exception e){
            //异常处理 记录系统日志
            e.printStackTrace();
//            String message = e.getMessage();
//            LogService.addErrLog();
            return null;
        }finally {
            jedis.close();
        }

        return omsCartItemList;
    }

    @Override
    public void checkCart(OmsCartItem omsCartItem) {

        Example example = new Example(OmsCartItem.class);
        example.createCriteria().andEqualTo("memberId",omsCartItem.getMemberId())
                .andEqualTo("productSkuId",omsCartItem.getProductSkuId());

        omsCartItemMapper.updateByExampleSelective(omsCartItem,example);

        //缓存同步
        flushCartCache(omsCartItem.getMemberId());

    }
}
