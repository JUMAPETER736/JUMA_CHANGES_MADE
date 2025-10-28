package com.uyscuti.social.circuit.cache.objectcache;



import static com.uyscuti.social.circuit.cache.diskcatche.EnhancedGson25JsonReader.getEnhancedGson25JsonReader;

import android.os.AsyncTask;
import android.util.Log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import com.uyscuti.social.circuit.cache.diskcatche.EnhancedGson25JsonReader;


import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;

public class CacheManager {
    private final static int CACHE_RUSH_SECONDS = 60 * 2;
    private static CacheManager ourInstance;
    private final DiskCache diskCache;
    private final HashMap<String, CachedObject> runtimeCache;

    public static CacheManager getInstance(DiskCache diskCache) {
        if (ourInstance == null) {
            ourInstance = new CacheManager(diskCache);
        }

        return ourInstance;
    }

    private CacheManager(DiskCache diskCache) {
        this.diskCache = diskCache;
        runtimeCache = new HashMap<String, CachedObject>();
    }

    public boolean exists(String key) {
        boolean result = false;

        try {
            result = diskCache.contains(key);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return result;
    }

    public Object get(String key, Class objectClass, Type objectType) {
        Object result = null;

        CachedObject runtimeCachedObject = runtimeCache.get(key);
        if (runtimeCachedObject != null && !runtimeCachedObject.isExpired()) {
            result = new Gson().fromJson(runtimeCachedObject.getPayload(), objectType);
        } else if (runtimeCachedObject != null && runtimeCachedObject.isSoftExpired()) {
            result = new SoftCachedObject<Object>(new Gson().fromJson(runtimeCachedObject.getPayload(), objectType));
        } else {
            try {
                String json = diskCache.getValue(key);
                if (json != null) {
                    CachedObject cachedObject = new Gson().fromJson(json, CachedObject.class);
                    if (!cachedObject.isExpired()) {
                        runtimeCache.put(key, cachedObject);
                        result = new Gson().fromJson(cachedObject.getPayload(), objectType);
                    } else {
                        if (cachedObject.isSoftExpired()) {
                            result = new SoftCachedObject<Object>(new Gson().fromJson(cachedObject.getPayload(), objectType));
                        }

                        // To avoid cache rushing, we insert the value back in the cache with a longer expiry
                        // Presumably, whoever received this expiration result will have inserted a fresh value by now
                        putAsync(key, new Gson().fromJson(cachedObject.getPayload(), objectType), CACHE_RUSH_SECONDS, false, new PutCallback() {
                            @Override
                            public void onSuccess() {

                            }

                            @Override
                            public void onFailure(Exception e) {

                            }
                        });
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                // Do nothing, return null
            }
        }

        return result;
    }

    public void getAsync(String key, Class objectClass, Type objectType, GetCallback getCallback) {
        new GetAsyncTask(key, objectClass, objectType, getCallback).execute();
    }

    public boolean unset(String key) {
        return put(key, null, -1, false);
    }

    public void unsetAsync(String key, PutCallback putCallback) {
        putAsync(
                key, null, -1,
                false, putCallback);
    }

    public boolean put(
            String key, Object object) {
        return put(key, object, -1, false);
    }

    public boolean put(
            String key, Object object, int expiryTimeSeconds,
            boolean allowSoftExpiry) {
        boolean result = false;

        try {
            String payloadJson = new Gson().toJson(object);
            CachedObject cachedObject = new CachedObject(payloadJson, expiryTimeSeconds, allowSoftExpiry);
            String json = new Gson().toJson(cachedObject);
            runtimeCache.put(key, cachedObject);
            diskCache.setKeyValue(key, json);
            result = true;
        } catch (Exception e) {
            e.printStackTrace();
            // Do nothing, return false
        }

        return result;
    }

    public void putAsync(String key, Object object, PutCallback putCallback) {
        putAsync(key, object, -1, false, putCallback);
    }

    public void putAsync(String key, Object object, int expiryTimeSeconds, boolean allowSoftExpiry, PutCallback putCallback) {
        new PutAsyncTask(key, object, expiryTimeSeconds, allowSoftExpiry, putCallback).execute();
    }

    public void clear() throws IOException {
        runtimeCache.clear();
        diskCache.clearCache();
    }

    public enum ExpiryTimes {
        ONE_SECOND(1),
        ONE_MINUTE(60),
        ONE_HOUR(60 * 60),
        ONE_DAY(60 * 60 * 24),
        ONE_WEEK(60 * 60 * 24 * 7),
        ONE_MONTH(60 * 60 * 24 * 30),
        ONE_YEAR(60 * 60 * 24 * 365);

        private final int seconds;

        ExpiryTimes(int seconds) {
            this.seconds = seconds;
        }

        public int asSeconds() {
            return seconds;
        }
    }

    @SuppressWarnings("unchecked")
    private class GetAsyncTask extends AsyncTask<Void, Void, Object> {
        private final String key;
        private final GetCallback callback;
        private final Type objectType;
        private final Class objectClass;
        private Exception e;

        private GetAsyncTask(String key, Class objectClass, Type objectType, GetCallback callback) {
            this.callback = callback;
            this.key = key;
            this.objectType = objectType;
            this.objectClass = objectClass;
        }

        @Override
        protected Object doInBackground(Void... voids) {
            Object result = null;

            CachedObject runtimeCachedObject = runtimeCache.get(key);
            if (runtimeCachedObject != null && !runtimeCachedObject.isExpired()) {
                result = new Gson().fromJson(runtimeCachedObject.getPayload(), objectType);
            } else if (runtimeCachedObject != null && runtimeCachedObject.isSoftExpired()) {
                result = new SoftCachedObject<Object>(new Gson().fromJson(runtimeCachedObject.getPayload(), objectType));
            } else {
                try {
                    String json = diskCache.getValue(key);
                    if (json != null) {
                        CachedObject cachedObject = new Gson().fromJson(json, CachedObject.class);

                        if (!cachedObject.isExpired()) {
                            result = new Gson().fromJson(cachedObject.getPayload(), objectType);
                            runtimeCache.put(key, cachedObject);
                        } else {
                            if (cachedObject.isSoftExpired()) {
                                result = new SoftCachedObject<Object>(new Gson().fromJson(cachedObject.getPayload(), objectType));
                            }

                            // To avoid cache rushing, we insert the value back in the cache with a longer expiry
                            // Presumably, whoever received this expiration result will have inserted a fresh value by now
                            putAsync(key, new Gson().fromJson(cachedObject.getPayload(), objectType), CACHE_RUSH_SECONDS, false, new PutCallback() {
                                @Override
                                public void onSuccess() {

                                }

                                @Override
                                public void onFailure(Exception e) {

                                }
                            });
                        }
                    }
                } catch (Exception e) {
                    this.e = e;
                }
            }

            return result;
        }

        @Override
        protected void onPostExecute(Object object) {
            if (callback != null) {
                if (e == null) {
                    callback.onSuccess(object);
                } else {
                    callback.onFailure(e);
                }
            }
        }
    }

    private class PutAsyncTask extends AsyncTask<Void, Void, Void> {
        private final PutCallback callback;
        private final String key;
        private Object payload;
        private final int expiryTimeSeconds;
        private final boolean allowSoftExpiry;
        private Exception e;

        private PutAsyncTask(String key, Object payload, int expiryTimeSeconds, boolean allowSoftExpiry, PutCallback callback) {
            this.key = key;
            this.callback = callback;
            this.payload = payload;
            this.expiryTimeSeconds = expiryTimeSeconds;
            this.allowSoftExpiry = allowSoftExpiry;
        }

        @Override
        protected Void doInBackground(Void... voids) {
			try {
				String payloadJson = new Gson().toJson(payload);
				// Convert payload to JSON in chunks

				CachedObject cachedObject = new CachedObject(payloadJson, expiryTimeSeconds, allowSoftExpiry);
				String json = new Gson().toJson(cachedObject);
				runtimeCache.put(key, cachedObject);
				diskCache.setKeyValue(key, json);
			} catch (Exception e) {
				this.e = e;
			}


            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            if (callback != null) {
                if (e == null) {
                    callback.onSuccess();
                } else {
                    callback.onFailure(e);
                }
            }
        }


        private String convertObjectToJsonInChunk(Object object) throws IOException {
            StringWriter stringWriter = new StringWriter();
            try (JsonWriter jsonWriter = new JsonWriter(stringWriter)) {
                Gson gson = new Gson();
                jsonWriter.beginArray(); // Start the JSON array

                // Convert object to JSON and write in chunks
                String objectJson = gson.toJson(object);
                int chunkSize = 4096; // Define your chunk size
                int length = objectJson.length();
                for (int i = 0; i < length; i += chunkSize) {
                    int endIndex = Math.min(i + chunkSize, length);
                    String chunk = objectJson.substring(i, endIndex);
                    jsonWriter.jsonValue(chunk);
                }

                jsonWriter.endArray(); // End the JSON array
            }
            return stringWriter.toString();
        }


        private String convertObjectToJsonInChunks(Object object) throws IOException {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            Gson gson = new Gson();
            jsonWriter.beginArray(); // Start the JSON array

            // Convert object to JSON and write in chunks
            String objectJson = gson.toJson(object);
            int chunkSize = 4096; // Define your chunk size
            int length = objectJson.length();
            for (int i = 0; i < length; i += chunkSize) {
                int endIndex = Math.min(i + chunkSize, length);
                String chunk = objectJson.substring(i, endIndex);
                jsonWriter.value(chunk);
            }

            jsonWriter.endArray(); // End the JSON array
            jsonWriter.close(); // Close the writer
            return stringWriter.toString();
        }

        private ByteArrayOutputStream convertCacheData(Object object) throws IOException {
            payload = null;
            ObjectMapper objectMapper = new ObjectMapper();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(outputStream);

            try {

                Log.d("SoundPickerActivity", "Converting object data to json");

                // Write payload object to JSON stream
                objectMapper.writeValue(jsonGenerator, object);
                try {
                    jsonGenerator.close();
                } catch (Exception e) {
                    Log.d("SoundPickerActivity", "failed to close json generator :" + e.getMessage());
                    e.printStackTrace();
                }
                Log.d("SoundPickerActivity", "Returning converted data");

                return outputStream;
            } finally {
                // Close resources
                objectMapper = null;
                jsonGenerator.close();
                outputStream.close();
            }
        }


        // Inside your convertCacheData method
        private String convertCacheDataT(Object object) throws IOException {
            Log.d("SoundPickerActivity", "Converting object data to json");

            ObjectMapper objectMapper = new ObjectMapper();
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            JsonGenerator jsonGenerator = new JsonFactory().createGenerator(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));

            try {
                // Write payload object to JSON stream
                objectMapper.writeValue(jsonGenerator, object);

                // Write JSON data to a temporary file
                File tempFile = File.createTempFile("temp", ".json");
                try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
                    outputStream.writeTo(fileOutputStream);
                }

                Log.d("SoundPickerActivity", "Cached data temp file size :" + tempFile.getPath() + " length : " + tempFile.length());

                // Read JSON data from the temporary file in chunks
                StringBuilder stringBuilder = new StringBuilder();
                try (FileInputStream fileInputStream = new FileInputStream(tempFile)) {
                    byte[] buffer = new byte[1024]; // Define your chunk size
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                        stringBuilder.append(new String(buffer, 0, bytesRead, StandardCharsets.UTF_8));

                    }
                }

                // Delete the temporary file
                tempFile.delete();

                Log.d("SoundPickerActivity", "Returning converted data");
                return stringBuilder.toString();
            } finally {
                // Close resources
                jsonGenerator.close();
                outputStream.close();
            }
        }

        private String convertDataToJsonInChunks(Object object) throws IOException {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            Gson gson = new Gson();

            // Begin writing JSON array
            jsonWriter.beginArray();

            // Serialize object to JSON and write in chunks
            gson.toJson(object, object.getClass(), jsonWriter);

            // End writing JSON array
            jsonWriter.endArray();

            jsonWriter.close(); // Close the writer
            return stringWriter.toString();
        }


        private String convertCacheObjectToJsonInChunks(Object object) throws IOException {
            StringWriter stringWriter = new StringWriter();
            JsonWriter jsonWriter = new JsonWriter(stringWriter);
            Gson gson = new Gson();

            // Begin writing JSON array
            jsonWriter.beginArray();

            // Serialize object to JSON and write in chunks
            gson.toJson(object, object.getClass(), jsonWriter);

            // End writing JSON array
            jsonWriter.endArray();

            jsonWriter.close(); // Close the writer
            return stringWriter.toString();
        }

        private String cacheObjectJson() throws IOException {
            try (final EnhancedGson25JsonReader input = getEnhancedGson25JsonReader(new InputStreamReader(new FileInputStream("./huge.json")));
                 final Writer output = new OutputStreamWriter(new BufferedOutputStream(new FileOutputStream("./huge.json.STRINGS")))) {
                while (input.hasNext()) {
                    final JsonToken token = input.peek();
                    switch (token) {
                        case BEGIN_OBJECT:
                            input.beginObject();
                            break;
                        case NAME:
                            input.nextName();
                            break;
                        case STRING:
                            input.nextSlicedString(output::write);
                            break;
                        default:
                            throw new AssertionError(token);
                    }
                }

                return output.toString();
            }
        }

    }
}
