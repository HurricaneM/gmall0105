package com.hur.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.bean.PmsBaseSaleAttr;
import com.hur.gmall.bean.PmsProductImage;
import com.hur.gmall.bean.PmsProductInfo;
import com.hur.gmall.bean.PmsProductSaleAttr;
import com.hur.gmall.manage.util.PmsUploadUtil;
import com.hur.gmall.service.SpuService;
import org.csource.common.MyException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Controller
@CrossOrigin
public class SpuController {

    @Reference
    SpuService spuService;

    @ResponseBody
    @RequestMapping("spuList")
    public List<PmsProductInfo> spuList(String catalog3Id){

        List<PmsProductInfo> productInfos = spuService.spuList(catalog3Id);
        return productInfos;

    }

    @ResponseBody
    @RequestMapping("baseSaleAttrList")
    public List<PmsBaseSaleAttr> baseSaleAttrList(){

        List<PmsBaseSaleAttr> baseSaleAttrList = spuService.baseSaleAttrList();
        return baseSaleAttrList;

    }


    @ResponseBody
    @RequestMapping("fileUpload")
    public String fileUpload(@RequestParam("file") MultipartFile multipartFile) throws IOException, MyException {

        //将文件上传到分布式的文件存储系统
        String imageUrl = PmsUploadUtil.uploadImage(multipartFile);

        //将图片的存储路径返回给页面
        return imageUrl;
    }

    @ResponseBody
    @RequestMapping("saveSpuInfo")
    public String saveSpuInfo(@RequestBody PmsProductInfo pmsProductInfo){

        spuService.saveSpuInfo(pmsProductInfo);

        return "success";

    }

    @ResponseBody
    @RequestMapping("spuSaleAttrList")
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId){

        List<PmsProductSaleAttr> productSaleAttrList = spuService.spuSaleAttrList(spuId);

        return productSaleAttrList;
    }

    @ResponseBody
    @RequestMapping("spuImageList")
    public List<PmsProductImage> spuImageList(String spuId){

        List<PmsProductImage> productImages = spuService.spuImageList(spuId);

        return productImages;
    }



}
