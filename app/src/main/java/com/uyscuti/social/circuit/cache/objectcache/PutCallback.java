package com.uyscuti.social.circuit.cache.objectcache;

public interface PutCallback {
	void onSuccess();

	void onFailure(Exception e);
}
