package com.leyou.item.pojo;

import lombok.Data;

import javax.persistence.Id;
import javax.persistence.Table;

/**
 * 商品信息模板表
 */
@Table(name = "tb_specification")
@Data
public class Specification {

    @Id
    private Long categoryId;  //商品类别id
    private String specifications;  //该商品的信息模板id

    @Override
    public String toString() {
        return "Specification{" +
                "categoryId=" + categoryId +
                ", specifications='" + specifications + '\'' +
                '}';
    }
}
