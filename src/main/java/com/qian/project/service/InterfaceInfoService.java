package com.qian.project.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.qian.qiancommon.model.entity.InterfaceInfo;

/**
* @author wangyixuan
* @description 针对表【interface_info(接口信息)】的数据库操作Service
* @createDate 2024-10-01 17:00:36
*/
public interface InterfaceInfoService extends IService<InterfaceInfo> {
    void validInterfaceInfo(InterfaceInfo post, boolean add);

}
