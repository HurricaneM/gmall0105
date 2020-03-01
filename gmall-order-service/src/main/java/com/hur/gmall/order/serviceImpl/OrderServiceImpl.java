package com.hur.gmall.order.serviceImpl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.OmsOrder;
import com.hur.gmall.bean.OmsOrderItem;
import com.hur.gmall.mq.ActiveMQUtil;
import com.hur.gmall.order.mapper.OmsOrderItemMapper;
import com.hur.gmall.order.mapper.OmsOrderMapper;
import com.hur.gmall.service.CartService;
import com.hur.gmall.service.OrderService;
import com.hur.gmall.util.RedisUtil;
import com.sun.org.apache.bcel.internal.generic.NEW;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.annotation.Order;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    RedisUtil redisUtil;
    @Autowired
    OmsOrderMapper omsOrderMapper;
    @Autowired
    OmsOrderItemMapper omsOrderItemMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Reference
    CartService cartService;

    @Override
    public String generateTradeCode(String memberId) {

        Jedis jedis = null;
        String key = "user:"+memberId+":tradeCode";
        String tradeCode = UUID.randomUUID().toString();

        try {
            jedis = redisUtil.getJedis();
            jedis.setex(key,60*60*15,tradeCode);
        }finally {
            jedis.close();
        }

        return tradeCode;
    }

    @Override
    public String checkTradeCode(String memberId,String tradeCode) {

        Jedis jedis = null;
        String key = "user:"+memberId+":tradeCode";
        String tradeCodeFromCache = "";

        try {
            jedis = redisUtil.getJedis();
            tradeCodeFromCache = jedis.get(key);
            //对比防重删令牌
            //使用lua脚本在发现key的同时就将他删除 防止并发订单攻击
            String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";

            Long eval = (Long) jedis.eval(script, Collections.singletonList(key),
                    Collections.singletonList(tradeCode));

            if (eval!=null&&eval!=0){
                return "success";
            }else {
                return "fail";
            }
        }finally {
            jedis.close();
        }


    }

    @Override
    public void saveOrder(OmsOrder omsOrder) {

        omsOrderMapper.insertSelective(omsOrder);
        String omsOrderId = omsOrder.getId();

        List<OmsOrderItem> omsOrderItemList = omsOrder.getOmsOrderItemList();
        for (OmsOrderItem omsOrderItem : omsOrderItemList) {

            omsOrderItem.setOrderId(omsOrderId);
            omsOrderItemMapper.insertSelective(omsOrderItem);

            //删除购物车数据


        }

    }

    @Override
    public OmsOrder getOrderByOutTradeNo(String outTradeNo) {

        OmsOrder omsOrder = new OmsOrder();
        omsOrder.setOrderSn(outTradeNo);
        OmsOrder omsOrder1 = omsOrderMapper.selectOne(omsOrder);
        return omsOrder1;
    }

    @Override
    public void updateOrder(OmsOrder omsOrder) {
        omsOrder.setStatus("1");

        Example example = new Example(OmsOrder.class);
        example.createCriteria().andEqualTo("orderSn",omsOrder.getOrderSn());

        OmsOrder updateOmsOrder = new OmsOrder();
        updateOmsOrder.setStatus("1");

        //发送队列
        Connection connection = null;
        Session session = null;


        try {
            omsOrderMapper.updateByExampleSelective(updateOmsOrder,example);


            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);

            Queue order_pay_queue = session.createQueue("ORDER_PAY_QUEUE");
            MessageProducer producer = session.createProducer(order_pay_queue);

            ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();
            OmsOrder omsSelect = new OmsOrder();
            omsSelect.setOrderSn(omsOrder.getOrderSn());
            OmsOrder order = omsOrderMapper.selectOne(omsSelect);

            OmsOrderItem omsOrderItem = new OmsOrderItem();
            omsOrderItem.setOrderSn(omsOrder.getOrderSn());
            List<OmsOrderItem> select = omsOrderItemMapper.select(omsOrderItem);
            order.setOmsOrderItemList(select);


            activeMQTextMessage.setText(JSON.toJSONString(order));

            producer.send(activeMQTextMessage);
            session.commit();
        } catch (JMSException e) {
            //消息回滚
            try {
                assert session != null;
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                assert connection != null;
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }

    }
}
