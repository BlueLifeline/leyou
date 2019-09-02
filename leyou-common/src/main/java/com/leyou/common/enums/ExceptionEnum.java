package com.leyou.common.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 异常枚举类
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
public enum ExceptionEnum {
    CATEGORY_BRAND_NOT_SAVE(500, "品牌分类中间表信息新增失败"),
    BRAND_NOT_SAVE(500, "品牌信息新增失败"),
    BRAND_NOT_FIND(400, "品牌信息未找到"),
    CATEGORY_IS_NULL(400, "商品目录未找到"),

    FILETYPE_NOT_SUPPORT(500, "文件格式不支持"),
    FILE_IS_NULL(500, "文件为空"),
    UPLOAD_FAILED(500, "文件上传失败"),
    SPECIFICATION_NOT_fOUND(400, "商品信息未找到"),

    SPU_INFO_NOT_FOUNT(400, "商品信息未找到");

    private int code;
    private String message;
}
