package com.hur.gmall.manage.util;

import org.csource.common.MyException;
import org.csource.fastdfs.ClientGlobal;
import org.csource.fastdfs.StorageClient;
import org.csource.fastdfs.TrackerClient;
import org.csource.fastdfs.TrackerServer;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class PmsUploadUtil {


    public static String uploadImage(MultipartFile multipartFile) throws IOException, MyException {

        //配置fdfs的全局链接地址
        String tracker = PmsUploadUtil.class.getResource("/tracker.conf").getPath();

        ClientGlobal.init(tracker);

        TrackerClient trackerClient = new TrackerClient();

        //获得一个Tracker实例
        TrackerServer trackerServer = trackerClient.getTrackerServer();


        //通过Tracker获得一个Storage链接客户端
        StorageClient storageClient = new StorageClient(trackerServer,null);

        byte[] bytes = multipartFile.getBytes();
        String originalFilename = multipartFile.getOriginalFilename();
        int i = originalFilename.lastIndexOf('.');
        String expName = originalFilename.substring(i + 1);

        String[] list = storageClient.upload_file(bytes,expName, null);

        String address = "http://192.168.115.129";
        for (String s:list){
            address = address.concat("/").concat(s);
            System.out.println(s);
            System.out.println(address);
        }

        return address;
    }
}
