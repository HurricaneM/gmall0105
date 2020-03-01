package com.hur.gmall.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.bean.PmsBaseAttrInfo;
import com.hur.gmall.bean.PmsBaseAttrValue;
import com.hur.gmall.bean.PmsBaseCatalog2;
import com.hur.gmall.service.AttrService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@CrossOrigin
public class AttrController {

    @Reference
    AttrService attrService;

    @ResponseBody
    @RequestMapping("attrInfoList")
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id){
        List<PmsBaseAttrInfo> list = attrService.attrInfoList(catalog3Id);
        return list;
    }

    @ResponseBody
    @RequestMapping("saveAttrInfo")
    public String saveAttrInfo(@RequestBody PmsBaseAttrInfo pmsBaseAttrInfo){
        attrService.saveAttrInfo(pmsBaseAttrInfo);
        return "success";
    }

    @ResponseBody
    @RequestMapping("getAttrValueList")
    public List<PmsBaseAttrValue> getAttrValueList(String attrId){
        List<PmsBaseAttrValue> attrValueList = attrService.getAttrValueList(attrId);
        return attrValueList;
    }

}
