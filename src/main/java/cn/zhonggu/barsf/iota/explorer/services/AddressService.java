package cn.zhonggu.barsf.iota.explorer.services;

import cn.zhonggu.barsf.iota.explorer.dao.models.Address;
import cn.zhonggu.barsf.iota.explorer.dao.mapper.AddressMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by ZhuDH on 2018/4/3.
 */
@Service
public class AddressService {
    @Autowired
    private AddressMapper mapper;

    public Address getAddressByPk(String pk){
        return mapper.selectByPrimaryKey(pk);
    }

    /* 验证地址是否使用过 */
}
