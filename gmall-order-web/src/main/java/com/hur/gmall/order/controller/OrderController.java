package com.hur.gmall.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.annotations.LoginRequired;
import com.hur.gmall.bean.OmsCartItem;
import com.hur.gmall.bean.OmsOrder;
import com.hur.gmall.bean.OmsOrderItem;
import com.hur.gmall.bean.UmsMemberReceiveAddress;
import com.hur.gmall.service.CartService;
import com.hur.gmall.service.OrderService;
import com.hur.gmall.service.SkuService;
import com.hur.gmall.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

@Controller
public class OrderController {

    @Reference
    UserService userService;
    @Reference
    CartService cartService;
    @Reference
    OrderService orderService;
    @Reference
    SkuService skuService;

    @LoginRequired(loginSuccess = true)
    @RequestMapping("submitOrder")
    public ModelAndView submitOrder(String deliveryAddressId, String totalAmount, String tradeCode, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        //检查交易码
        String success = orderService.checkTradeCode(memberId, tradeCode);

        if (success.equals("success")) {
            //订单对象
            OmsOrder omsOrder = new OmsOrder();
            omsOrder.setAutoConfirmDay(7);
            omsOrder.setCreateTime(new Date());
            omsOrder.setDiscountAmount(null);
            //omsOrder.setFreightAmount();  运费 支付后生成物流信息时用
            omsOrder.setMemberId(memberId);
            omsOrder.setMemberUsername(nickname);
            omsOrder.setNote("快点发货");

            String outTradeNo = "gmall";
            outTradeNo = outTradeNo + System.currentTimeMillis();//将毫秒订单戳拼接到外部订单号
            SimpleDateFormat yyyymmddhHmmss = new SimpleDateFormat("YYYYMMDDHHmmss");
            outTradeNo = outTradeNo + yyyymmddhHmmss.format(new Date());//将时间字符串拼接到外部订单号

            omsOrder.setOrderSn(outTradeNo);
            omsOrder.setPayAmount(new BigDecimal(totalAmount));
            omsOrder.setOrderType(1);

            UmsMemberReceiveAddress receiveAddress = userService.getReceiveAddressById(deliveryAddressId);
            omsOrder.setReceiverCity(receiveAddress.getCity());
            omsOrder.setReceiverDetailAddress(receiveAddress.getDetailAddress());
            omsOrder.setReceiverName(receiveAddress.getName());
            omsOrder.setReceiverPhone(receiveAddress.getPhoneNumber());
            omsOrder.setReceiverPostCode(receiveAddress.getPostCode());
            omsOrder.setReceiverProvince(receiveAddress.getProvince());
            omsOrder.setReceiverRegion(receiveAddress.getRegion());
            //当前时间加一天  一天后配送
            Calendar c = Calendar.getInstance();
            c.add(Calendar.DATE,1);
            Date date = c.getTime();
            omsOrder.setReceiveTime(date);
            omsOrder.setSourceType(0);
            omsOrder.setStatus("0");
            omsOrder.setTotalAmount(new BigDecimal(totalAmount));


            List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);
            List<OmsOrderItem> omsOrderItemList = new ArrayList<>();
            for (OmsCartItem cartItem : omsCartItemList) {
                if (cartItem.getIsChecked().equals("1")) {
                    //获得订单详情表
                    //验价
                    boolean check = skuService.checkPrices(cartItem.getProductSkuId(),cartItem.getPrice());
                    //验库存  远程调用库存系统
                    if (check) {
                        OmsOrderItem omsOrderItem = new OmsOrderItem();

                        omsOrderItem.setProductName(cartItem.getProductName());
                        omsOrderItem.setProductPic(cartItem.getProductPic());
                        omsOrderItem.setProductQuantity(cartItem.getQuantity());
                        omsOrderItem.setProductPrice(cartItem.getPrice());
                        omsOrderItem.setOrderSn(outTradeNo);//外部订单号 用来和其它系统交互避免重复
                        omsOrderItem.setRealAmount(cartItem.getQuantity().multiply(cartItem.getPrice()));
                        omsOrderItem.setProductSkuCode(cartItem.getProductSkuCode());//条形码
                        omsOrderItem.setProductSkuId(cartItem.getProductSkuId());
                        omsOrderItem.setProductId(cartItem.getProductId());
                        omsOrderItem.setProductSn("仓库中对应的商品编号");//在仓库中的skuId

                        omsOrderItemList.add(omsOrderItem);
                    }

                }
            }
            omsOrder.setOmsOrderItemList(omsOrderItemList);
            //将订单和订单详情写入数据库同时删除购物车中对应的数据
            orderService.saveOrder(omsOrder);

            ModelAndView modelAndView = new ModelAndView("redirect:http://payment.gmall.com:8087/index");
            modelAndView.addObject("outTradeNo",outTradeNo);
            modelAndView.addObject("totalAmount",totalAmount);
            return modelAndView;

        }else {
            ModelAndView modelAndView = new ModelAndView("tradeFail");
            return modelAndView;
        }

    }


    @LoginRequired(loginSuccess = true)
    @RequestMapping("toTrade")
    public String toTrade(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        List<UmsMemberReceiveAddress> address = userService.getReceiveAddressByMemberId(memberId);

        List<OmsOrderItem> omsOrderItemList = new ArrayList<>();
        BigDecimal totalAmount = new BigDecimal("0");
        List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);
        for (OmsCartItem cartItem : omsCartItemList) {
            if (cartItem.getIsChecked().equals("1")) {
                OmsOrderItem omsOrderItem = new OmsOrderItem();
                omsOrderItem.setProductId(cartItem.getProductId());
                omsOrderItem.setProductPic(cartItem.getProductPic());
                omsOrderItem.setProductName(cartItem.getProductName());
                omsOrderItem.setProductPrice(cartItem.getPrice());
                omsOrderItem.setProductQuantity(cartItem.getQuantity());
                omsOrderItemList.add(omsOrderItem);
                totalAmount = totalAmount.add(cartItem.getQuantity().multiply(cartItem.getPrice()));
            }
        }

        modelMap.put("totalAmount", totalAmount);
        modelMap.put("omsOrderItemList", omsOrderItemList);
        modelMap.put("nickname", nickname);
        modelMap.put("userAddressList", address);

        //生成交易码  在提交订单的时候进行交易码校验
        String tradeCode = orderService.generateTradeCode(memberId);
        modelMap.put("tradeCode", tradeCode);

        return "trade";
    }
}
