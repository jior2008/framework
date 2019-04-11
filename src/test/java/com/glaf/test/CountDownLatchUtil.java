package com.glaf.test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CountDownLatchUtil {

	@FunctionalInterface
	public interface MyFunctionalInterface {
		void run();
	}

	private CountDownLatch start;
	private CountDownLatch end;
	private int pollSize = 10;

	public CountDownLatchUtil() {
		this(10);
	}

	public CountDownLatchUtil(int pollSize) {
		this.pollSize = pollSize;
		start = new CountDownLatch(1);
		end = new CountDownLatch(pollSize);
	}

	public void latch(MyFunctionalInterface functionalInterface) throws InterruptedException {
		ExecutorService executorService = Executors.newFixedThreadPool(pollSize);
		for (int i = 0; i < pollSize; i++) {
			Runnable run = new Runnable() {
				@Override
				public void run() {
					try {
						start.await();
						functionalInterface.run();
					} catch (InterruptedException ex) {
						ex.printStackTrace();
					} finally {
						end.countDown();
					}
				}
			};
			executorService.submit(run);
		}
		start.countDown();
		end.await();
		executorService.shutdown();
	}
}