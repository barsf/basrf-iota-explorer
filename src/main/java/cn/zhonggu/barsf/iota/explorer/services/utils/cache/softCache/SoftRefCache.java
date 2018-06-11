package cn.zhonggu.barsf.iota.explorer.services.utils.cache.softCache;

import java.lang.ref.ReferenceQueue;
import java.util.concurrent.ConcurrentHashMap;


public abstract class SoftRefCache<K, V> implements Cache<K, V> {
    // 缓存，用软引用记录
    private ConcurrentHashMap<K, ExtraInfoReference<V>> cache = new ConcurrentHashMap<K, ExtraInfoReference<V>>();
    private ReferenceQueue<V> refQueue = new ReferenceQueue<V>();

    @Override
    public V get(K key,boolean createIfEmpty) {
        V value = null;

        if (cache.containsKey(key)) {
            ExtraInfoReference<V> refValue = cache.get(key);
            value = refValue.get();
        }
        // 如果软引用被回收
        if (createIfEmpty && value == null) {
            // 清除软引用队列
            clearRefQueue();
            // 创建值并放入缓存
            value = supplyValue(key);
            ExtraInfoReference<V> refValue = new ExtraInfoReference<V>(key, value, refQueue);
            cache.put(key, refValue);
        }

        return value;
    }

    /**
     * 实现set方法
     */
    @Override
    public boolean set(K key, V value) {
        ExtraInfoReference<V> refValue = new ExtraInfoReference<V>(key, value, refQueue);
        cache.put(key, refValue);
        return true;
    }

    /**
     * 定义创建值的方法
     * @return
     */
    protected abstract V supplyValue(K key);

    /**
     * 从软引用队列中移除无效引用，
     * 同时从cache中删除无效缓存
     */
    @SuppressWarnings("unchecked")
    protected void clearRefQueue() {
        ExtraInfoReference<V> refValue = null;
        while((refValue = (ExtraInfoReference<V>) refQueue.poll()) != null) {
            K key = (K) refValue.getExtraInfo();
            cache.remove(key);
        }
    }
}