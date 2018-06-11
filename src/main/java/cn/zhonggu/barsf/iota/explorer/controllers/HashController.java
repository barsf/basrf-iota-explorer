package cn.zhonggu.barsf.iota.explorer.controllers;

import cn.zhonggu.barsf.iota.explorer.services.TransactionService;
import cn.zhonggu.barsf.iota.explorer.utils.TransactionHelper;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

/**
 * Created by ZhuDH on 2018/4/4.
 */
@Controller
@RequestMapping("/hash")
public class HashController {

    @Resource
    private TransactionService txService;

    @RequestMapping("/notfound/{errHash}")
    public String notFund(@PathVariable("errHash") String errHash, Model model) {
        errHash = TransactionHelper.cutEveryTo81(errHash);
        model.addAttribute("errHash", errHash);
        return "hashnotfound";  // todo
    }

    @RequestMapping("/{hash}")
    @ResponseBody
    public String switchTo(@PathVariable("hash") String hash) {
        String realHash = TransactionHelper.cutEveryTo81(hash);
        String belong = txService.getTransactionByHash(realHash);
        if ("none".equals(belong)) {
            return "hash/notfound/" + realHash;
        } else if ("addr".equals(belong)) {
            return "addr/" + realHash;
        } else if ("tran".equals(belong)) {
            return "tran/" + realHash;
        } else if ("bundle".equals(belong)) {
            return "bundle/" + realHash;
        } else if ("tag".equals(belong)) {
            return "tag/" + realHash;
        }
        throw new RuntimeException("type not found");
    }
}
