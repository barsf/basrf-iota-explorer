package cn.zhonggu.barsf.iota.explorer.controllers;

import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import cn.zhonggu.barsf.iota.explorer.services.MilestoneTracer;
import cn.zhonggu.barsf.iota.explorer.services.TransactionService;
import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by ZhuDH on 2018/4/3.
 */
@Controller
@RequestMapping("/bundle")
public class BundleController {
    private static final int INVALIDED_BUNDLE_FLAG = -987654321;
    private static final int MILESTONE_BUNDLE_FLAG = -123456789;

    @Resource
    private TransactionService transactionService;
    @Autowired
    private MilestoneTracer mt;

    @RequestMapping("/{bundlehash}")
    public String directToBundleInfo(Model model, @PathVariable("bundlehash") String pk) {
        String realHash = TransactionHelper.cutEveryTo81(pk);
        model.addAttribute("pk", realHash);
        return "business/bundleinfo";
    }

    @ResponseBody
    @RequestMapping("/detail/{bundlehash}")
    public HashMap<String, Object> getAddressByPk(@PathVariable("bundlehash") String bundleHash) {
        String realHash = TransactionHelper.cutEveryTo81(bundleHash);
        List<Transaction> attachTrans = transactionService.getTransactionByBundle(realHash);

        int status = 1;
        long amountTrans = 0;
        long minTime = Long.MAX_VALUE;

        //input output 分组
        ArrayList<HashMap<Long, Transaction>> implicatedList = TransactionHelper.getImplicatedTrans(attachTrans, new AtomicBoolean(false));


        Transaction lastTailTrans = null;
        final int[] totolAmountOfAttachTran = {0};
        for (HashMap<Long, Transaction> attachTran : implicatedList) {
            if (lastTailTrans !=null && totolAmountOfAttachTran[0] != 0){
                lastTailTrans.snapshot = INVALIDED_BUNDLE_FLAG;
                totolAmountOfAttachTran[0] = 0;
            }
            Transaction tailTrans = attachTran.get(0L);
            amountTrans = +tailTrans.getValue();

            if (tailTrans.getSnapshot() != -1 && tailTrans.getSnapshot() <= 0) {
                status = 0;
            }
            // TODO: 2018/5/17 iri提供完整的验证逻辑, 太多东西了, 没有扯过来... 这里只简单的验证金额
            // 检查总金额,不为0则不合法
            attachTran.forEach((k,v)-> totolAmountOfAttachTran[0] +=v.getValue());
            lastTailTrans = tailTrans;
            minTime = Math.min(minTime,
                    tailTrans.getAttachmentTimestamp() == 0 ? Long.parseLong((tailTrans.getTimestamp() + "000").substring(0, 13)) : tailTrans.getAttachmentTimestamp());
            if (mt.MILESTONE_SET.contains(tailTrans.getHash())){
                tailTrans.snapshot= MILESTONE_BUNDLE_FLAG;
            }
        }

        if (lastTailTrans !=null && totolAmountOfAttachTran[0] != 0){
            lastTailTrans.snapshot = INVALIDED_BUNDLE_FLAG;
            totolAmountOfAttachTran[0] = 0;
        }

        HashMap<String, Object> rv = new HashMap<String, Object>();
        rv.put("total", amountTrans);
        rv.put("time", minTime);
        rv.put("status", status);
        rv.put("list", implicatedList);
        return rv;
    }

}
