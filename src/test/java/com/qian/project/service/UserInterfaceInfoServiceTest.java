package com.qian.project.service;


import com.qian.qiancommon.service.InnerUserInterfaceInfoService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.index.PathBasedRedisIndexDefinition;

import javax.annotation.Resource;

@SpringBootTest
public class UserInterfaceInfoServiceTest {

    @Resource
    private UserInterfaceInfoService userInterfaceInfoService;


    @Resource
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;

    @Test
    public void invokeCount() {
        boolean b = innerUserInterfaceInfoService.invokeCount(4L, 1L);
        Assertions.assertTrue(b);
    }
}