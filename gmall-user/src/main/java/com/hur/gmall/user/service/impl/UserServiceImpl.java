package com.hur.gmall.user.service.impl;

import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.bean.UmsMemberReceiveAddress;
import com.hur.gmall.service.UserService;


import com.hur.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.hur.gmall.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;

    @Override
    public List<UmsMember> getAllUsers() {
        List<UmsMember> users = userMapper.selectAll();
        return users;
    }

    @Override
    public List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId) {

        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setMemberId(memberId);


        Example example = new Example(UmsMemberReceiveAddress.class);
        example.createCriteria().andEqualTo("memberId",memberId);


        List<UmsMemberReceiveAddress> umsMemberReceiveAddresses =
                umsMemberReceiveAddressMapper.select(umsMemberReceiveAddress);

        return umsMemberReceiveAddresses;
    }
}
