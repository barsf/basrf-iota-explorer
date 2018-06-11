package cn.zhonggu.barsf.iota.explorer.services.utils.cache.softCache;

/**
 * Created by ZhuDH on 2018/4/4.
 */
public class TransactionCache<K,V> extends SoftRefCache<K,V> {
    @Override
    protected V supplyValue(K key) {
        throw new RuntimeException("method do not support");
    }
}
