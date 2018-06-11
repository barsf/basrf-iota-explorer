package cn.zhonggu.barsf.iota.explorer.dao.models;

import javax.persistence.*;

/**
 * Created by paul on 5/6/17.
 */
@Table(name = "t_state_diff")
public class StateDiff {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY,generator="Mysql")
    private String hash;
    @Column(name = "trytes")
    private byte[] trytes;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public byte[] getTrytes() {
        return trytes;
    }

    public void setTrytes(byte[] trytes) {
        this.trytes = trytes;
    }
}
