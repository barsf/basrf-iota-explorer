package cn.zhonggu.barsf.iota.explorer.dao.models;

import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import org.springframework.util.StringUtils;

import javax.persistence.*;

/**
 * Created by paul on 3/2/17 for iri.
 */
@Table(name = "t_transaction")
public class Transaction  {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY, generator = "Mysql")
    private String hash;
    private Boolean barsfTransaction = false;
    // 注意,这个字段已经被分隔到另一张表中 [TransactionTrytes]
    @Transient
    public byte[] bytes;
    @Transient
    private TransactionTrytes innerTrytes;
    public String address;
    public String bundle;
    public String trunk;
    public String branch;
    public String obsoleteTag;
    public Long value;
    public Long currentIndex;
    public Long lastIndex;
    public Long timestamp;

    public String tag ;
    public Long attachmentTimestamp;
    @Column(name = "at_upper_bound")
    public Long attachmentTimestampUpperBound;
    @Column(name = "at_lower_bound")
    public Long attachmentTimestampLowerBound;

    public Integer validity;
    public Integer type = -1;
    public Long arrivalTime;

    @Transient
    public Boolean parsed = false;
    public Boolean solid = false;
    public Long height = 0L;
    public String sender = "";
    public Integer snapshot;

    @Transient
    public String signature = "";
    @Transient
    public String leftTran = "";
    @Transient
    public String rightTran = "";


    public Transaction() {
        this.barsfTransaction = false;
        this.bytes = null;
        this.innerTrytes = null;
        this.address = "LOADING...";
        this.bundle = "LOADING...";
        this.trunk = "LOADING...";
        this.branch = "LOADING...";
        this.obsoleteTag = "LOADING...";
        this.value = 0L;
        this.currentIndex = 0L;
        this.lastIndex = 0L;
        this.timestamp = 0L;
        this.tag = "none";
        this.attachmentTimestamp = 0L;
        this.attachmentTimestampUpperBound = 0L;
        this.attachmentTimestampLowerBound = 0L;
        this.validity = 0;
        this.type = 0;
        this.arrivalTime = 0L;
        this.parsed = false;
        this.solid = false;
        this.height = 0L;
        this.sender = "LOADING...";
        this.snapshot = 0;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public Boolean getBarsfTransaction() {
        return barsfTransaction;
    }

    public void setBarsfTransaction(Boolean barsfTransaction) {
        this.barsfTransaction = barsfTransaction;
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }

    public TransactionTrytes getInnerTrytes() {
        return innerTrytes;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getBundle() {
        return bundle;
    }

    public void setBundle(String bundle) {
        this.bundle = bundle;
    }

    public String getTrunk() {
        return trunk;
    }

    public void setTrunk(String trunk) {
        this.trunk = trunk;
    }

    public String getBranch() {
        return branch;
    }

    public void setBranch(String branch) {
        this.branch = branch;
    }

    public String getObsoleteTag() {
        return obsoleteTag;
    }

    public void setObsoleteTag(String obsoleteTag) {
        this.obsoleteTag = obsoleteTag;
    }

    public Long getArrivalTime() {
        return arrivalTime;
    }

    public void setArrivalTime(Long arrivalTime) {
        this.arrivalTime = arrivalTime;
    }

    public Long getValue() {
        return value;
    }

    public void setValue(Long value) {
        this.value = value;
    }

    public Long getCurrentIndex() {
        return currentIndex;
    }

    public void setCurrentIndex(Long currentIndex) {
        this.currentIndex = currentIndex;
    }

    public Long getLastIndex() {
        return lastIndex;
    }

    public void setLastIndex(Long lastIndex) {
        this.lastIndex = lastIndex;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getTag() {
        return tag;
    }

    public void setTag(String tag) {
        tag = TransactionHelper.cut9From81(tag);

        if (!StringUtils.isEmpty(tag)){
            this.tag = tag;
        }
    }

    public Long getAttachmentTimestamp() {
        return attachmentTimestamp;
    }

    public void setAttachmentTimestamp(Long attachmentTimestamp) {
        this.attachmentTimestamp = attachmentTimestamp;
    }

    public Long getAttachmentTimestampUpperBound() {
        return attachmentTimestampUpperBound;
    }

    public void setAttachmentTimestampUpperBound(Long attachmentTimestampUpperBound) {
        this.attachmentTimestampUpperBound = attachmentTimestampUpperBound;
    }

    public Long getAttachmentTimestampLowerBound() {
        return attachmentTimestampLowerBound;
    }

    public void setAttachmentTimestampLowerBound(Long attachmentTimestampLowerBound) {
        this.attachmentTimestampLowerBound = attachmentTimestampLowerBound;
    }

    public Integer getValidity() {
        return validity;
    }

    public void setValidity(Integer validity) {
        this.validity = validity;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }


    public Boolean getParsed() {
        return parsed;
    }

    public void setParsed(Boolean parsed) {
        this.parsed = parsed;
    }

    public Boolean getSolid() {
        return solid;
    }

    public void setSolid(Boolean solid) {
        this.solid = solid;
    }

    public Long getHeight() {
        return height;
    }

    public void setHeight(Long height) {
        this.height = height;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public Integer getSnapshot() {
        return snapshot;
    }

    public void setSnapshot(Integer snapshot) {
        this.snapshot = snapshot;
    }

    public String getSignature() {
        return signature;
    }

    public void setSignature(String signature) {
        this.signature = signature;
    }

    // 从数据库中读取数据时,需要通过这个方法setTrytes 内部对bytes做了联动
    public void setInnerTrytes(TransactionTrytes innerTrytes) {
        this.innerTrytes = innerTrytes;
        this.bytes = innerTrytes.bytes;
    }

    public String getLeftTran() {
        return leftTran;
    }

    public void setLeftTran(String leftTran) {
        this.leftTran = leftTran;
    }

    public String getRightTran() {
        return rightTran;
    }

    public void setRightTran(String rightTran) {
        this.rightTran = rightTran;
    }
}
