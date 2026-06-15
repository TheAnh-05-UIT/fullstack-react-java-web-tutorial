package com.web_tutorial.javabackend.util;

import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.lang.Nullable;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.web_tutorial.javabackend.domain.dto.response.RestResponse;
import com.web_tutorial.javabackend.util.annotation.ApiMessage;

import jakarta.servlet.http.HttpServletResponse;

// Format tất cả response API trong hệ thống
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    /**
     * Trả về true nghĩa là Áp dụng cho các API trong hệ thống.
     */
    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    /**
     * Hàm này được Spring Boot gọi trc khi gửi dữ liệu JSON về cho Frontend.
     */
    @SuppressWarnings("null")
    @Override
    public Object beforeBodyWrite(
            @Nullable Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {

        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int statusCode = httpServletResponse.getStatus();

        // Tạo sẵn một lớp vỏ RestResponse
        RestResponse<Object> res = new RestResponse<>();
        res.setStatusCode(statusCode);

        // Nếu Controller trả về String thì không bọc lại tránh lỗi casting của Spring
        if (body instanceof String) {
            return body;
        }

        // Dữ liệu này đã được GlobalExceptionHandler xử lý
        if (statusCode >= 400) {
            return body;
        }
        // Gọi API Thành công
        else {
            res.setData(body);

            // Dùng Reflection đọc hàm Controller có gắn annotation @ApiMessage
            ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);

            res.setMessage(apiMessage != null ? apiMessage.value() : "Success");
        }

        // Trả về dữ liệu đã được format
        return res;
    }
}
