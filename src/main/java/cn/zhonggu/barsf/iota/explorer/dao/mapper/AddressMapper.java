package cn.zhonggu.barsf.iota.explorer.dao.mapper;


import cn.zhonggu.barsf.iota.explorer.dao.mapper.base.IotaEntryBaseMapper;
import cn.zhonggu.barsf.iota.explorer.dao.models.Address;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * Created by ZhuDH on 2018/3/29.
 */
public interface AddressMapper extends IotaEntryBaseMapper<Address> {
    // 专为地址查询TransHash优化
    @Select("SELECT `hash` FROM t_transaction where address = #{address}")
    List<String> selectHashesByAddress(@Param("address") String address);
}
