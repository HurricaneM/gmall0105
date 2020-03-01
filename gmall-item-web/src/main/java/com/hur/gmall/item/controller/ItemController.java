package com.hur.gmall.item.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.PmsProductSaleAttr;
import com.hur.gmall.bean.PmsSkuInfo;
import com.hur.gmall.bean.PmsSkuSaleAttrValue;
import com.hur.gmall.service.SkuService;
import com.hur.gmall.service.SpuService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class ItemController {

    @Reference
    SkuService skuService;
    @Reference
    SpuService spuService;

    @RequestMapping("index")
    public String test(){
        return "index";
    }

    @RequestMapping("{skuId}.html")
    public String item(@PathVariable String skuId, ModelMap modelMap){
        //sku对象
        PmsSkuInfo skuInfo = skuService.getSkuById(skuId);
        //销售属性列表
        List<PmsProductSaleAttr> pmsProductSaleAttrList =
                spuService.spuSaleAttrListCheckBySku(skuInfo.getProductId(),skuInfo.getId());

        modelMap.put("skuInfo",skuInfo);
        modelMap.put("spuSaleAttrListCheckBySku",pmsProductSaleAttrList);

        //查询当前sku上的spu下的其他sku集合
        Map<String,String> skuSaleAttrValueHash = new HashMap<>();
        List<PmsSkuInfo> skuInfoList = skuService.getSkuSaleAttrValueBySpu(skuInfo.getProductId());

        for (PmsSkuInfo pmsSkuInfo : skuInfoList) {
            StringBuilder key = new StringBuilder();
            String value = pmsSkuInfo.getId();

            List<PmsSkuSaleAttrValue> skuSaleAttrValueList = pmsSkuInfo.getSkuSaleAttrValueList();

            for (PmsSkuSaleAttrValue pmsSkuSaleAttrValue : skuSaleAttrValueList) {
                key.append(pmsSkuSaleAttrValue.getSaleAttrValueId()).append("|");
            }

            skuSaleAttrValueHash.put(key.toString(),value);
        }

        //将sku销售属性hash表放到页面
        String skuSaleAttrValueJsonStr = JSON.toJSONString(skuSaleAttrValueHash);

        modelMap.put("skuSaleAttrValueHash",skuSaleAttrValueJsonStr);

        return "item";
    }

}
