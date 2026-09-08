package com.back.nbe12142team06.global.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class RsData<T> {
    private String statusCode;
    private String msg;
    private T data;

    public int getStatusCode() {
        return Integer.parseInt(this.statusCode.split("-")[0]);
    }
}
