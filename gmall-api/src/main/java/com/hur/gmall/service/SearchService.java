package com.hur.gmall.service;

import com.hur.gmall.bean.PmsSearchParam;
import com.hur.gmall.bean.PmsSearchSkuInfo;

import java.util.List;

public interface SearchService {
    List<PmsSearchSkuInfo> list(PmsSearchParam pmsSearchParam);
}
