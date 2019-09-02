package com.leyou.item.web;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import com.leyou.item.pojo.Specification;
import com.leyou.item.service.SpecificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("spec")
public class SpecificationController {

    @Autowired
    private SpecificationService specificationService;

    /**
     * 根据商品分类id查询Specification
     * @param id
     * @return
     */
    @GetMapping("/groups/{id}")
    public ResponseEntity<String> querySpecificationByCategoryId(@PathVariable("id") Long id){

        Specification spec = this.specificationService.queryById(id);
        if (spec == null) {
            throw new MyRuntimeException(ExceptionEnum.SPECIFICATION_NOT_fOUND);
        }

        return ResponseEntity.ok(spec.getSpecifications());
    }
}
