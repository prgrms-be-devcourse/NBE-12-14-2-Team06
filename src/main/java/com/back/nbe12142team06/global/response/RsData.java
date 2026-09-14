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
        // - 없이 사용하는 경우 대비
        int idx = statusCode.indexOf('-');
        return Integer.parseInt(idx == -1 ? statusCode : this.statusCode.substring(0, idx));
    }
}
