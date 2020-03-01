package com.hur.gmall.search;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.bean.PmsSearchSkuInfo;
import com.hur.gmall.bean.PmsSkuInfo;
import com.hur.gmall.service.SkuService;
import io.searchbox.client.JestClient;
import io.searchbox.core.Index;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallSearchServiceImplApplicationTests {

    @Reference
    SkuService skuService;

    @Autowired
    JestClient jestClient;

    @Test
    public void contextLoads() throws IOException {
        //查询Mysql数据
        List<PmsSkuInfo> pmsSkuInfoList = skuService.getAllSkuInfo();

        //转换为es数据结构
        List<PmsSearchSkuInfo> pmsSearchSkuInfoList = new ArrayList<>();

        for (PmsSkuInfo pmsSkuInfo:pmsSkuInfoList){
            PmsSearchSkuInfo pmsSearchSkuInfo = new PmsSearchSkuInfo();
            BeanUtils.copyProperties(pmsSkuInfo,pmsSearchSkuInfo);

            pmsSearchSkuInfoList.add(pmsSearchSkuInfo);
        }

        //存入es
        for (PmsSearchSkuInfo pmsSearchSkuInfo:pmsSearchSkuInfoList){
            Index put = new Index.Builder(pmsSearchSkuInfo).index("gmall0105").type("pmsSkuInfo")
                    .id(pmsSearchSkuInfo.getId()).build();
            jestClient.execute(put);
        }

    }

}
