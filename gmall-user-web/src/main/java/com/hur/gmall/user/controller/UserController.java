package com.hur.gmall.user.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.bean.UmsMemberReceiveAddress;
import com.hur.gmall.service.UserService;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@CrossOrigin
public class UserController {

    @Reference
    UserService userService;

//    @RequestMapping("getAllUser")
    @GetMapping("getAllUser")
    public List<UmsMember> getAllUsers(){

        List<UmsMember> users=userService.getAllUsers();

        return users;
    }

    //    @RequestMapping("getAllUser")
    @GetMapping("getReceiveAddressByMemberId")
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId){

        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses=userService.getReceiveAddressByMemberId(memberId);

        return umsMemberReceiveAddresses;
    }

}
