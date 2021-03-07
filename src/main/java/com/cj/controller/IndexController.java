package com.cj.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * @author chenjie 2020-04-11 20:56:31
 */
@Controller
public class IndexController {

    @RequestMapping("/")
    public String index(Model model) {

        return "index";
    }

}