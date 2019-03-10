# easy_threadPool
execute方法 源码能大概理解 但是自己表述不明白 所以就引用网上的话
1、如果线程池中的线程数量少于corePoolSize，就创建新的线程来执行新添加的任务
2、如果线程池中的线程数量大于等于corePoolSize，但队列workQueue未满，则将新添加的任务放到workQueue中
3、如果线程池中的线程数量大于等于corePoolSize，且队列workQueue已满，但线程池中的线程数量小于maximumPoolSize，则会创建新的线程来处理被添加的任务
4、如果线程池中的线程数量等于了maximumPoolSize，就用RejectedExecutionHandler来执行拒绝策略
其实源码比这复杂的多 水平有限 
    if (workerCountOf(c) < corePoolSize) {
            if (addWorker(command, true))
                return;
            c = ctl.get();
        }
        if (isRunning(c) && workQueue.offer(command)) {
            int recheck = ctl.get();
            if (! isRunning(recheck) && remove(command))
                reject(command);
            else if (workerCountOf(recheck) == 0)
                addWorker(null, false);
        }
        else if (!addWorker(command, false))
            reject(command);


手撸简易版线程池
生活不止眼前的苟且，还有远处的肮脏。
                            ---阿瞒

