package com.qian.qianapigateway;


import com.qian.qiancommon.model.entity.InterfaceInfo;
import com.qian.qiancommon.model.entity.User;
import com.qian.qiancommon.service.InnerInterfaceInfoService;
import com.qian.qiancommon.service.InnerUserInterfaceInfoService;
import com.qian.qiancommon.service.InnerUserService;
import com.qianapi.qianapiclientsdk.utils.SignUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.dubbo.config.annotation.DubboReference;
import org.reactivestreams.Publisher;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.core.io.buffer.DataBufferFactory;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 全局过滤
 */

@Slf4j
@Component
public class CustomGlobalFilter implements GlobalFilter, Ordered {

    @DubboReference
    private InnerUserService innerUserService;

    @DubboReference
    private InnerInterfaceInfoService innerInterfaceInfoService;

    @DubboReference
    private InnerUserInterfaceInfoService innerUserInterfaceInfoService;


    public static final List<String> IP_WHITE_LIST = Arrays.asList("127.0.0.1");

    public static final String INERTFACE_HOST = "http://localhost:8123";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 1. 请求日志
        ServerHttpRequest request = exchange.getRequest();
        String path = INERTFACE_HOST + request.getPath().value();
        String method = request.getMethod().toString();
        log.info("请求唯一标识：" + request.getId());
        log.info("请求路径：" + path);
        log.info("请求方法：" + method);
        log.info("请求参数：" + request.getQueryParams());
        String sourceIp = request.getRemoteAddress().getHostString();
        log.info("请求来源地址：" + request.getRemoteAddress());
        log.info("请求来源地址：" + request.getLocalAddress().getHostString());

        ServerHttpResponse serverHttpResponse = exchange.getResponse();
        // 2. 访问控制 - 黑白名单
        if(!IP_WHITE_LIST.contains(sourceIp)){
            serverHttpResponse.setStatusCode(HttpStatus.FORBIDDEN);
            return serverHttpResponse.setComplete();
        }
        log.info("IP在白名单中，放行");

        // 3. API网关鉴权（ak sk）
        HttpHeaders headers = request.getHeaders();
        String accessKey = headers.getFirst("accessKey");
        String body = headers.getFirst("body");
        String timestamp = headers.getFirst("timestamp");
        String nonce = headers.getFirst("nonce");
        String sign = headers.getFirst("sign");


        //获取的时间戳 - 当前时间戳 如果大五分钟 表示超时
        final Long FIVE_MINIUTES = 5 * 60 * 1000L;
        if(Math.abs(Long.parseLong(timestamp) - System.currentTimeMillis()) > FIVE_MINIUTES){
            return handleNoAuth(serverHttpResponse);
        }
        if(Long.parseLong(nonce)>10000){
            return handleNoAuth(serverHttpResponse);
        }

        /**
         * 实际情况是去数据库中查是否已分配给用户
         */
        User invokeUser = null;
        try {
            invokeUser = innerUserService.getInvokeUser(accessKey);
        }catch (Exception e){
            log.error("获取invoke用户信息失败", e);
        }
        if(invokeUser == null){
            // 如果用户信息为空， 处理未授权情况返回响应
            return handleNoAuth(serverHttpResponse);
        }

        // 获取sk
        String serectKety = invokeUser.getSecretKey();
        // 检查sk签名 是否和请求中的签名是否一致
        if(!sign.equals(SignUtils.genSign(body, serectKety)) || sign == null){
            return handleNoAuth(serverHttpResponse);
        }


        // 4. 判断请求的接口是否存在
        /**
         * 从数据库查询模拟接口是否存在
         */
        InterfaceInfo interfaceInfo = null;
        try {
            interfaceInfo = innerInterfaceInfoService.getInterfaceInfo(path, method);
        }catch (Exception e){
            log.error("获取接口信息失败", e);
        }
        if(interfaceInfo == null){
            // 如果接口信息为空， 处理未授权情况返回响应
            return handleNoAuth(serverHttpResponse);
        }



