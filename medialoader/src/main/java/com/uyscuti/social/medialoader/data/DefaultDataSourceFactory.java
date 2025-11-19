package com.uyscuti.social.medialoader.data;

import com.uyscuti.social.medialoader.MediaLoaderConfig;
import com.uyscuti.social.medialoader.data.file.FileDataSource;
import com.uyscuti.social.medialoader.data.file.RandomAcessFileDataSource;
import com.uyscuti.social.medialoader.data.file.cleanup.DiskLruCache;
import com.uyscuti.social.medialoader.data.file.cleanup.SimpleDiskLruCache;
import com.uyscuti.social.medialoader.data.url.DefaultUrlDataSource;
import com.uyscuti.social.medialoader.data.url.UrlDataSource;

import java.io.File;

/**
 * 数据源默认生产工厂
 *
 * @author vincanyang
 */
public final class DefaultDataSourceFactory {

    public static UrlDataSource createUrlDataSource(String url) {
        return new DefaultUrlDataSource(url);
    }

    public static UrlDataSource createUrlDataSource(DefaultUrlDataSource dataSource) {
        return new DefaultUrlDataSource(dataSource);
    }

    public static FileDataSource createFileDataSource(File file, DiskLruCache diskLruStorage) {
        return new RandomAcessFileDataSource(file, diskLruStorage);
    }

    public static DiskLruCache createDiskLruCache(MediaLoaderConfig mediaLoaderConfig) {
        return new SimpleDiskLruCache(mediaLoaderConfig);
    }
}
