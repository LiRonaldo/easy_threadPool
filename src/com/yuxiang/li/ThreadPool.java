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
            //添加进线程集合 此处为了省事直接让核心线程全部启动，jdk中不是这样的 jdk是当提交任务的时候才会启动一个线程，并且是当前的工作线程要小雨corepoolsize才会去创建一个新的线程，病启动，加入线程的集合
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
        //此处比较简单,jdk是先去判断当前线程数是否小于corepoolSize 如果小于的话 直接去创建一个新的线程去执行，
        // 如果大于等于的话就放到队列里去（有界队列）:1.如果放进去的线程是非运行状态的话，移除并拒绝，2.如果当前的线程数是0的话 就创建一个非核心线程，
        //如果放不进去：如果当前线程<maximumPoolsize 则创建一个非核心线程去跑 提交的任务，反之 拒绝
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
    //注意 如果队列里还有任务要执行完，关闭的时候从队列里取任务的时候不要用阻塞的方法
    public void shutdown() {
        this.isWorking = false;
        //判断每个线程的状态
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



