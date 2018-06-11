package cn.zhonggu.barsf.iota.explorer.services.utils.cache.softCache;

public interface Cache<K, V> {
    public V get(K key,boolean createIfEmpty);
    public boolean set(K key, V value);
}