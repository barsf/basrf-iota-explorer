package cn.zhonggu.barsf.iota.explorer.dao.models;

import javax.persistence.*;

/**
 * Created by ZhuDH on 2018/4/2.
 */
@Table(name = "t_transaction_trytes")
public class TransactionTrytes {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "Mysql")
    private String hash;

    @Column(name = "trytes")
    public byte[] bytes;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}
