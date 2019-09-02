package com.leyou.goods.controller;

import com.leyou.goods.service.GoodsHtmlService;
import com.leyou.goods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.ModelAndView;

import java.util.Map;

/**
 * Feature: 商品详情页面跳转
 */
@Controller
@RequestMapping("item")
public class GoodsController {

    @Autowired
    private GoodsService goodsService;
    
    @Autowired
    private GoodsHtmlService goodsHtmlService;

    @GetMapping("{id}.html")
    public String toItemPage(Model model, @PathVariable("id") Long id){
        Map<String, Object> modelMap = this.goodsService.loadModel(id);
        model.addAllAttributes(modelMap);
        for(Map.Entry<String, Object> entry: modelMap.entrySet()){
            System.out.println(entry.getKey() + ":" + entry.getValue());
        }
        goodsHtmlService.asyncExecute(id);
        return "item";
    }
}
