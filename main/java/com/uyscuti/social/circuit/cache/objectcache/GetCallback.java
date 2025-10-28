package com.uyscuti.social.circuit.cache.objectcache;

public interface GetCallback<T> {
	void onSuccess(T object);

	void onFailure(Exception e);
}
