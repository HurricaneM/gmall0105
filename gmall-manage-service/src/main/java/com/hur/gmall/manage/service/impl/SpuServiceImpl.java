package com.hur.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hur.gmall.bean.*;
import com.hur.gmall.manage.mapper.*;
import com.hur.gmall.service.SpuService;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class SpuServiceImpl implements SpuService {
    @Autowired
    PmsBaseSaleAttrMapper pmsBaseSaleAttrMapper;
    @Autowired
    PmsProductInfoMapper pmsProductInfoMapper;
    @Autowired
    PmsProductSaleAttrMapper pmsProductSaleAttrMapper;
    @Autowired
    PmsProductSaleAttrValueMapper pmsProductSaleAttrValueMapper;
    @Autowired
    PmsProductImageMapper pmsProductImageMapper;


    @Override
    public List<PmsProductInfo> spuList(String catalog3Id) {
        PmsProductInfo pmsProductInfo = new PmsProductInfo();
        pmsProductInfo.setCatalog3Id(catalog3Id);

        List<PmsProductInfo> productInfos = pmsProductInfoMapper.select(pmsProductInfo);
        return productInfos;
    }

    @Override
    public List<PmsBaseSaleAttr> baseSaleAttrList() {
        List<PmsBaseSaleAttr> baseSaleAttrList = pmsBaseSaleAttrMapper.selectAll();
        return baseSaleAttrList;
    }

    @Override
    public String saveSpuInfo(PmsProductInfo pmsProductInfo) {

        //保存商品信息
        pmsProductInfoMapper.insert(pmsProductInfo);
        List<PmsProductSaleAttr> spuSaleAttrList = pmsProductInfo.getSpuSaleAttrList();

        List<PmsProductInfo> productInfos = pmsProductInfoMapper.select(pmsProductInfo);
        String productId = productInfos.get(0).getId();

        for (PmsProductSaleAttr productSaleAttr:spuSaleAttrList){

            //保存商品销售属性
            productSaleAttr.setProductId(productId);
            pmsProductSaleAttrMapper.insert(productSaleAttr);
            //保存商品销售属性值
            List<PmsProductSaleAttrValue> spuSaleAttrValueList = productSaleAttr.getSpuSaleAttrValueList();
            for (PmsProductSaleAttrValue productSaleAttrValue:spuSaleAttrValueList){
                productSaleAttrValue.setProductId(productId);
                pmsProductSaleAttrValueMapper.insert(productSaleAttrValue);
            }
        }

        //保存商品图片
        List<PmsProductImage> spuImageList = pmsProductInfo.getSpuImageList();
        for (PmsProductImage productImage:spuImageList){
            productImage.setProductId(productId);
            pmsProductImageMapper.insert(productImage);
        }


        return "success";
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrList(String spuId) {

        PmsProductSaleAttr productSaleAttr = new PmsProductSaleAttr();
        productSaleAttr.setProductId(spuId);
        List<PmsProductSaleAttr> productSaleAttrList = pmsProductSaleAttrMapper.select(productSaleAttr);

        for (PmsProductSaleAttr productSaleAttr1:productSaleAttrList){

            PmsProductSaleAttrValue productSaleAttrValue = new PmsProductSaleAttrValue();
            productSaleAttrValue.setProductId(spuId);
            productSaleAttrValue.setSaleAttrId(productSaleAttr1.getSaleAttrId());
            List<PmsProductSaleAttrValue> productSaleAttrValueList =
                    pmsProductSaleAttrValueMapper.select(productSaleAttrValue);
            productSaleAttr1.setSpuSaleAttrValueList(productSaleAttrValueList);
        }

        return productSaleAttrList;
    }

    @Override
    public List<PmsProductImage> spuImageList(String spuId) {

        PmsProductImage productImage = new PmsProductImage();
        productImage.setProductId(spuId);
        List<PmsProductImage> pmsProductImages = pmsProductImageMapper.select(productImage);

        return pmsProductImages;
    }

    @Override
    public List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId) {


//        for (PmsProductSaleAttr productSaleAttr:productSaleAttrList) {
//
//            PmsProductSaleAttrValue productSaleAttrValue = new PmsProductSaleAttrValue();
//            productSaleAttrValue.setSaleAttrId(productSaleAttr.getSaleAttrId());
//            productSaleAttrValue.setProductId(productId);
//            List<PmsProductSaleAttrValue> productSaleAttrValueList =
//                    pmsProductSaleAttrValueMapper.select(productSaleAttrValue);
//
//            productSaleAttr.setSpuSaleAttrValueList(productSaleAttrValueList);
//        }

        List<PmsProductSaleAttr> pmsProductSaleAttrList =
                pmsProductSaleAttrMapper.selectSpuSaleAttrListCheckedBySku(productId,skuId);

        return pmsProductSaleAttrList;
    }

}