        // 5. 请求转发，调用模拟接口
        Mono<Void> filter = chain.filter(exchange);

        // 6. 响应日志
        return handleResponse(exchange, chain, interfaceInfo.getId(), invokeUser.getId());

    }


    /**
     * 处理响应
     *
     * @param exchange
     * @param chain
     * @return
     */
    public Mono<Void> handleResponse(ServerWebExchange exchange, GatewayFilterChain chain, long interfaceId, long userId) {
        try {
            // 获取原始的响应对象
            ServerHttpResponse originalResponse = exchange.getResponse();
            // 获取数据缓冲工厂
            DataBufferFactory bufferFactory = originalResponse.bufferFactory();
            // 获取响应的状态码
            HttpStatus statusCode = originalResponse.getStatusCode();

            // 判断状态码是否为200 OK(按道理来说,现在没有调用,是拿不到响应码的,对这个保持怀疑 沉思.jpg)
            if(statusCode == HttpStatus.OK) {
                // 创建一个装饰后的响应对象(开始穿装备，增强能力)
                ServerHttpResponseDecorator decoratedResponse = new ServerHttpResponseDecorator(originalResponse) {

                    // 重写writeWith方法，用于处理响应体的数据
                    // 这段方法就是只要当我们的模拟接口调用完成之后,等它返回结果，
                    // 就会调用writeWith方法,我们就能根据响应结果做一些自己的处理
                    @Override
                    public Mono<Void> writeWith(Publisher<? extends DataBuffer> body) {
                        log.info("body instanceof Flux: {}", (body instanceof Flux));
                        // 判断响应体是否是Flux类型
                        if (body instanceof Flux) {
                            Flux<? extends DataBuffer> fluxBody = Flux.from(body);
                            // 返回一个处理后的响应体
                            // (这里就理解为它在拼接字符串,它把缓冲区的数据取出来，一点一点拼接好)
                            return super.writeWith(
                                    fluxBody.map(dataBuffer -> {
                                // 读取响应体的内容并转换为字节数组
                                /**
                                 * 调用成功 接口调用次数+1 invokecount
                                 */
                                try {
                                    innerUserInterfaceInfoService.invokeCount(userId, interfaceId);
                                }catch (Exception e){
                                    log.error("invokeCount error", e);
                                }


                                byte[] content = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(content);
                                DataBufferUtils.release(dataBuffer);//释放掉内存
                                // 构建日志
                                StringBuilder sb2 = new StringBuilder(200);
                                sb2.append("<--- {} {} \n");
                                List<Object> rspArgs = new ArrayList<>();
                                rspArgs.add(originalResponse.getStatusCode());
                                //rspArgs.add(requestUrl);
                                String data = new String(content, StandardCharsets.UTF_8);//data
                                sb2.append(data);
                                log.info(sb2.toString(), rspArgs.toArray());//log.info("<-- {} {}\n", originalResponse.getStatusCode(), data);
                                // 将处理后的内容重新包装成DataBuffer并返回
                                return bufferFactory.wrap(content);
                            }));
                        } else {
                            log.error("<--- {} 响应code异常", getStatusCode());
                        }
                        return super.writeWith(body);
                    }
                };
                // 对于200 OK的请求,将装饰后的响应对象传递给下一个过滤器链,并继续处理(设置repsonse对象为装饰过的)
                return chain.filter(exchange.mutate().response(decoratedResponse).build());
            }
            // 对于非200 OK的请求，直接返回，进行降级处理
            return chain.filter(exchange);
        }catch (Exception e){
            // 处理异常情况，记录错误日志
            log.error("gateway log exception.\n" + e);
            return chain.filter(exchange);
        }
    }


    @Override
    public int getOrder() {
        return -1;
    }

    public Mono<Void> handleNoAuth(ServerHttpResponse response) {
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        return response.setComplete();
    }
}
