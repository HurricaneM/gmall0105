package com.hur.gmall.search.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.annotations.LoginRequired;
import com.hur.gmall.bean.*;
import com.hur.gmall.service.AttrService;
import com.hur.gmall.service.SearchService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
public class SearchController {

    @Reference
    SearchService searchService;

    @Reference
    AttrService attrService;

    @RequestMapping("/list.html")
    public String list(PmsSearchParam pmsSearchParam, ModelMap modelMap) {//三级分类Id，关键字
        //调用搜索服务  返回搜索结果
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = searchService.list(pmsSearchParam);
        modelMap.put("skuLsInfoList", pmsSearchSkuInfoList);

        //抽取检索结果所包含的平台属性id
        Set<String> pmsBaseAttrIdSet = new HashSet<>();
        for (PmsSearchSkuInfo pmsSearchSkuInfo : pmsSearchSkuInfoList) {
            for (PmsSkuAttrValue pmsSkuAttrValue : pmsSearchSkuInfo.getSkuAttrValueList()) {
                pmsBaseAttrIdSet.add(pmsSkuAttrValue.getValueId());
            }
        }

        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = attrService.getAttrValueListByValueId(pmsBaseAttrIdSet);
        String urlParam = getUrlParam(pmsSearchParam);
        //对平台属性集合进行进一步处理 去掉当前条件中valueId所在的属性组
        String[] delValueIds = pmsSearchParam.getValueId();
        //面包屑
        List<PmsSearchCrumb> pmsSearchCrumbList = new ArrayList<>();
        if (delValueIds != null) {
            for (String delValueId : delValueIds) {

                Iterator<PmsBaseAttrInfo> iterator = pmsBaseAttrInfoList.iterator();


                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
                pmsSearchCrumb.setValueId(delValueId);
                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));


                while (iterator.hasNext()) {
                    PmsBaseAttrInfo pmsBaseAttrInfo = iterator.next();
                    List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
                    for (PmsBaseAttrValue pmsBaseAttrValue : attrValueList) {

                        String valueId = pmsBaseAttrValue.getId();
                        if (delValueId.equals(valueId)) {
                            //面包屑的属性值名称
                            pmsSearchCrumb.setValueName(pmsBaseAttrValue.getValueName());
                            //删除该属性值所在的属性组
                            iterator.remove();
                        }
                    }
                }

                pmsSearchCrumbList.add(pmsSearchCrumb);
            }
        }
        String keyword = pmsSearchParam.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            modelMap.put("keyword", keyword);

        }


//        if (delValueIds != null) {
//            //如果valueId不为空，说明当前请求中包含属性参数，每一个属性参数都会生成一个面面包屑
//            for (String delValueId : delValueIds) {
//                PmsSearchCrumb pmsSearchCrumb = new PmsSearchCrumb();
//                pmsSearchCrumb.setValueId(delValueId);
//                pmsSearchCrumb.setValueName(delValueId);
//                pmsSearchCrumb.setUrlParam(getUrlParam(pmsSearchParam, delValueId));
//                pmsSearchCrumbList.add(pmsSearchCrumb);
//            }
//        }
        modelMap.put("attrValueSelectedList", pmsSearchCrumbList);

        modelMap.put("urlParam", urlParam);
        modelMap.put("attrList", pmsBaseAttrInfoList);
        return "list";
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam) {

        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&catalog3Id=" + catalog3Id;
            } else {
                urlParam = "catalog3Id=" + catalog3Id;
            }
        }

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&keyword=" + keyword;
            } else {
                urlParam = "keyword=" + keyword;
            }
        }

        if (valueId != null) {
            for (String attrValueId : valueId) {
                urlParam = urlParam + "&valueId=" + attrValueId;
            }
        }

        return urlParam;
    }

    private String getUrlParam(PmsSearchParam pmsSearchParam, String delValueId) {

        String catalog3Id = pmsSearchParam.getCatalog3Id();
        String keyword = pmsSearchParam.getKeyword();
        String[] valueId = pmsSearchParam.getValueId();

        String urlParam = "";
        if (StringUtils.isNotBlank(catalog3Id)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&catalog3Id=" + catalog3Id;
            } else {
                urlParam = "catalog3Id=" + catalog3Id;
            }
        }

        if (StringUtils.isNotBlank(keyword)) {
            if (StringUtils.isNotBlank(urlParam)) {
                urlParam = urlParam + "&keyword=" + keyword;
            } else {
                urlParam = "keyword=" + keyword;
            }
        }

        if (valueId != null) {
            for (String attrValueId : valueId) {
                if (!delValueId.equals(attrValueId)) {
                    urlParam = urlParam + "&valueId=" + attrValueId;
                }
            }
        }

        return urlParam;
    }

    @LoginRequired(loginSuccess = false)
    @RequestMapping("/index")
    public String toIndex() {
        return "index";
    }
}
