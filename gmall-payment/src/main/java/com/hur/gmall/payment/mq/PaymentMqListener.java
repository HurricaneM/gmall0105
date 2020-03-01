package com.hur.gmall.payment.mq;

import com.hur.gmall.bean.PaymentInfo;
import com.hur.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import java.util.Date;
import java.util.Map;

@Component
public class PaymentMqListener {

    @Autowired
    PaymentService paymentService;

    @JmsListener(destination = "PAYMENT_CHECK_QUEUE",containerFactory = "jmsQueueListener")
    public void consumerPaymentCheckedResult(MapMessage mapMessage) throws JMSException {
        String outTradeNo = mapMessage.getString("out_trade_no");
        int count = mapMessage.getInt("count");

        //调用paymentService的支付宝检查接口
        System.out.println("进行延迟检查");
        Map<String,Object> resultMap = paymentService.checkAlipayPayment(outTradeNo);
//        Map<String,Object> resultMap = null;


        if (resultMap!=null&&!resultMap.isEmpty()){
            String tradeStatus = (String) resultMap.get("trade_status");

            //根据查询结果 判断是进行下一次延迟查询还是支付成功更新数据和后续任务
            if (StringUtils.isNotBlank(tradeStatus)&&tradeStatus.equals("TRADE_SUCCESS")){
                //支付成功 更新支付发送支付队列
                System.out.println("支付成功");
                PaymentInfo paymentInfo = new PaymentInfo();
                paymentInfo.setPaymentStatus("已付款");
                paymentInfo.setAlipayTradeNo(mapMessage.getString("trade_no"));//支付宝的交易凭证号
                paymentInfo.setOrderSn(outTradeNo);//商户订单号
                paymentInfo.setCallbackContent(mapMessage.getString("callBack"));
                paymentInfo.setCallbackTime(new Date());
                //更新用户支付状态
                //进行支付成功的幂等性检查
                paymentService.updatePaymentInfo(paymentInfo);
                return;


            }else {
                if (count>0){
                    //继续发送延迟检查任务
                    System.out.println("支付失败 剩余检查次数"+count+"继续发送延迟检查任务");
                    count--;
                    paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
                }else{
                    System.out.println("剩余次数用尽结束检查");
                }
            }
        }else {
            if (count>0){
                //继续发送延迟检查任务
                System.out.println("支付失败 剩余检查次数"+count+"继续发送延迟检查任务");
                count--;
                paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,count);
            }else{
                System.out.println("剩余次数用尽结束检查");
            }
        }
    }
}
