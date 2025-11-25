package com.qian.project.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qian.project.common.ErrorCode;
import com.qian.project.exception.BusinessException;
import com.qian.project.mapper.UserMapper;
import com.qian.project.service.UserInterfaceInfoService;
import com.qian.project.mapper.UserInterfaceInfoMapper;
import com.qian.qiancommon.model.entity.InterfaceInfo;
import com.qian.qiancommon.model.entity.User;
import com.qian.qiancommon.model.entity.UserInterfaceInfo;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

/**
* @author wangyixuan
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Service实现
* @createDate 2025-04-21 22:21:44
*/
@Service
public class UserInterfaceInfoServiceImpl extends ServiceImpl<UserInterfaceInfoMapper, UserInterfaceInfo>
    implements UserInterfaceInfoService{

    @Override
    public void validUserInterfaceInfo(UserInterfaceInfo userInterfaceInfo, boolean add) {

        // 判断接口信息对象是否为空,为空则抛出参数错误的异常
        if (userInterfaceInfo == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取接口信息对象的名称
        if (add) {
            if (userInterfaceInfo.getInterfaceInfoId() <= 0 || userInterfaceInfo.getUserId() <= 0) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
            }
        }

        if (userInterfaceInfo.getLeftNum() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "剩余次数不能小于0");
        }
    }

    @Override
    public boolean invokeCount(long userId, long interfaceInfoId){
        if (userId <= 0 || interfaceInfoId <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "接口或用户不存在");
        }
        UpdateWrapper<UserInterfaceInfo> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.eq("interfaceInfoId", interfaceInfoId);
        // 保证剩余次数大于0
        updateWrapper.gt("leftNum", 0);
        updateWrapper.setSql("leftNum = leftNum - 1, totalNum = totalNum + 1");
        boolean result = this.update(updateWrapper);


//        if (userInterfaceInfo == null) {
//            // 之前没有调用过
//            userInterfaceInfo = new UserInterfaceInfo();
//            userInterfaceInfo.setUserId(userId);
//            userInterfaceInfo.setInterfaceInfoId(interfaceInfoId);
//            userInterfaceInfo.setTotalNum(1);
//            userInterfaceInfoMapper.insert(userInterfaceInfo);
//        }
//        else {
//            // 之前调用过
//            userInterfaceInfo.setTotalNum(userInterfaceInfo.getTotalNum() + 1);
//            userInterfaceInfoMapper.updateById(userInterfaceInfo);
//        }
        return result;
    }
}




