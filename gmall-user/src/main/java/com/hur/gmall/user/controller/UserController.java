package com.hur.gmall.user.controller;

import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.bean.UmsMemberReceiveAddress;
import com.hur.gmall.service.UserService;
import com.hur.gmall.user.service.impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.sound.midi.Receiver;
import java.util.List;

@RestController
@CrossOrigin
public class UserController {
    @Autowired
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
