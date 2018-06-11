package cn.zhonggu.barsf.iota.explorer.dao.models;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Created by paul on 3/8/17 for iri.
 */

public class Hashes {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY,generator="Mysql")
    private String hash;

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }
}
