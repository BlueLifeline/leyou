package com.leyou.common.advice;

import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.exception.MyRuntimeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice(basePackages = "com.leyou")
public class MyExceptionAdvice {

    /**
     * 统一异常处理
     * @param ex
     * @return
     */
    @ExceptionHandler(MyRuntimeException.class)
    public ResponseEntity<String> runtimeHandler(MyRuntimeException ex){
        System.out.println("执行了异常处理");
        ExceptionEnum enums = ex.getExceptionEnum();
        return ResponseEntity.status(enums.getCode()).body(enums.getMessage());
    }

}
