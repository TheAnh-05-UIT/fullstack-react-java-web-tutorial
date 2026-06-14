package com.web_tutorial.javabackend.domain.dto.response;

// Chuẩn hóa dữ liệu trả về
public class RestResponse<T> {

    // Mã trạng thái
    private int statusCode;

    // Chi tiết lỗi
    private String error;

    // Thông báo
    private Object message;

    // Dữ liệu trả về
    private T data;

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public Object getMessage() {
        return message;
    }

    public void setMessage(Object message) {
        this.message = message;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

}
