package com.hur.gmall.service;

import com.hur.gmall.bean.UmsMember;
import com.hur.gmall.bean.UmsMemberReceiveAddress;

import java.util.List;

public interface UserService {
    List<UmsMember> getAllUsers();

    List<UmsMemberReceiveAddress> getReceiveAddressByMemberId(String memberId);

    UmsMember login(UmsMember umsMember);

    void addTokenToCache(String token, String memberId);

    UmsMember addOauthUser(UmsMember userMember);

    UmsMember checkMember(String uid);

    UmsMemberReceiveAddress getReceiveAddressById(String deliveryAddressId);
}
