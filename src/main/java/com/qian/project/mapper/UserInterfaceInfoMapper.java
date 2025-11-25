package com.qian.project.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.qian.qiancommon.model.entity.UserInterfaceInfo;
import java.util.List;
/**
* @author wangyixuan
* @description 针对表【user_interface_info(用户调用接口关系)】的数据库操作Mapper
* @createDate 2025-04-21 22:21:44
* @Entity com.qian.project.model.entity.UserInterfaceInfo
*/
public interface UserInterfaceInfoMapper extends BaseMapper<UserInterfaceInfo> {


    //select interfaceInfoId, sum(totalNum) as totalNum
    //from user_interface_info
    //group by interfaceInfoId
    //order by totalNum desc
    //limit 3;

    List<UserInterfaceInfo> listTopInvokeInterfaceInfo(int limit);

}




