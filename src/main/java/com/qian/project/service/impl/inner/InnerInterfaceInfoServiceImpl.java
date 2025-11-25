package com.qian.project.service.impl.inner;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.qian.project.common.ErrorCode;
import com.qian.project.exception.BusinessException;
import com.qian.project.mapper.InterfaceInfoMapper;
import com.qian.project.mapper.UserMapper;
import com.qian.qiancommon.model.entity.InterfaceInfo;

import com.qian.qiancommon.service.InnerInterfaceInfoService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.DubboService;

import javax.annotation.Resource;
@DubboService
public class InnerInterfaceInfoServiceImpl implements InnerInterfaceInfoService {

    @Resource
    private InterfaceInfoMapper interfaceInfoMapper;

    /**
     * 从数据库查询模拟接口是否存在
     * @param path
     * @param method
     * @return
     */
    @Override
    public InterfaceInfo getInterfaceInfo(String path, String method) {
        // 参数校验
        if(StringUtils.isAnyBlank(path) || StringUtils.isAnyBlank(method)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<InterfaceInfo> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("url", path)
                    .eq("method", method);
        InterfaceInfo interfaceInfo = interfaceInfoMapper.selectOne(queryWrapper);

        return interfaceInfo;
    }
}
