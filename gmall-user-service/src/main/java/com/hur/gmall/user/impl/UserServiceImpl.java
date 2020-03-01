package com.hur.gmall.user.impl;


import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.bean.UmsMemberReceiveAddress;
import com.hur.gmall.service.UserService;
import com.hur.gmall.user.mapper.UmsMemberReceiveAddressMapper;
import com.hur.gmall.user.mapper.UserMapper;
import com.hur.gmall.util.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Service;

//import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    UserMapper userMapper;
    @Autowired
    UmsMemberReceiveAddressMapper umsMemberReceiveAddressMapper;
    @Autowired
    RedisUtil redisUtil;

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

    @Override
    public UmsMember login(UmsMember umsMember) {
        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();

            if (jedis!=null){
                String umsMemberInfo = jedis.get("user:" + umsMember.getUsername()+umsMember.getPassword() + ":info");

                if (umsMemberInfo!=null){
                    UmsMember member = JSON.parseObject(umsMemberInfo, UmsMember.class);
                    if (member.getPassword().equals(umsMember.getPassword())){
                        return member;
                    }
                }else {
                    //redis中没有
                    //密码错误或者还没存入redis
                    //在数据库中查找
                    UmsMember umsMemberFromDB = getUserFormDB(umsMember);
                    if (umsMemberFromDB!=null){
                        //存入cache
                        jedis.setex("user:" + umsMember.getId() + ":Info",60*60*24,JSON.toJSONString(umsMemberFromDB));
                        return umsMemberFromDB;
                    }
                }
            }else {
                //连接redis失败 开启数据库
                UmsMember umsMemberFromDB = getUserFormDB(umsMember);
                if (umsMemberFromDB!=null){
                    //存入cache
                    jedis.setex("user:" + umsMember.getId() + ":Info",60*60*24,JSON.toJSONString(umsMemberFromDB));
                    return umsMemberFromDB;
                }
            }

        }finally {
            jedis.close();
        }

        return null;
    }

    @Override
    public void addTokenToCache(String token, String memberId) {
        Jedis jedis = null;
        try {
            jedis = redisUtil.getJedis();
            jedis.setex("user:"+memberId+"token",60*60*2,token);
        }finally {
            jedis.close();
        }
    }

    @Override
    public UmsMember addOauthUser(UmsMember userMember) {
        int i = userMapper.insertSelective(userMember);
        return userMember;
    }

    @Override
    public UmsMember checkMember(String uid) {
        UmsMember memberCheck = new UmsMember();
        memberCheck.setSourceUid(uid);
        UmsMember member = userMapper.selectOne(memberCheck);
        return member;
    }

    @Override
    public UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId) {
        UmsMemberReceiveAddress umsMemberReceiveAddress = new UmsMemberReceiveAddress();
        umsMemberReceiveAddress.setId(deliveryAddressId);
        UmsMemberReceiveAddress umsMemberReceiveAddress1 = umsMemberReceiveAddressMapper.selectOne(umsMemberReceiveAddress);

        return umsMemberReceiveAddress1;
    }

    private UmsMember getUserFormDB(UmsMember umsMember) {

        UmsMember memberFromDB = userMapper.selectOne(umsMember);

        return memberFromDB;
    }
}
