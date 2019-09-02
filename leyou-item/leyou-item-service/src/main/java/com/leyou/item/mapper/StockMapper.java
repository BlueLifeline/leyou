package com.leyou.item.mapper;

import com.leyou.item.pojo.Sku;
import com.leyou.item.pojo.Stock;
import tk.mybatis.mapper.additional.idlist.SelectByIdListMapper;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface StockMapper extends Mapper<Stock>, SelectByIdListMapper<Stock, Long> {
}
