package com.hur.gmall.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.hur.gmall.bean.PmsBaseAttrInfo;
import com.hur.gmall.bean.PmsBaseAttrValue;
import com.hur.gmall.manage.mapper.PmsBaseAttrInfoMapper;
import com.hur.gmall.manage.mapper.PmsBaseAttrValueMapper;
import com.hur.gmall.service.AttrService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
import java.util.Set;

@Service
public class AttrServiceImpl implements AttrService {
    @Autowired
    PmsBaseAttrInfoMapper pmsBaseAttrInfoMapper;
    @Autowired
    PmsBaseAttrValueMapper pmsBaseAttrValueMapper;

    @Override
    public List<PmsBaseAttrInfo> attrInfoList(String catalog3Id) {
        PmsBaseAttrInfo pmsBaseAttrInfo = new PmsBaseAttrInfo();
        pmsBaseAttrInfo.setCatalog3Id(catalog3Id);
        List<PmsBaseAttrInfo> baseAttrInfos = pmsBaseAttrInfoMapper.select(pmsBaseAttrInfo);

        for (PmsBaseAttrInfo baseAttrInfo:baseAttrInfos){

            PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
            pmsBaseAttrValue.setAttrId(baseAttrInfo.getId());
            List<PmsBaseAttrValue> baseAttrValueList = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

            baseAttrInfo.setAttrValueList(baseAttrValueList);
        }



        return baseAttrInfos;
    }

    @Override
    public String saveAttrInfo(PmsBaseAttrInfo pmsBaseAttrInfo) {

        String id = pmsBaseAttrInfo.getId();

        if (StringUtils.isBlank(id)){
            //id为空  保存
            //保存属性
            pmsBaseAttrInfoMapper.insertSelective(pmsBaseAttrInfo);

            //保存属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            for (PmsBaseAttrValue attrValue:attrValueList){
                attrValue.setAttrId(pmsBaseAttrInfo.getId());
                pmsBaseAttrValueMapper.insertSelective(attrValue);
            }
        }else{
            //id不为空 修改
            //属性
            Example example = new Example(PmsBaseAttrInfo.class);
            example.createCriteria().andEqualTo("id",pmsBaseAttrInfo.getId());
            pmsBaseAttrInfoMapper.updateByExampleSelective(pmsBaseAttrInfo,example);
            //属性值
            List<PmsBaseAttrValue> attrValueList = pmsBaseAttrInfo.getAttrValueList();
            //按照属性id删除所有属性值
            PmsBaseAttrValue attrValue = new PmsBaseAttrValue();
            attrValue.setAttrId(pmsBaseAttrInfo.getId());
            pmsBaseAttrValueMapper.delete(attrValue);
            //保存新的属性值
            for (PmsBaseAttrValue pmsBaseAttrValue:attrValueList){
                if (pmsBaseAttrValue.getAttrId()==null){
                    pmsBaseAttrValue.setAttrId(pmsBaseAttrInfo.getId());
                }
                pmsBaseAttrValueMapper.insertSelective(pmsBaseAttrValue);
            }
        }

        return "success";
    }

    @Override
    public List<PmsBaseAttrValue> getAttrValueList(String attrId) {

        PmsBaseAttrValue pmsBaseAttrValue = new PmsBaseAttrValue();
        pmsBaseAttrValue.setAttrId(attrId);
        List<PmsBaseAttrValue> attrValueList = pmsBaseAttrValueMapper.select(pmsBaseAttrValue);

        return attrValueList;
    }

    @Override
    public List<PmsBaseAttrInfo> getAttrValueListByValueId(Set<String> pmsBaseAttrIdSet) {

        String valueIdStr = StringUtils.join(pmsBaseAttrIdSet, ",");
        List<PmsBaseAttrInfo> pmsBaseAttrInfoList = pmsBaseAttrInfoMapper.selectByPmsBaseAttrIdSet(valueIdStr);

        return pmsBaseAttrInfoList;
    }


}
