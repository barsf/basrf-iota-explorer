package cn.zhonggu.barsf.iota.explorer.dao.models;

import javax.persistence.*;

/**
 * Created by paul on 4/11/17.
 */
@Table(name = "t_milestone")
public class Milestone{

    @Id
    @Column(name = "mt_index")
    @GeneratedValue(strategy= GenerationType.IDENTITY,generator="Mysql")
    public Integer index;

    @Column(name = "tx_hash")
    public String hash;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
