package com.hur.gmall.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.service.UserService;
import com.hur.gmall.util.HttpclientUtil;
import com.hur.gmall.util.JwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.thymeleaf.standard.expression.GreaterOrEqualToExpression;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {

    @Reference
    UserService userService;

    @RequestMapping("vlogin")
    public String vLogin(HttpServletRequest request,String code ){
        //授权码换取access token
        Map<String,String> paramMap = new HashMap<>();
        paramMap.put("client_id","3400407400");
        paramMap.put("client_secret","e18f3af3e838d675c746fc95c5f3da83");
        paramMap.put("grant_type","authorization_code");
        paramMap.put("redirect_uri","http://passport.gmall.com:8085/vlogin");
        paramMap.put("code",code);

        String doPost = HttpclientUtil.doPost("https://api.weibo.com/oauth2/access_token", paramMap);
        Map<String,Object> map = JSON.parseObject(doPost, Map.class);
        System.out.println(map.get("uid"));
        System.out.println(map.get("access_token"));

        //access token换取微博用户的信息
        String accessToken = (String) map.get("access_token");
        String uid = (String) map.get("uid");
        String showUserUrl = "https://api.weibo.com/2/users/show.json?access_token="+accessToken+"&uid="+uid;
        String userJson = HttpclientUtil.doGet(showUserUrl);
        Map<String,Object> userMap = JSON.parseObject(userJson, Map.class);

        //将用户信息写入数据库 用户类型设置为微博用户
        UmsMember userMember = new UmsMember();
        userMember.setSourceType("2");
        userMember.setAccessCode(code);
        userMember.setAccessToken(accessToken);
        userMember.setSourceUid((String) userMap.get("idstr"));
        userMember.setCity((String) userMap.get("location"));
        userMember.setNickname((String) userMap.get("screen_name"));
        String gender = (String) userMap.get("gender");
        String g = "0";
        if (gender.equals("m")){
            g = "1";
        }
        userMember.setGender(g);

        UmsMember memberCheck = userService.checkMember(userMember.getSourceUid());

        if (memberCheck==null){
            userMember = userService.addOauthUser(userMember);
        }else {
            userMember = memberCheck;
        }
        //生成JWT的token 重定向回之前页面并且携带该token

        String token = setToken(userMember, request);

        return "redirect:http://search.gmall.com:8083/index?token="+token;
    }

    @ResponseBody
    @RequestMapping("verify")
    public String verify(String token, String currentIp){

        Map<String,String> map = new HashMap<>();
        //通过JWT验证token真假
        System.out.println(token);
        System.out.println(currentIp);
        Map<String, Object> decode = JwtUtil.decode(token, "2020gmall0105", currentIp);
        String s= "1";
        if (decode!=null){
            map.put("memberId", (String) decode.get("memberId"));
            map.put("nickname", (String) decode.get("nickname"));
            map.put("status","success");
        }else {
            map.put("status","fail");
        }

        return JSON.toJSONString(map);
    }

    @ResponseBody
    @RequestMapping("login")
    public String login(UmsMember umsMember,HttpServletRequest request){
        String token = "";
        //调用用户服务  验证用户名和密码
        UmsMember umsMemberLogin = userService.login(umsMember);
        if (umsMemberLogin!=null){
            //登录成功
            //用JWT制作token
            token = setToken(umsMemberLogin,request);

            return token;
        }else {
            //失败
            return "false";
        }
    }

    @RequestMapping("index")
    public String index(String ReturnUrl, ModelMap modelMap){

        if (StringUtils.isNotBlank(ReturnUrl)){
            modelMap.put("ReturnUrl",ReturnUrl);
        }

        return "index";
    }

    public String setToken(UmsMember umsMember,HttpServletRequest request){
        //用JWT制作token
        String memberId = umsMember.getId();
        String nickname = umsMember.getNickname();
        Map<String,Object> userMap = new HashMap<>();
        userMap.put("memberId",memberId);
        userMap.put("nickname",nickname);

        String ip = request.getHeader("x-forwarded-for");//获取通过nginx转发的客户端IP
        if(StringUtils.isBlank(ip)){
            ip = request.getRemoteAddr();//从request中获取ip
            if (StringUtils.isBlank(ip)){
                ip = "127.0.0.1";
            }
        }

        //按照设计的算法对参数进行加密后生成token
        String token = JwtUtil.encode("2020gmall0105",userMap,ip);
        //将token存入redis一份
        userService.addTokenToCache(token,memberId);
        return token;
    }
}
