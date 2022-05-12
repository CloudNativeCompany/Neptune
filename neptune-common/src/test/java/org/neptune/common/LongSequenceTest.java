package org.neptune.common;

import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * org.neptune.common - LongSequenceTest
 *
 * @author tony-is-coding
 * @date 2022/5/12 18:40
 */
class LongSequenceTest {
    private static final long ONE_MILLION = 1000000;
    private static final long TEN_MILLION = 10000000;

    @Test
    public void testInOneThread() {
        long target = TEN_MILLION;
        LongSequence seq = new LongSequence();
        long start = System.currentTimeMillis();
        for (int i = 0; i < target; i++) {
            seq.next();
        }
        System.out.println(System.currentTimeMillis() - start);
        System.out.println(seq.next() == target + 1);
    }


    @Test
    public void testInMultiThread() throws InterruptedException {
        final long target = ONE_MILLION * 2;
        LongSequence seq = new LongSequence(64);
        long start = System.currentTimeMillis();

//        CountDownLatch latch = new CountDownLatch(8);

        final Runnable runnable = () -> {
            for (int i = 0; i < target; i++) {
                seq.next();
            }
        };

        Thread t1 = new Thread(runnable);
        Thread t2 = new Thread(runnable);
        Thread t3 = new Thread(runnable);
        Thread t4 = new Thread(runnable);
        Thread t5 = new Thread(runnable);
        Thread t6 = new Thread(runnable);
        Thread t7 = new Thread(runnable);
        Thread t8 = new Thread(runnable);

        t1.start();
        t2.start();
        t3.start();
        t4.start();
        t5.start();
        t6.start();
        t7.start();
        t8.start();

        t1.join();
        t2.join();
        t3.join();
        t4.join();
        t5.join();
        t6.join();
        t7.join();
        t8.join();

        System.out.println(System.currentTimeMillis() - start);
        System.out.println(seq.next());
    }


}