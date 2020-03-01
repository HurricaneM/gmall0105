package com.hur.gmall.service;

import com.hur.gmall.bean.PmsSkuInfo;

import java.math.BigDecimal;
import java.util.List;

public interface SkuService {

    String saveSkuInfo(PmsSkuInfo pmsSkuInfo);

    PmsSkuInfo getSkuById(String skuId);

    List<PmsSkuInfo> getSkuSaleAttrValueBySpu(String productId);

    PmsSkuInfo getSkuByIdFromDb(String skuId);

    List<PmsSkuInfo> getAllSkuInfo();

    boolean checkPrices(String productSkuId, BigDecimal price);
}
