package com.glaf.test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HelloServiceImpl implements HelloService {

	private final static Log log = LogFactory.getLog(HelloServiceImpl.class);

	@Transactional
	@Override
	public void sayHello(long timeMillis) {
		long time = System.currentTimeMillis() - timeMillis;
		if (time > 5000) {
			// 超过5秒的打印日志输出
			log.debug("time : " + time);
		}
		try {
			// 模拟业务执行时间为1s
			Thread.sleep(1000);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

}
