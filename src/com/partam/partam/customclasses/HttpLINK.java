package com.partam.partam.customclasses;

import org.apache.http.client.methods.HttpGet;

public class HttpLINK extends HttpGet
{
    public static final String METHOD_LINK = "LINK";

    public HttpLINK(final String url) {
        super(url);
    }

    @Override
    public String getMethod() {
        return METHOD_LINK;
    }
}