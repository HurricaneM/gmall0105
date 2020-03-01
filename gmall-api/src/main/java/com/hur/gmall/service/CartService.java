package com.hur.gmall.service;

import com.hur.gmall.bean.OmsCartItem;

import java.util.List;

public interface CartService {
    OmsCartItem getCartByUserAndSkuId(String memberId, String skuId);

    void addCart(OmsCartItem omsCartItem);

    void updateCart(OmsCartItem cartItemByUser);

    void flushCartCache(String memberId);

    List<OmsCartItem> cartList(String userId);

    void checkCart(OmsCartItem omsCartItem);
}
