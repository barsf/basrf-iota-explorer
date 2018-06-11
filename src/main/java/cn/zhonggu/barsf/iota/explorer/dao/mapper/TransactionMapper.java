package cn.zhonggu.barsf.iota.explorer.dao.mapper;


import cn.zhonggu.barsf.iota.explorer.dao.mapper.base.IotaEntryBaseMapper;
import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import org.apache.ibatis.annotations.*;

import java.util.List;

/**
 * Created by ZhuDH on 2018/3/29.
 */
public interface TransactionMapper extends IotaEntryBaseMapper<Transaction> {
    @Select({"select * from t_transaction where address = #{address} limit #{limit}"})
    public List<Transaction> selectByAddress(@Param("address") String address,@Param("limit") int limit);

    @Select({"select * from t_transaction where bundle = #{bundle} limit #{limit}"})
    public List<Transaction> selectByBundle(@Param("bundle") String bundle,@Param("limit") int limit);
}
