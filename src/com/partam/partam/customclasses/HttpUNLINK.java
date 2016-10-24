package com.partam.partam.customclasses;

import org.apache.http.client.methods.HttpGet;

public class HttpUNLINK extends HttpGet 
{
    public static final String METHOD_UNLINK = "UNLINK";

    public HttpUNLINK(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_UNLINK;
    }
}