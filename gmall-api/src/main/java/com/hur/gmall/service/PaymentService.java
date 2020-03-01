package com.hur.gmall.service;

import com.hur.gmall.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {
    void savePaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(PaymentInfo paymentInfo);

    void sendDelayPaymentResultCheckQueue(String outTradeNo,int count);

    Map<String, Object> checkAlipayPayment(String outTradeNo);
}
