package com.qian.qianapiinterface.controller;

import cn.hutool.crypto.SignUtil;
import cn.hutool.http.HttpRequest;
import com.qianapi.qianapiclientsdk.model.User;
import com.qianapi.qianapiclientsdk.utils.SignUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping("/name")
public class NameController {

    @GetMapping("/get")
    public String getName(String name, HttpServletRequest request){
        System.out.println(request);
        return "名字为"+name;
    }

    @PostMapping("/post")
    public String getNameByPost(@RequestParam String name) {
        return "POST 你的名字是" + name;
    }

    @PostMapping("/user")
    public String getUserName(@RequestBody User user, HttpServletRequest request){
        String access = request.getHeader("accessKey");
        String body = request.getHeader("body");

        String timestamp = request.getHeader("timestamp");
        String nonce = request.getHeader("nonce");
        String sign = request.getHeader("sign");

        // TODO 实际情况是去数据库中查是否已分配给用户
        if(!access.equals("access")){
            throw new RuntimeException("无权限");
        }
        //获取的时间戳 - 当前时间戳 如果大五分钟 表示超时
        if(Math.abs(Long.parseLong(timestamp) - System.currentTimeMillis()) > 5 * 60 * 1000){
            throw new RuntimeException("无权限");
        }
        if(Long.parseLong(nonce)>10000){
            throw new RuntimeException("无权限");
        }
        // TODO 客户端的 secretKey 就是服务端签发的 实际应当去数据库中查找accessKey对应的
        String serverSign = SignUtils.genSign(body, "asdfghjkl");
        if(!sign.equals(serverSign)){
            throw new RuntimeException("无权限");
        }


        return "传参名字为" + user.getUsername();
    }

}