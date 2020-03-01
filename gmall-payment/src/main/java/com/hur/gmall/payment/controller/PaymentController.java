package com.hur.gmall.payment.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.hur.gmall.annotations.LoginRequired;
import com.hur.gmall.bean.OmsOrder;
import com.hur.gmall.bean.PaymentInfo;
import com.hur.gmall.payment.config.AlipayConfig;
import com.hur.gmall.service.OrderService;
import com.hur.gmall.service.PaymentService;
import org.apache.commons.lang3.StringUtils;
import org.assertj.core.internal.cglib.asm.$AnnotationVisitor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.stereotype.Service;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PaymentController {
    @Autowired
    AlipayClient alipayClient;
    @Reference
    OrderService orderService;
    @Autowired
    PaymentService paymentService;


    @LoginRequired(loginSuccess = true)
    @RequestMapping("alipay/callback/return")
    public String alipayCallbackReturn(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap, HttpServletResponse response) {

        //从回调请求中获取支付宝参数
        String sign = request.getParameter("sign");
        String trade_no = request.getParameter("trade_no");
        String out_trade_no = request.getParameter("out_trade_no");
        String total_amount = request.getParameter("total_amount");
        String trade_status = request.getParameter("trade_status");
        String subject = request.getParameter("subject");
        String callBack = request.getQueryString();

        //通过支付宝的paramMap进行签名验证 2.0版本的接口将paramMap参数去掉了导致同步请求无法验签
        if (StringUtils.isNotBlank(sign)){
            //验签成功
            PaymentInfo paymentInfo = new PaymentInfo();
            paymentInfo.setPaymentStatus("已付款");
            paymentInfo.setAlipayTradeNo(trade_no);//支付宝的交易凭证号
            paymentInfo.setOrderSn(out_trade_no);//商户订单号
            paymentInfo.setCallbackContent(callBack);
            paymentInfo.setCallbackTime(new Date());
            //更新用户支付状态
            //进行支付成功的幂等性检查
            paymentService.updatePaymentInfo(paymentInfo);
            //支付成功后引发的系统服务：订单服务更新 》库存服务 》物流服务
        }





        return "finish";
    }


    @LoginRequired(loginSuccess = true)
    @RequestMapping("wx/submit")
    public String wx(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap, HttpServletResponse response) {

        return null;
    }

    @ResponseBody
    @LoginRequired(loginSuccess = true)
    @RequestMapping("alipay/submit")
    public String alipay(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap, HttpServletResponse response) {

        //获取一个支付宝请求的客户端（他不是一个链接 而是一个封装好的http表单请求）
        String from = "";
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();//创建api对应的request

        //回调函数
        alipayRequest.setReturnUrl(AlipayConfig.return_payment_url);
        alipayRequest.setNotifyUrl(AlipayConfig.notify_payment_url);
        Map<String,Object> map = new HashMap<>();

        map.put("out_trade_no",outTradeNo);
        map.put("product_code","FAST_INSTANT_TRADE_PAY");
        map.put("total_amount",0.01);
        map.put("subject","锦鲤");


        String s = JSON.toJSONString(map);
        alipayRequest.setBizContent(s);

        try {
            from = alipayClient.pageExecute(alipayRequest).getBody();//调用SDK生成表单
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        System.out.println(from);

        //生成并保存用户的支付信息
        OmsOrder omsOrder = orderService.getOrderByOutTradeNo(outTradeNo);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setAlipayTradeNo(outTradeNo);
        paymentInfo.setCreateTime(new Date());
        paymentInfo.setOrderId(omsOrder.getId());
        paymentInfo.setPaymentStatus("未付款");
        paymentInfo.setSubject("锦鲤");
        paymentInfo.setTotalAmount(totalAmount);
        paymentInfo.setOrderSn(omsOrder.getOrderSn());
        paymentService.savePaymentInfo(paymentInfo);

        //向消息中间件发送一个检查支付状态（支付服务消费）的延迟消息队列
        paymentService.sendDelayPaymentResultCheckQueue(outTradeNo,5);

        //提交请求到支付宝
        return from;
    }

    @LoginRequired(loginSuccess = true)
    @RequestMapping("index")
    public String index(String outTradeNo, BigDecimal totalAmount, HttpServletRequest request, ModelMap modelMap, HttpServletResponse response){

        String memberId = (String) request.getAttribute("memberId");
        String nickname = (String) request.getAttribute("nickname");

        modelMap.put("nickname",nickname);
        modelMap.put("outTradeNo",outTradeNo);
        modelMap.put("totalAmount",totalAmount);
        return "index";
    }
}
