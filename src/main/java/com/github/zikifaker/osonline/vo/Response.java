package com.github.zikifaker.osonline.vo;

import java.io.Serializable;

/**
 * 后端返回的响应
 *
 * @param <T> 返回数据类型
 */
public class Response<T> implements Serializable {

    /**
     * 编码
     * 1为成功，其它数字为失败
     */
    private Integer code; //

    /**
     * 错误信息
     */
    private String msg;

    /**
     * 数据
     */
    private T data;

    public static <T> Response<T> success() {
        Response<T> Response = new Response<T>();
        Response.code = 1;
        return Response;
    }

    public static <T> Response<T> success(T object) {
        Response<T> Response = new Response<T>();
        Response.code = 1;
        Response.data = object;
        return Response;
    }

    public static <T> Response<T> error(String msg) {
        Response<T> Response = new Response<>();
        Response.msg = msg;
        Response.code = 0;
        return Response;
    }

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }
}
