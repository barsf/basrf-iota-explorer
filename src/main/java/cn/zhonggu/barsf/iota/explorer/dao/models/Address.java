package cn.zhonggu.barsf.iota.explorer.dao.models;

import javax.persistence.Table;


/**
 * Created by paul on 5/15/17.
 */
@Table(name="t_address")
public class Address extends Hashes{
    private Long barsfBalance = 0L;

    public Long getBarsfBalance() {
        return barsfBalance;
    }

    public void setBarsfBalance(Long barsfBalance) {
        this.barsfBalance = barsfBalance;
    }

    public Address() {
    }
}
