package com.uyscuti.social.medialoader.tinyhttpd.codec;

import com.uyscuti.social.medialoader.tinyhttpd.request.Request;
import com.uyscuti.social.medialoader.tinyhttpd.response.ResponseException;

/**
 * {@link Request}的解码器
 *
 * @author vincanyang
 */
public interface RequestDecoder<T extends Request> {

    T decode(byte[] bytes) throws ResponseException;
}
