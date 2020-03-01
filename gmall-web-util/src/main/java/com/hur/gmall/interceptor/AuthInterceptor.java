package com.hur.gmall.interceptor;

import com.alibaba.fastjson.JSON;
import com.hur.gmall.annotations.LoginRequired;
import com.hur.gmall.util.CookieUtil;
import com.hur.gmall.util.HttpclientUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.thymeleaf.context.WebContext;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.annotation.Annotation;
import java.util.HashMap;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter{


        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//            String newToken = request.getParameter("newToken");
//            if(newToken!=null&&newToken.length()>0){
//                CookieUtil.setCookie(request,response,"token",newToken,WebConst.cookieExpire,false);
//            }

            //拦截代码

            //判断被拦截请求的访问方法的注解（是否需要被拦截）
            HandlerMethod hm = (HandlerMethod)handler;
            LoginRequired methodAnnotation = hm.getMethodAnnotation(LoginRequired.class);

            if (methodAnnotation==null){
                return true;
            }

            String token = "";
            String oldToken = CookieUtil.getCookieValue(request,"oldToken",true);
            if (StringUtils.isNoneBlank(oldToken)){
                token = oldToken;
            }

            //getParameter 是用来接受用post或get方法传递过来的参数的.
            //getAttribute 必须先setAttribute.
            String newToken = request.getParameter("token");
            if (StringUtils.isNotBlank(newToken)){
                token = newToken;
            }

            //获得该请求是否必须登陆成功
            boolean loginSuccess = methodAnnotation.loginSuccess();

            //调用认证中心进行认证
            String success = "fail";
            Map<String,String> successMap = new HashMap<>();
            if (StringUtils.isNotBlank(token)){
                //调用验证中心进行验证
                String ip = request.getHeader("x-forwarded-for");//获取通过nginx转发的客户端IP
                if(StringUtils.isBlank(ip)){
                    ip = request.getRemoteAddr();//从request中获取ip
                    if (StringUtils.isBlank(ip)){
                        ip = "127.0.0.1";
                    }
                }
                String successJson = HttpclientUtil.doGet("http://passport.gmall.com:8085/verify?token=" + token+"&currentIp="+ip);

                successMap = JSON.parseObject(successJson,Map.class);
                success = successMap.get("status");
            }

            if (loginSuccess){

                //必须登录成功才能使用
                if(!success.equals("success")){
                    //重定向回passport登录
                    StringBuffer requestURL = request.getRequestURL();
                    response.sendRedirect("http://passport.gmall.com:8085/index?ReturnUrl="+requestURL);
                    return false;
                }else {
                    request.setAttribute("memberId",successMap.get("memberId"));
                    request.setAttribute("nickname",successMap.get("nickname"));

                    if (StringUtils.isNotBlank(token)){
                        //验证通过 覆盖cookie中的token
                        CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);

                    }
                }

            }else{
                //不登陆也能使用
                if(success.equals("success")){
                    //需要将token携带的用户信息进行写入
                    request.setAttribute("memberId",successMap.get("memberId"));
                    request.setAttribute("nickname",successMap.get("nickname"));

                    if (StringUtils.isNotBlank(token)){
                        //验证通过 覆盖cookie中的token
                        CookieUtil.setCookie(request,response,"oldToken",token,60*60*2,true);

                    }
                }
            }


            return true;
        }
}
