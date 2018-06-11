package cn.zhonggu.barsf.iota.explorer.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class BarsfApiController {

    @RequestMapping("/barsfapi")
    public String gotoBarsfApi(){

        return  "/ext/barsfapi";
    }

}
