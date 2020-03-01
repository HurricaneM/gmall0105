package com.hur.gmall.service;

import com.hur.gmall.bean.PmsBaseSaleAttr;
import com.hur.gmall.bean.PmsProductImage;
import com.hur.gmall.bean.PmsProductInfo;
import com.hur.gmall.bean.PmsProductSaleAttr;

import java.util.List;

public interface SpuService {
    public List<PmsProductInfo> spuList(String catalog3Id);

    List<PmsBaseSaleAttr> baseSaleAttrList();

    String saveSpuInfo(PmsProductInfo pmsProductInfo);

    List<PmsProductSaleAttr> spuSaleAttrList(String spuId);

    List<PmsProductImage> spuImageList(String spuId);

    List<PmsProductSaleAttr> spuSaleAttrListCheckBySku(String productId,String skuId);
}
