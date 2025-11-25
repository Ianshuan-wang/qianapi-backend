package com.qian.project.model.vo;

import com.qian.project.model.entity.Post;
import com.qian.qiancommon.model.entity.InterfaceInfo;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 接口信息视图
 *
 * @author WYX
 * @TableName product
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class InterfaceInfoVO extends InterfaceInfo {

    /**
     * 调用总数
     */
    private Integer totalNum;

    private static final long serialVersionUID = 1L;
}