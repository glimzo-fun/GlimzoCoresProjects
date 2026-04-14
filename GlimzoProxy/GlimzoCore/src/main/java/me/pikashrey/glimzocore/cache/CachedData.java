package me.pikashrey.glimzocore.cache;
/**
 * Wraps a value with a timestamp, used for timed in-memory caches.
 */
public class CachedData<T> {
    private final T    value;
    private final long cachedAt;
    private final long ttlMs;
    public CachedData(T value, long ttlMs) {
        this.value    = value;
        this.cachedAt = System.currentTimeMillis();
        this.ttlMs    = ttlMs;
    }
    public T       getValue()   { return value; }
    public boolean isExpired()  { return System.currentTimeMillis() - cachedAt > ttlMs; }
    public boolean isValid()    { return !isExpired(); }
    public long    getAge()     { return System.currentTimeMillis() - cachedAt; }
}
