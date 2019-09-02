package com.leyou.item.pojo;

/**
 * @author li
 * @time 2019/07/28
 * @feature: 商品分类对应的实体，使用通用mapper生成
 */
import lombok.Data;
import tk.mybatis.mapper.annotation.KeySql;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Table(name="tb_category")
@Data
public class Category implements Serializable {
	@Id
	@GeneratedValue(strategy= GenerationType.IDENTITY)  //主键生成策略
	private Long id;
	private String name;
	private Long parentId;
	private Boolean isParent;
	/**
	 * 排序指数，越小越靠前
	 */
	private Integer sort;

	@Override
	public String toString() {
		return "Category{" +
				"id=" + id +
				", name='" + name + '\'' +
				", parentId=" + parentId +
				", isParent=" + isParent +
				", sort=" + sort +
				'}';
	}
}