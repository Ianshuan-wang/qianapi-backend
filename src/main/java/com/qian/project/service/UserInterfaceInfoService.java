package com.qian.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qian.qiancommon.model.entity.UserInterfaceInfo;

/**
* @author wangyixuan
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service
* @createDate 2025-04-21 22:21:44
*/
public interface UserInterfaceInfoService extends IService<UserInterfaceInfo> {
    void validUserInterfaceInfo(UserInterfaceInfo post, boolean add);

    boolean invokeCount(long userId, long interfaceInfoId);



}
