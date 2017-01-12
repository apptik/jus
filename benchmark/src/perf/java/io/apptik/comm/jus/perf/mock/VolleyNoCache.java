package io.apptik.comm.jus.perf.mock;

public class VolleyNoCache implements com.android.volley.Cache {
    @Override
    public void clear() {
    }

    @Override
    public Entry get(String key) {
        return null;
    }

    @Override
    public void put(String key, Entry entry) {
    }

    @Override
    public void invalidate(String key, boolean fullExpire) {
    }

    @Override
    public void remove(String key) {
    }

    @Override
    public void initialize() {
    }
}
