package com.qianapi.qianapiclientsdk.client;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.qianapi.qianapiclientsdk.model.User;
import com.qianapi.qianapiclientsdk.utils.SignUtils;

import java.util.HashMap;

import static com.qianapi.qianapiclientsdk.utils.SignUtils.genSign;


public class QianapiClient {

    public static final String GATEWAY_HOST = "http://localhost:8090";
    private String accessKey;

    private String secretKey;

    public QianapiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public String getName(String name){
        // 最简单的HTTP请求，可以自动通过header等信息判断编码，不区分HTTP和HTTPS
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result1= HttpUtil.get(GATEWAY_HOST+"/api/name/", paramMap);
        System.out.println(result1);
        return result1;

    }

    public String getNamePost(String name){
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);

        String result= HttpUtil.post(GATEWAY_HOST+"/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public HashMap<String, String> getHeaderMap(String body){
        HashMap<String, String> map = new HashMap<>();
        map.put("accessKey",accessKey);

//        map.put("secretKey",secretKey); 一定不能明文发送给后端
        map.put("body",body);
        map.put("nonce", RandomUtil.randomNumbers(4));
        map.put("timestamp ",String.valueOf(System.currentTimeMillis()));
        map.put("sign", SignUtils.genSign(body,secretKey));

        return map;
    }

    public String getUserName(User user){
        String json = JSONUtil.toJsonStr(user);
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST+"/api/name/user")
                .addHeaders(getHeaderMap(json))
                .body(json)
                .execute();
        String result = httpResponse.body();
        System.out.println(result);
        return result;
    }
}
