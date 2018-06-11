package cn.zhonggu.barsf.iota.explorer.services;

import cn.zhonggu.barsf.iota.explorer.dao.mapper.MilestoneMapper;
import cn.zhonggu.barsf.iota.explorer.dao.mapper.TransactionMapper;
import cn.zhonggu.barsf.iota.explorer.dao.mapper.TransactionTrytesMapper;
import cn.zhonggu.barsf.iota.explorer.dao.models.Milestone;
import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import cn.zhonggu.barsf.iota.explorer.dao.models.TransactionTrytes;
import cn.zhonggu.barsf.iota.explorer.services.utils.cache.softCache.TransactionCache;
import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import org.apache.ibatis.session.RowBounds;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import tk.mybatis.mapper.entity.Example;

import javax.annotation.PostConstruct;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by ZhuDH on 2018/4/2.
 */
@Service
public class TransactionService {
    private static final int ADDRESS_QUERY_SIZE = 100;
    private static final int BUNDLE_QUERY_SIZE = 2000;
    // 这个缓存只保存通过hash查询而来的数据(因为它必定会再查一次), 不保存任何直接根据类型查询的数据(重复查询率很低)
    private static final TransactionCache<String, Transaction> txCache = new TransactionCache<String, Transaction>();

    @Autowired
    private MilestoneTracer mt;
    @Autowired
    private TransactionMapper mapper;
    @Autowired
    private TransactionTrytesMapper trytesMapper;

    public Transaction getTransactionByPk(String pk) {
        Transaction cacheTransaction = txCache.get(pk, false);
        cacheTransaction = cacheTransaction == null ? mapper.selectByPrimaryKey(pk) : cacheTransaction;
        if (mt.MILESTONE_SET.contains(cacheTransaction.getHash())){
            // 约定 这个值表示里程碑
            cacheTransaction.setSnapshot(-123456789);
        }
        return cacheTransaction;
    }

    public TransactionTrytes getTransactionBody(String pk) {
        return trytesMapper.selectByPrimaryKey(pk);
    }

    public List<Transaction> getTransactionByAddress(String addressHash) {
        return mapper.selectByAddress(addressHash, ADDRESS_QUERY_SIZE);
    }

    public List<Transaction> getTransactionByBundle(String bundleHash) {
        return mapper.selectByBundle(bundleHash, BUNDLE_QUERY_SIZE);
    }

    public String getTransactionByHash(String hash) {
        Example example = new Example(Transaction.class);
        example.createCriteria().andEqualTo("address", hash)
                .orEqualTo("bundle", hash)
                .orEqualTo("hash", hash)
                .orEqualTo("tag", TransactionHelper.add9To81(hash));
        RowBounds rb = new RowBounds(0, ADDRESS_QUERY_SIZE);
        List<Transaction> allMatched = mapper.selectByExampleAndRowBounds(example, rb);

        final AtomicInteger trans = new AtomicInteger(0);
        final AtomicInteger bundle = new AtomicInteger(0);
        final AtomicInteger addr = new AtomicInteger(0);
        final AtomicInteger tag = new AtomicInteger(0);
        allMatched.forEach(tx ->
                {
                    // 保存对象到缓存
                    txCache.set(tx.getHash(), tx);
                    if (tx.getHash().equals(hash)) {
                        trans.incrementAndGet();
                    } else if (tx.getBundle().equals(hash)) {
                        bundle.incrementAndGet();
                    } else if (tx.getAddress().equals(hash)) {
                        addr.incrementAndGet();
                    } else {
                        tag.incrementAndGet();
                    }

                }
        );


        if (trans.get() >= bundle.get() && trans.get() >= addr.get() && trans.get() >= tag.get() && trans.get() > 0) {
            return "tran";
        } else if (bundle.get() >= trans.get() && bundle.get() >= addr.get() && bundle.get() >= tag.get() && bundle.get() > 0) {
            return "bundle";
        } else if (addr.get() >= trans.get() && addr.get() > bundle.get() && addr.get() > tag.get() && addr.get() > 0) {
            return "addr";
        } else if (tag.get() > 0) {
            return "tag";
        } else {
            return "none";
        }
    }

    public List<Transaction> getLeftTransaction(String hash) {
        Example example = new Example(Transaction.class);
        example.selectProperties("hash").createCriteria().andEqualTo("trunk", hash);
        List<Transaction> allMatched = mapper.selectByExample(example);
        return allMatched;
    }
}

