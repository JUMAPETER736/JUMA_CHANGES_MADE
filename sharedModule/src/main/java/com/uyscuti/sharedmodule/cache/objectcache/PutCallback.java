package com.uyscuti.sharedmodule.cache.objectcache;

public interface PutCallback {
	void onSuccess();

	void onFailure(Exception e);
}
