package cn.zhonggu.barsf.iota.explorer.utils;

import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ZhuDH on 2018/4/9.
 */
public class TransactionHelper {
    private static final Logger log = LoggerFactory.getLogger(TransactionHelper.class);
    static final int[][] BYTE_TO_TRITS_MAPPINGS = new int[243][];
    public static final int NUMBER_OF_TRITS_IN_A_BYTE = 5;
    public static final int NUMBER_OF_TRITS_IN_A_TRYTE = 3;
    private static final int NONCE_TRINARY_OFFSET = 7938, NONCE_TRINARY_SIZE = 81;
    public static final int TRINARY_SIZE = NONCE_TRINARY_OFFSET + NONCE_TRINARY_SIZE;


    static {
        final int[] trits = new int[NUMBER_OF_TRITS_IN_A_BYTE];

        for (int i = 0; i < 243; i++) {
            BYTE_TO_TRITS_MAPPINGS[i] = Arrays.copyOf(trits, NUMBER_OF_TRITS_IN_A_BYTE);
            increment(trits, NUMBER_OF_TRITS_IN_A_BYTE);
        }

        for (int i = 0; i < 27; i++) {
            increment(trits, NUMBER_OF_TRITS_IN_A_TRYTE);
        }
    }
    private static void increment(final int[] trits, final int size) {
        for (int i = 0; i < size; i++) {
            if (++trits[i] > Converter.MAX_TRIT_VALUE) {
                trits[i] = Converter.MIN_TRIT_VALUE;
            } else {
                break;
            }
        }
    }


    // 找到相互牵连的数据分组
    public static ArrayList<HashMap<Long, Transaction>> getImplicatedTrans(List<Transaction> attachTrans, AtomicBoolean checkOk) {
        attachTrans.sort(Comparator.comparing(Transaction::getCurrentIndex));

        ArrayList<HashMap<Long, Transaction>> implicatedList = new ArrayList<>();
        for (Transaction attachTran : attachTrans) {
            if (attachTran.getCurrentIndex() == 0) {
                HashMap<Long, Transaction> implicateMap = new HashMap<>();
                implicatedList.add(implicateMap);
                implicateMap.put(0L, attachTran);
//                amountTrans = attachTran.getValue();
            } else {
                Long currentIndex = attachTran.getCurrentIndex();
                Transaction preTran = null;
                for (HashMap<Long, Transaction> aImpMap : implicatedList) {
                    if ((preTran = aImpMap.get(currentIndex - 1)) != null) {
                        if (preTran.getTrunk().equals(attachTran.getHash())) {
                            aImpMap.put(currentIndex, attachTran);
                            break;
                        }
                    }
                }
                if (preTran == null) {
                    log.error("tran should not be alone");
                    checkOk.set(false);
                }
            }
        }

        implicatedList.sort((o1, o2) -> (o1.get(0L).getAttachmentTimestamp() > o2.get(0L).getAttachmentTimestamp()) ? -1 : (o1.get(0L).getAttachmentTimestamp().equals(o2.get(0L).getAttachmentTimestamp())) ? 0 : 1);

        // 判断分析是否有reattachment
        // confirm的集合,如果hashcode重复的,并且unconfirmed就是reattachment;
        HashSet<Integer> confirmSet = new HashSet<>();
        for (HashMap<Long, Transaction> bundle : implicatedList) {
            boolean confirm = true;
            int totalHash= 0;
            for (Map.Entry<Long, Transaction> entry : bundle.entrySet()) {
                if (entry.getValue().snapshot <= 0){
                    confirm = false;
                }
                totalHash = (totalHash+entry.getValue().getAddress()+entry.getValue().getValue()).hashCode();
            }
            if (confirm){
                confirmSet.add(totalHash);
            }
        }

        for (HashMap<Long, Transaction> subBundle : implicatedList) {
            boolean confirm = true;
            int totalHash= 0;
            for (Map.Entry<Long, Transaction> entry : subBundle.entrySet()) {
                if (entry.getValue().snapshot <= 0){
                    confirm = false;
                }
                // 总地址金额hash一样就认为是reattach
                totalHash = (totalHash+entry.getValue().getAddress()+entry.getValue().getValue()).hashCode();
            }
            if (!confirm && confirmSet.contains(totalHash)){
                // 把index = 0 的snapshot改成 -1 表示reattach
                subBundle.get(0L).snapshot = -1;
            }
        }


        return implicatedList;
    }

    public static String getSignature(byte[] trytesInByte) {
        return Converter.trytes(Arrays.copyOfRange(trits(trytesInByte), 0, 6561));
    }

    public static byte[] getNonce(byte[] trytesInByte) {
        byte[] nonce = Converter.allocateBytesForTrits(NONCE_TRINARY_SIZE);
        Converter.bytes(trits(trytesInByte), NONCE_TRINARY_OFFSET, nonce, 0, 8019 - NONCE_TRINARY_OFFSET);
        return nonce;
    }

    public static String add9To81(String string) {
        if (string.length() > 81) {
            return string.substring(0, 81);
        }

        int lengthNeedAppend = 81 - string.length();

        StringBuilder stringBuilder = new StringBuilder(string);
        for (int i = 0; i < lengthNeedAppend; i++) {
            stringBuilder.append("9");
        }
        return stringBuilder.toString();
    }

    public static String cutEveryTo81(String bundleHash) {
        String realHash = bundleHash.trim().replaceAll("\"", "");
        if (realHash.length() > 81) {
            realHash = realHash.substring(0, 81);
        }
        return realHash;
    }

    public static String cut9From81(String string) {
        char[] chars = string.toCharArray();
        int finalIndex = chars.length;
        for (int i = chars.length - 1; i >= 0; i--) {
            if (chars[i] == '9') {
                finalIndex = i;
            } else {
                break;
            }
        }
        return new String(Arrays.copyOf(chars, finalIndex));
    }

    public static int[] trits(byte[] transactionBytes) {
        int[] trits;
        trits = new int[TRINARY_SIZE];
        if (transactionBytes != null) {
            getTrits(transactionBytes, trits);
        }
        return trits;
    }

    public static void getTrits(final byte[] bytes, final int[] trits) {

        int offset = 0;
        for (int i = 0; i < bytes.length && offset < trits.length; i++) {
            System.arraycopy(BYTE_TO_TRITS_MAPPINGS[bytes[i] < 0 ? (bytes[i] + BYTE_TO_TRITS_MAPPINGS.length) : bytes[i]], 0, trits, offset, trits.length - offset < NUMBER_OF_TRITS_IN_A_BYTE ? (trits.length - offset) : NUMBER_OF_TRITS_IN_A_BYTE);
            offset += NUMBER_OF_TRITS_IN_A_BYTE;
        }
        while (offset < trits.length) {
            trits[offset++] = 0;
        }
    }
}
