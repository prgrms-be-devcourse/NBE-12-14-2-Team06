package com.back.nbe12142team06.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RsData<T> {
    private String statusCode;
    private String msg;
    private T data;

    public RsData(String statusCode, String msg) {
        this.statusCode = statusCode;
        this.msg = msg;
    }

    public int simpleStatusCode() {
        return Integer.parseInt(this.statusCode.substring(0, statusCode.indexOf("-")));
    }
}
