package com.uyscuti.social.medialoader.tinyhttpd.request;

import com.uyscuti.social.medialoader.tinyhttpd.HttpHeaders;
import com.uyscuti.social.medialoader.tinyhttpd.HttpVersion;

/**
 * 请求接口
 *
 * @author vincanyang
 */
public interface Request {

    HttpMethod method();

    String url();

    HttpVersion protocol();

    HttpHeaders headers();

    String getParam(String name);
}
