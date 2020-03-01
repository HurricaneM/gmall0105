//package com.hur.gmall.manage;
//
//
//import org.csource.common.MyException;
//import org.csource.fastdfs.ClientGlobal;
//import org.csource.fastdfs.StorageClient;
//import org.csource.fastdfs.TrackerClient;
//import org.csource.fastdfs.TrackerServer;
//import org.junit.Test;
//import org.junit.runner.RunWith;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.test.context.junit4.SpringRunner;
//
//
//import java.io.IOException;
//
//@SpringBootTest
//@RunWith(SpringRunner.class)
//class GmallManageWebApplicationTests {
//
//    @Test
//    public void contextLoad() throws IOException, MyException {
//
//        //配置fdfs的全局链接地址
//        String tracker = GmallManageWebApplicationTests.class.getResource("tracker.conf").getPath();
//
//        ClientGlobal.init(tracker);
//
//        TrackerClient trackerClient = new TrackerClient();
//
//        //获得一个Tracker实例
//        TrackerServer trackerServer = trackerClient.getTrackerServer();
//
//
//        //通过Tracker获得一个Storage链接客户端
//        StorageClient storageClient = new StorageClient(trackerServer,null);
//
//        String[] list = storageClient.upload_file("F:/wallpaper/samurai-silhouette-lanterns-mountain.jpeg",
//                 "jpeg", null);
//
//        for (String s:list){
//            System.out.println(s);
//        }
//
//    }
//
//
//
//}
package com.hur.gmall.manage;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest
public class GmallManageWebApplicationTests {

    @Test
    public void contextLoads() throws IOException, MyException {

        //配置fdfs的全局链接地址
        String tracker = GmallManageWebApplicationTests.class.getResource("/tracker.conf").getPath();

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();

        //获得一个Tracker实例
        TrackerServer trackerServer = trackerClient.getTrackerServer();


        //通过Tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);

        String[] list = storageClient.upload_file("F:/wallpaper/samurai-silhouette-lanterns-mountain.jpeg",
                 "jpeg", null);

        String address = "http://192.168.115.129";
        for (String s:list){
            address = address.concat("/").concat(s);
            System.out.println(s);
            System.out.println(address);
        }
    }

}

