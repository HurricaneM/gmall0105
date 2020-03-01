package com.hur.gmall.payment.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.AlipayResponse;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.hur.gmall.bean.PaymentInfo;
import com.hur.gmall.mq.ActiveMQUtil;
import com.hur.gmall.payment.mapper.PaymentInfoMapper;
import com.hur.gmall.service.PaymentService;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    PaymentInfoMapper paymentInfoMapper;
    @Autowired
    ActiveMQUtil activeMQUtil;
    @Autowired
    AlipayClient alipayClient;

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(PaymentInfo paymentInfo) {

        PaymentInfo paymentInfoParam = new PaymentInfo();
        paymentInfoParam.setOrderSn(paymentInfo.getOrderSn());
        PaymentInfo paymentInfoCheck = paymentInfoMapper.selectOne(paymentInfoParam);

        if (StringUtils.isNotBlank(paymentInfoCheck.getPaymentStatus())&&
        paymentInfoCheck.getPaymentStatus().equals("已支付")){
            return;
        }else {
            String orderSn = paymentInfo.getOrderSn();
            Example example = new Example(PaymentInfo.class);
            example.createCriteria().andEqualTo("orderSn",orderSn);

            Connection connection = null;
            Session session = null;

            try {
                paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
                connection = activeMQUtil.getConnectionFactory().createConnection();

                session = connection.createSession(true,Session.SESSION_TRANSACTED);
                //支付成功后引发的系统服务：订单服务更新 》库存服务 》物流服务
                //调用mq发送支付成功信息
                Queue payment_success_queue = session.createQueue("PAYMENT_SUCCESS_QUEUE");
                MessageProducer producer = session.createProducer(payment_success_queue);

                //ActiveMQTextMessage activeMQTextMessage = new ActiveMQTextMessage();字符串文本
                ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
                activeMQMapMessage.setString("out_trade_no",paymentInfo.getOrderSn());
                producer.send(activeMQMapMessage);
                session.commit();
            } catch (JMSException e) {
                //消息回滚
                try {
                    session.rollback();
                } catch (JMSException ex) {
                    ex.printStackTrace();
                }
            }finally {
                try {
                    connection.close();
                } catch (JMSException e) {
                    e.printStackTrace();
                }
            }

        }


    }

    @Override
    public void sendDelayPaymentResultCheckQueue(String outTradeNo,int count) {

        Connection connection = null;
        Session session = null;
        try {
            connection = activeMQUtil.getConnectionFactory().createConnection();
            session = connection.createSession(true,Session.SESSION_TRANSACTED);

            //支付成功后引发的系统服务：订单服务更新 》库存服务 》物流服务
            //调用mq发送支付成功信息
            Queue payment_check_queue = session.createQueue("PAYMENT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(payment_check_queue);

            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("out_trade_no",outTradeNo);
            activeMQMapMessage.setInt("count",count);

            //加入延迟时间
            activeMQMapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY,1000*10);

            producer.send(activeMQMapMessage);
            session.commit();
        } catch (JMSException e) {
            //消息回滚
            try {
                session.rollback();
            } catch (JMSException ex) {
                ex.printStackTrace();
            }
        }finally {
            try {
                connection.close();
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public Map<String, Object> checkAlipayPayment(String outTradeNo) {
        Map<String, Object> resultMap = new HashMap<>();

        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        Map<String,Object> requestMap = new HashMap<>();
        requestMap.put("out_trade_no",outTradeNo);
        request.setBizContent(JSON.toJSONString(requestMap));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }

        if (response.isSuccess()){
            System.out.println("交易创建 调用成功");
            resultMap.put("out_trade_no",response.getOutTradeNo());
            resultMap.put("trade_no",response.getTradeNo());
            resultMap.put("trade_status",response.getTradeStatus());
            resultMap.put("callBack",response.getMsg());
        }else {
            System.out.println("有可能交易未创建调用失败");
        }

        return resultMap;
    }
}
