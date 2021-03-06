/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.glaf.framework.id;

import com.glaf.core.dao.EntityDAO;
import com.glaf.core.id.IdBlock;
import com.glaf.core.id.IdGenerator;
import com.glaf.core.util.DateUtils;
import com.glaf.core.util.StringTools;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

@Service("idGenerator")
@Transactional
public class MyBatisDbIdGenerator implements IdGenerator {
	static class RefreshTask implements Runnable {

		public void run() {
			try {
				lastIdMap.clear();
				nextIdMap.clear();
			} catch (Exception ex) {
				logger.error(ex);
			}
		}

	}

	private final static Log logger = LogFactory.getLog(MyBatisDbIdGenerator.class);

	private static final ConcurrentMap<String, AtomicLong> lastIdMap = new ConcurrentHashMap<>();

	private static final ConcurrentMap<String, AtomicLong> nextIdMap = new ConcurrentHashMap<>();

	private static final ThreadLocal<Map<String, Integer>> threadLocalVaribles = new ThreadLocal<Map<String, Integer>>();

	private static final ScheduledExecutorService scheduledThreadPool = Executors.newSingleThreadScheduledExecutor();

	public static void clear() {
		threadLocalVaribles.remove();
	}

	private volatile EntityDAO entityDAO;

	private volatile long lastId = -1;

	private volatile long nextId = 0;

	public MyBatisDbIdGenerator() {
		logger.info("----------------MyBatis3DbIdGenerator--------------");
		try {
			RefreshTask command = new RefreshTask();
			scheduledThreadPool.scheduleAtFixedRate(command, 60, 10, TimeUnit.SECONDS);
		} catch (Exception ex) {
			logger.error(ex);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized void getNewBlock() {
		IdBlock idBlock = entityDAO.nextDbidBlock();
		this.nextId = idBlock.getNextId();
		this.lastId = idBlock.getLastId();
		if (logger.isDebugEnabled()) {
			logger.debug("----------------NEXTID------------------------");
			logger.debug("nextId:" + nextId);
			logger.debug("lastId:" + lastId);
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized void getNewBlock(String name) {
		IdBlock idBlock = entityDAO.nextDbidBlock(name);
		Long nextId2 = idBlock.getNextId();
		Long lastId2 = idBlock.getLastId();
		if (nextId2 < lastId2) {
			AtomicLong lastId2x = lastIdMap.get(name);
			AtomicLong nextId2x = nextIdMap.get(name);
			if (lastId2x != null && nextId2x != null) {
				lastId2x.set(lastId2);
				nextId2x.set(nextId2);
				lastIdMap.put(name, lastId2x);
				nextIdMap.put(name, nextId2x);
				logger.debug("----------------NEXTID------------------------");
				logger.debug("nextId:" + nextId2x.get());
				logger.debug("lastId:" + lastId2x.get());
			}
		}
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized String getNextId() {
		return Long.toString(this.nextId());
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized String getNextId(String name) {
		return Long.toString(this.nextId(name));
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized String getNextId(String tablename, String idColumn, String createBy) {
		int day = DateUtils.getNowYearMonthDay();
		String idLike = day + "/" + createBy + "-";
		String cacheKey = tablename + "_" + idColumn + "_" + createBy + "_" + day;
		if (threadLocalVaribles.get() != null && threadLocalVaribles.get().get(cacheKey) != null) {
			int maxId = threadLocalVaribles.get().get(cacheKey);
			maxId = maxId + 1;
			threadLocalVaribles.get().put(cacheKey, maxId);
            return idLike + StringTools.getDigit7Id(maxId);
		}
		int maxId = entityDAO.getTableUserMaxId(tablename, idColumn, createBy);
		if (threadLocalVaribles.get() == null) {
			Map<String, Integer> map = new HashMap<String, Integer>();
			map.put(cacheKey, maxId);
			threadLocalVaribles.set(map);
		} else {
			threadLocalVaribles.get().put(cacheKey, maxId);
		}
        return idLike + StringTools.getDigit7Id(maxId);
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized long nextId() {
		if (lastId < nextId) {
			this.getNewBlock();
		}
		return nextId++;
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized long nextId(String name) {
		AtomicLong lastId2 = lastIdMap.get(name);
		AtomicLong nextId2 = nextIdMap.get(name);
		if (lastId2 == null) {
			lastId2 = new AtomicLong(0L);
			lastIdMap.put(name, lastId2);
		}
		if (nextId2 == null) {
			nextId2 = new AtomicLong(1L);
			nextIdMap.put(name, nextId2);
		}
		if (lastId2.get() < nextId2.get()) {
			this.getNewBlock(name);
		}
        return nextId2.incrementAndGet();
	}

	@Transactional(propagation = Propagation.REQUIRED)
	public synchronized long nextId(String tablename, String idColumn) {
		long maxId = entityDAO.getMaxId(tablename, idColumn);
		return maxId + 1;
	}

	@javax.annotation.Resource
	public void setEntityDAO(EntityDAO entityDAO) {
		this.entityDAO = entityDAO;
	}
}
