package com.github.sparsick.testcontainers.gitserver.http;

import java.util.Objects;

public class HttpProxySetting {

    private String httpProxy;
    private String httpsProxy;
    private String noProxy;

    public HttpProxySetting(String httpProxy, String httpsProxy, String noProxy) {
        this.httpProxy = httpProxy;
        this.httpsProxy = httpsProxy;
        this.noProxy = noProxy;
    }

    public String getHttpProxy() {
        return httpProxy;
    }

    public String getHttpsProxy() {
        return httpsProxy;
    }

    public String getNoProxy() {
        return noProxy;
    }
}
