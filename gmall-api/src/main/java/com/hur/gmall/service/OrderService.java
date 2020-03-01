package com.hur.gmall.service;

import com.hur.gmall.bean.OmsOrder;

public interface OrderService {
    String generateTradeCode(String memberId);

    String checkTradeCode(String memberId,String tradeCode);

    void saveOrder(OmsOrder omsOrder);

    OmsOrder getOrderByOutTradeNo(String outTradeNo);

    void updateOrder(OmsOrder omsOrder);
}
