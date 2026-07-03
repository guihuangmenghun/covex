package com.covex.common.result;

import lombok.Data;

import java.io.Serializable;

/**
 * 统一 API 响应
 */
@Data
public class Result<T> implements Serializable {

    private int code;
    private String message;
    private T data;
    private String traceId;

    private Result() {}

    public static <T> Result<T> ok() {
        return ok(null);
    }

    public static <T> Result<T> ok(T data) {
        Result<T> r = new Result<>();
        r.setCode(200);
        r.setMessage("success");
        r.setData(data);
        return r;
    }

    public static <T> Result<T> fail(int code, String message) {
        Result<T> r = new Result<>();
        r.setCode(code);
        r.setMessage(message);
        return r;
    }

    public static <T> Result<T> fail(String message) {
        return fail(500, message);
    }
}
