package com.uyscuti.social.medialoader.manager;

import com.uyscuti.social.medialoader.tinyhttpd.request.Request;
import com.uyscuti.social.medialoader.tinyhttpd.response.Response;
import com.uyscuti.social.medialoader.tinyhttpd.response.ResponseException;

import java.io.IOException;

/**
 * Media业务接口
 *
 * @author vincanyang
 */
public interface MediaManager {

    void responseByRequest(Request request, Response response) throws ResponseException, IOException;

    void pauseDownload(String url);

    void resumeDownload(String url);

    void destroy();
}
