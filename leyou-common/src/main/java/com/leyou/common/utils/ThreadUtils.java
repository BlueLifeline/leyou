package com.leyou.common.utils;

import java.util.concurrent.*;

/**
 * @Feature: 线程工具
 */
public class ThreadUtils {

    private static final ExecutorService es = Executors.newFixedThreadPool(10);

    public static void execute(Runnable runnable){
        es.submit(runnable);
    }
}
