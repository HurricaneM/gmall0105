package com.hur.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.bean.PmsSkuInfo;
import com.hur.gmall.service.SkuService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@CrossOrigin
public class SkuController {
    @Reference
    SkuService skuService;

    @ResponseBody
    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody PmsSkuInfo pmsSkuInfo){


        System.out.println(pmsSkuInfo.getId());

        pmsSkuInfo.setProductId(pmsSkuInfo.getSpuId());

        //处理默认图片
        String skuDefaultImg = pmsSkuInfo.getSkuDefaultImg();
        if (StringUtils.isBlank(skuDefaultImg)){
            pmsSkuInfo.setSkuDefaultImg(pmsSkuInfo.getSkuImageList().get(0).getImgUrl());
        }

        skuService.saveSkuInfo(pmsSkuInfo);

        return "success";
    }
}
