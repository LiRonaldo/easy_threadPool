package com.yuxiang.li;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/***
 * @ClassName: ThreadPool
 * @Description:手撸简易版线程池
 * @Auther: liyx
 */
public class ThreadPool {
    //仓库
    private BlockingQueue<Runnable> blockingQueue;
    //线程集合
    private List<Thread> workers;

    private static class Worker extends Thread {
        private ThreadPool threadPool;

        public Worker(ThreadPool threadPool) {
            this.threadPool = threadPool;
        }

        @Override
        public void run() {
            while (this.threadPool.isWorking || this.threadPool.blockingQueue.size() > 0) {
                Runnable task = null;
                try {
                    //如果没有关掉线程池 从队列里拿任务的时候要用阻塞的方法
                    if (this.threadPool.isWorking) {
                        task = this.threadPool.blockingQueue.take();
                    } else {
                        //如果线程池关掉了把仓库剩余的任务取出来，用非阻塞的方法
                        task = this.threadPool.blockingQueue.poll();
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (task != null) {
                    task.run();
                    System.out.println("线程：" + Thread.currentThread().getName() + "执行完毕");
                }
            }
        }
    }

    public ThreadPool(int poolSize, int taskSize) {
        if (poolSize <= 0 || taskSize <= 0) {
            throw new IllegalArgumentException("非法参数");
        }
        this.blockingQueue = new LinkedBlockingQueue<>(taskSize);
        this.workers = Collections.synchronizedList(new ArrayList<>());
        for (int i = 0; i < poolSize; i++) {
            Worker worker = new Worker(this);
            //准备就绪
            worker.start();
            //添加进线程集合
            workers.add(worker);
        }
    }

    //往仓库里放线程，不阻塞，利用blockingQueue的方法
    public boolean submit(Runnable task) {
        if (this.isWorking) {
            return this.blockingQueue.offer(task);
        } else {
            return false;
        }
    }

    //放香菜阻塞方法
    public void execute(Runnable task) {
        try {
            if (this.isWorking) {
                this.blockingQueue.put(task);
            }
        } catch (InterruptedException e) {
            System.out.println("放线程阻塞方法报错");
        }
    }

    public volatile boolean isWorking = true;

    //关闭线程池
    public void shutdown() {
        this.isWorking = false;
        for (Thread thread : workers) {
            if (thread.getState().equals(Thread.State.BLOCKED) || thread.getState().equals(Thread.State.WAITING)) {
                thread.interrupt();
            }
        }
    }

    //测试
    public static void main(String[] args) {
        //初始化
        ThreadPool threadPool = new ThreadPool(4, 8);
        for (int i = 0; i < 8; i++) {
            threadPool.submit(new Runnable() {
                @Override
                public void run() {
                    System.out.println("线程放到仓库中");
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
        //关闭线程池
        threadPool.shutdown();
    }
}



