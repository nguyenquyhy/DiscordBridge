package com.nguyenquyhy.spongediscord.discord.util;

/**
 * Created by nguye on 1/11/2016.
 */
public class HttpException extends Exception {
    public final String url;
    public final int responseCode;
    public final String content;

    public HttpException(String cause, String url, int responseCode, String content) {
        super(cause);
        this.url = url;
        this.responseCode = responseCode;
        this.content = content;
    }

    public HttpException(String cause, String url, int responseCode) {
        this (cause, url, responseCode, null);
    }
}
