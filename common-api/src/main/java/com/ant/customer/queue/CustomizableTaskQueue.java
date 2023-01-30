package com.ant.customer.queue;

import com.sun.istack.internal.Nullable;
import lombok.Data;
import com.ant.customer.executor.CustomizableThreadPoolExecutor;

import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * MIT License
 * <p>
 * Copyright (c) 2019 chenmudu (陈晨)
 * <p>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p>
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * @Author chenchen6
 * @Date: 2020/1/1 18:38
 * @Description: 定制化线程池的定制化任务队列。
 *                目标：修改JDK线程池的入队逻辑。
 */

@Data
public class CustomizableTaskQueue extends LinkedBlockingQueue<Runnable> {

    /**
     * 停止的时候调用.调用停止时仅存在调用线程一个线程.
     */
    private Integer forceRemainingCapacityOnCheck = null;

    /**
     * 所属父线程池。
     */
    private transient volatile CustomizableThreadPoolExecutor parentExecutor;

    /**
     * 不提供无参数的构造器。
     * @param capacity
     */
    public CustomizableTaskQueue(@Nullable Integer capacity) {
        super(capacity);
    }


    /**
     * 强制任务入队。最大限度去执行任务。
     * @param runnableTask      当前要入队的任务。
     * @param timeOut           超时时间。
     * @param unit              超时时间单位。
     * @return {@link LinkedBlockingQueue#offer(java.lang.Object, long, java.util.concurrent.TimeUnit)}
     * @throws InterruptedException
     */
    public boolean forceInsertTaskQueue(Runnable runnableTask, long timeOut, TimeUnit unit) throws InterruptedException {
        Objects.requireNonNull(runnableTask);
        parentExecutorHandler();
        return super.offer(runnableTask, timeOut, unit);
    }

    /**
     * 强制任务入队。最大限度去执行任务
     *
     * @param   runnableTask  当前要入队的任务。
     * @return  {@link LinkedBlockingQueue#offer(java.lang.Object)}
     */
    public boolean forceInsertTaskQueue(Runnable runnableTask) {
        Objects.requireNonNull(runnableTask);
        parentExecutorHandler();
        return super.offer(runnableTask);
    }

    /**
     *  线程池内线程数量达到核心线程数时,不再优先加入队列内,而是优先创建线程至最大线程数。
     *  符合I/O密集型任务的特点。合理且完美的利用CPU的性能。
     * @param   runnableTask  当前任务
     * @return  加入队列是否成功 {@link LinkedBlockingQueue#offer(java.lang.Object)}
     */
    @Override
    public boolean offer(Runnable runnableTask) {
        //代表此队列无父线程池
        if(Objects.isNull(parentExecutor)) {
            super.offer(runnableTask);
        }
        //池内可容纳线程数已到达最大限度。
        if(parentExecutor.getPoolSize() == parentExecutor.getMaximumPoolSize()) {
            return super.offer(runnableTask);
        }
        //存在空闲线程,入队即可。
        if(parentExecutor.getSubmmitedTaskCount().get() <= parentExecutor.getPoolSize()) {
            return super.offer(runnableTask);
        }

        //   coreThreadCount < currentThreadCount < maxThreadCount
        if(parentExecutor.getPoolSize() < parentExecutor.getMaximumPoolSize()) {
            return false;
        }
        return super.offer(runnableTask);
    }

    /**
     * 任务队列所属线程池的判空处理
     */
    private void parentExecutorHandler() {
        if(Objects.isNull(parentExecutor) || parentExecutor.isShutdown()) {
            throw new RejectedExecutionException("current task queue's parent executor is null!");
        }
    }


    /**
     * {@link ThreadPoolExecutor#addWorker(java.lang.Runnable, boolean)}
     * 暂停的时候需要检查当前queue的大小。
     * @return
     */
    @Override
    public int remainingCapacity() {
        if(Objects.nonNull(this.forceRemainingCapacityOnCheck)) {
            return Integer.valueOf(this.forceRemainingCapacityOnCheck);
        }
        return super.remainingCapacity();
    }
}
