package com.fukun.order.config.bean;

import com.fukun.commons.web.decoder.FeignErrorDecoder;
import com.fukun.commons.web.handler.GlobalExceptionHandler;
import com.fukun.commons.web.handler.ResponseResultHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * bean相关的配置类
 *
 * @author tangyifei
 * @since 2019-5-24 11:54:20
 */
@Configuration
public class BeanConfig {

    /**
     * 注册全局异常处理器到spring容器中
     *
     * @return 全局异常处理器相关的bean
     */
    @Bean
    public GlobalExceptionHandler globalExceptionHandler() {
        return new GlobalExceptionHandler();
    }

    /**
     * 注册feign错误解码器器到spring容器中
     *
     * @return feign错误解码器器相关的bean
     */
    @Bean
    public FeignErrorDecoder feignErrorDecoder() {
        return new FeignErrorDecoder();
    }

    /**
     * 注册接口响应体处理器到spring容器中
     *
     * @return 接口响应体处理器相关的bean
     */
    @Bean
    public ResponseResultHandler responseResultHandler() {
        return new ResponseResultHandler();
    }

}
