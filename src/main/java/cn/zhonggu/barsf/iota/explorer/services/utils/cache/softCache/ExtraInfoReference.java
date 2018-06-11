package cn.zhonggu.barsf.iota.explorer.services.utils.cache.softCache;

import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;

public class ExtraInfoReference<T> extends SoftReference<T> {
    private Object info;

    public ExtraInfoReference(Object info, T t, ReferenceQueue<T> refQueue) {
        super(t, refQueue);
        this.info = info;
    }

    public Object getExtraInfo() {
        return this.info;
    }
}