package com.hur.gmall.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.annotations.LoginRequired;
import com.hur.gmall.bean.OmsCartItem;
import com.hur.gmall.bean.PmsSkuInfo;
import com.hur.gmall.cart.util.CookieUtil;
import com.hur.gmall.service.CartService;
import com.hur.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Controller
public class CartController {
    @Reference
    SkuService skuService;
    @Reference
    CartService cartService;

    List<OmsCartItem> omsCartItemList = new ArrayList<>();

//添加商品到购物车
    @LoginRequired(loginSuccess = false)
    @RequestMapping("addToCart")
    public String addToCart(String skuId, int quantity, HttpServletRequest request, HttpServletResponse response) {

        //调用商品服务查询商品信息
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);

        //将商品信息封装成购物车信息
        OmsCartItem omsCartItem = new OmsCartItem();
        omsCartItem.setCreateDate(new Date());
        omsCartItem.setDeleteStatus(0);
        omsCartItem.setModifyDate(new Date());
        omsCartItem.setPrice(skuInfo.getPrice());
        omsCartItem.setProductAttr("");
        omsCartItem.setProductBrand("14");
        omsCartItem.setProductCategoryId(skuInfo.getCatalog3Id());
        omsCartItem.setProductId(skuInfo.getProductId());
        omsCartItem.setProductName(skuInfo.getSkuName());
        omsCartItem.setProductPic(skuInfo.getSkuDefaultImg());
        omsCartItem.setProductSkuCode("1111111111");
        omsCartItem.setProductSkuId(skuId);
        omsCartItem.setQuantity(new BigDecimal(quantity));
        //判断用户是否登录
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");


        if (StringUtils.isNotBlank(memberId)) {
            //用户已登录
            OmsCartItem cartItemByUser = cartService.getCartByUserAndSkuId(memberId, skuId);

            if (cartItemByUser == null) {
                //用户购物车中不存在该商品
                omsCartItem.setMemberId(memberId);
                omsCartItem.setMemberNickname(nickname);
                omsCartItem.setQuantity(new BigDecimal(quantity));
                cartService.addCart(omsCartItem);
            } else {
                //用户购物车中存在该商品
                cartItemByUser.setQuantity(omsCartItem.getQuantity().add(cartItemByUser.getQuantity()));
                cartService.updateCart(cartItemByUser);
            }
            cartService.flushCartCache(memberId);
        } else {
            //用户未登录
            List<OmsCartItem> omsCartItemList = new ArrayList<>();
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);

            if (StringUtils.isNotBlank(cartListCookie)) {
                //cookie不为空
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
                Boolean cookieExit = if_cookie_exit(omsCartItemList, omsCartItem);
                if (cookieExit) {
                    //cookie已经存在
                    for (OmsCartItem cartItem : omsCartItemList) {
                        if (cartItem.getProductSkuId().equals(omsCartItem.getProductSkuId())) {
                            cartItem.setQuantity(cartItem.getQuantity().add(omsCartItem.getQuantity()));
                        }
                    }
                } else {
                    //cookie尚未存在
                    omsCartItemList.add(omsCartItem);
                }
            } else {
                //cookie为空
                omsCartItemList.add(omsCartItem);

            }
            CookieUtil.setCookie(request, response, "cartListCookie", JSON.toJSONString(omsCartItemList), 60 * 60 * 72, true);

        }

        return "redirect:/success.html";
    }

    //显示购物车
    @LoginRequired(loginSuccess = false)
    @RequestMapping("cartList")
    public String cartList(HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        List<OmsCartItem> omsCartItemList = new ArrayList<>();
        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");


        if (StringUtils.isBlank(memberId)) {
            //用户未登录查询cookie
            String cartListCookie = CookieUtil.getCookieValue(request, "cartListCookie", true);
            if (StringUtils.isNotBlank(cartListCookie)) {
                omsCartItemList = JSON.parseArray(cartListCookie, OmsCartItem.class);
            }
        } else {
            //用户已登录查询db
            omsCartItemList = cartService.cartList(memberId);
        }

        for (OmsCartItem cartItem : omsCartItemList) {
            cartItem.setTotalPrice(cartItem.getPrice().multiply(cartItem.getQuantity()));
        }


        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("cartList", omsCartItemList);


        return "cartList";
    }

    //购物车内嵌页
    @LoginRequired(loginSuccess = false)
    @RequestMapping("checkCart")
    public String checkCart(String isChecked,String skuId, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {

        String memberId = (String)request.getAttribute("memberId");
        String nickname = (String)request.getAttribute("nickname");


        //调用服务修改状态
        OmsCartItem cartItem = new OmsCartItem();
        cartItem.setProductSkuId(skuId);
        cartItem.setMemberId(memberId);
        cartItem.setIsChecked(isChecked);
        cartService.checkCart(cartItem);

        //将最新的数据从缓存中取出 渲染给内嵌页面
        List<OmsCartItem> omsCartItemList = cartService.cartList(memberId);
        for (OmsCartItem omsCartItem : omsCartItemList) {
            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
        }
        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
        modelMap.put("totalAmount",totalAmount);
        modelMap.put("cartList",omsCartItemList);

        return "cartListInner";
    }





//    @RequestMapping("quantityChange")
//    public String quantityChange(BigDecimal quantity,String productSkuId, HttpServletRequest request, HttpServletResponse response, ModelMap modelMap) {
//
//        String userId = "1";
//
//        //修改服务状态
//        OmsCartItem cartItem = new OmsCartItem();
//        cartItem.setQuantity(quantity);
//        cartItem.setProductSkuId(productSkuId);
//        cartItem.setMemberId(userId);
//        cartService.checkCart(cartItem);
//
//        //返回给inner
//        List<OmsCartItem> omsCartItemList = cartService.cartList(userId);
//        for (OmsCartItem omsCartItem : omsCartItemList) {
//            omsCartItem.setTotalPrice(omsCartItem.getPrice().multiply(omsCartItem.getQuantity()));
//        }
//        BigDecimal totalAmount = getTotalAmount(omsCartItemList);
//        modelMap.put("totalAmount",totalAmount);
//        modelMap.put("cartList",omsCartItemList);
//
//        return "cartListInner";
//    }

        private BigDecimal getTotalAmount(List<OmsCartItem> omsCartItemList) {

        BigDecimal totalAmount = new BigDecimal("0");
        for (OmsCartItem cartItem : omsCartItemList) {
            if (cartItem.getIsChecked().equals("1")){
                BigDecimal totalPrice = cartItem.getQuantity().multiply(cartItem.getPrice());
                totalAmount = totalAmount.add(totalPrice);
            }
        }
        return totalAmount;
    }


    private Boolean if_cookie_exit(List<OmsCartItem> omsCartItemList, OmsCartItem omsCartItem) {
        String productSkuId = omsCartItem.getProductSkuId();
        for (OmsCartItem cartItem : omsCartItemList) {
            if (cartItem.getProductSkuId().equals(productSkuId)) {
                return true;
            }
        }
        return false;
    }

}
