package cn.zhonggu.barsf.iota.explorer.services.utils.cache;

import java.util.Iterator;
import java.util.LinkedHashSet;

/**
 * Created by ZhuDH on 2018/5/2.
 */
public class FIFOCache<T> {

    private final int capacity;
    private LinkedHashSet<T> set;

    public FIFOCache(int capacity) {
        this.capacity = capacity;
        this.set = new LinkedHashSet<>();
    }

    public T pop() {
        T rv  = null;
        while (set.size() > 0) {
            Iterator<T> iterator = this.set.iterator();
            rv = iterator.next();
            iterator.remove();
        }
        return rv;
    }

    public boolean push(T value) {
        if (this.set.size() >= this.capacity) {
            Iterator<T> it = this.set.iterator();
            it.next();
            it.remove();
        }
        return this.set.add(value);
    }

    public static void main(String[] args) {
         LinkedHashSet<String> set = new LinkedHashSet<>(100);
         set.add("1");
         set.add("2");
         set.add("3");
         set.add("4");
         String aa = "";
         while (set.size() >0){
             System.out.println(set.size());
             Iterator<String> iterator = set.iterator();
             aa = iterator.next();
             iterator.remove();
             System.out.println(set.size());
//             System.out.println(aa);
         }
    }
}
