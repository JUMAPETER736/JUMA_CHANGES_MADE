package com.uyscuti.sharedmodule.cache.objectcache;

public interface GetCallback<T> {
	void onSuccess(T object);

	void onFailure(Exception e);
}
