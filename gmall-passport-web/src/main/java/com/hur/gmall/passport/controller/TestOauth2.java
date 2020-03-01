package com.hur.gmall.passport.controller;

import com.alibaba.fastjson.JSON;
import com.hur.gmall.util.CookieUtil;
import com.hur.gmall.util.HttpclientUtil;

import java.util.HashMap;
import java.util.Map;

public class TestOauth2 {


    public static void main(String[] args) {
        //client id(app key) = 3400407400
        //http://passport.gmall.com:8085/vlogin
        //https://api.weibo.com/oauth2/authorize?client_id=3400407400&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin

//        String s1 = HttpclientUtil.doGet("https://api.weibo.com/oauth2/authorize?client_id=3400407400&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin");
//        System.out.println(s1);

        //code = 01c3624bd527cd51e9899d63e6d29c4b
        //secret = e18f3af3e838d675c746fc95c5f3da83
        String code = "01c3624bd527cd51e9899d63e6d29c4b";
        String s2 = "http://passport.gmall.com:8085/vlogin?code=01c3624bd527cd51e9899d63e6d29c4b";

        //https://api.weibo.com/oauth2/access_token?client_id=3400407400&client_secret=e18f3af3e838d675c746fc95c5f3da83&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=CODE


        //3
//        Map<String,String> paramMap = new HashMap<>();
//        paramMap.put("client_id","3400407400");
//        paramMap.put("client_secret","e18f3af3e838d675c746fc95c5f3da83");
//        paramMap.put("grant_type","authorization_code");
//        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
//        paramMap.put("code","7d6929fa993a341579776afd9067760c");
//
//        String s3 = "https://api.weibo.com/oauth2/access_token?client_id=3400407400&client_secret=e18f3af3e838d675c746fc95c5f3da83&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=01c3624bd527cd51e9899d63e6d29c4b";
//        String doPost = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", paramMap);
//        Map<String,String> map = JSON.parseObject(doPost, Map.class);
//        System.out.println(map.get("uid"));
//        System.out.println(map.get("access_token"));

        String access_token = "2.00irENTDeSlHiD29df786684HeeJcC";
        String s4 = "https://api.weibo.com/2/users/show.json?access_token="+access_token+"&uid=1";
        String s = HttpclientUtil.doGet(s4);
        Map map1 = JSON.parseObject(s, Map.class);
        System.out.println(map1);


    }

    public static String getCode(){
        //获取授权码
        String url = "https://api.weibo.com/oauth2/authorize?client_id=3400407400&response_type=code&redirect_uri=http://passport.gmall.com:8085/vlogin";
        String s = HttpclientUtil.doGet(url);
        return s;
    }

    public String getAccessToken(String code){
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","3400407400");
        paramMap.put("client_secret","e18f3af3e838d675c746fc95c5f3da83");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);

        String s3 = "https://api.weibo.com/oauth2/access_token?client_id=3400407400&client_secret=e18f3af3e838d675c746fc95c5f3da83&grant_type=authorization_code&redirect_uri=http://passport.gmall.com:8085/vlogin&code=01c3624bd527cd51e9899d63e6d29c4b";
        String doPost = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", paramMap);
        Map<String,String> map = JSON.parseObject(doPost, Map.class);
        System.out.println(map.get("uid"));
        System.out.println(map.get("access_token"));

        return map.get("access_token");
    }

    public static Map<String,String> getUserInfo(String accessToken){
        String s4 = "https://api.weibo.com/2/users/show.json?access_token="+accessToken+"&uid=1";
        String s = HttpclientUtil.doGet(s4);
        Map map1 = JSON.parseObject(s, Map.class);
        System.out.println(map1);
        return map1;
    }
}
