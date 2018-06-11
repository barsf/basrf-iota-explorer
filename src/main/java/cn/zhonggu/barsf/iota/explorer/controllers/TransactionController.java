package cn.zhonggu.barsf.iota.explorer.controllers;

import cn.zhonggu.barsf.iota.explorer.dao.models.Transaction;
import cn.zhonggu.barsf.iota.explorer.dao.models.TransactionTrytes;
import cn.zhonggu.barsf.iota.explorer.services.TransactionService;
import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.List;
import java.util.zip.DataFormatException;

import static cn.zhonggu.barsf.iota.explorer.utils.Converter.uncompress;

/**
 * Created by ZhuDH on 2018/4/2.
 */

@Controller
@RequestMapping("/tran")
public class TransactionController {
    private static Logger logger = LoggerFactory.getLogger(TransactionController.class);

    @Resource
    private TransactionService transactionService;

    @ResponseBody
    @RequestMapping("/detail/{transhash}")
    public Transaction directToTraninfo(@PathVariable("transhash") String pk, @RequestParam(name = "showSignature", required = false, defaultValue = "false") boolean showSignature) {
        String realHash = TransactionHelper.cutEveryTo81(pk);

        Transaction tran = transactionService.getTransactionByPk(realHash);
        if (tran != null) {
            tran.setTimestamp(Long.parseLong((tran.getTimestamp() + "000").substring(0, 13)));

            if (tran.currentIndex != 0) {
                List<Transaction> leftTran = transactionService.getLeftTransaction(tran.getHash());
                if (leftTran.size()>1){
                    logger.info(" mutileLeftTran, check this:"+tran.getHash());
                    tran.setLeftTran(leftTran.get(0).getHash());
                } else if (leftTran.size() == 1) {
                    tran.setLeftTran(leftTran.get(0).getHash());
                }
            }
            if (tran.currentIndex != tran.lastIndex) {
                tran.setRightTran(tran.getTrunk());
            }

        }

        if (showSignature && tran.getInnerTrytes() == null) {
            TransactionTrytes body = transactionService.getTransactionBody(realHash);
            String signature = null;
            try {
                signature = TransactionHelper.getSignature(uncompress(body.bytes));
            } catch (DataFormatException e) {
                signature = "parse failed";
            }
            tran.setSignature(signature);
        }

        return tran;
    }


    @RequestMapping("/{transhash}")
    public String hellow(Model model, @PathVariable("transhash") String pk) {
        String realHash = TransactionHelper.cutEveryTo81(pk);
        model.addAttribute("pk", realHash);
        return "business/transinfo";
    }

}
