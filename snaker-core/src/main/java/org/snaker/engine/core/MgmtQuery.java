/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine.core;

import java.util.List;

import org.snaker.engine.IMgmtQuery;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.RepoHistoryTask;
import org.snaker.engine.access.dao.RepoHistoryOrder;
import org.snaker.engine.access.dao.RepoHistoryTaskActor;
import org.snaker.engine.access.dao.RepoOrder;
import org.snaker.engine.access.dao.RepoTask;
import org.snaker.engine.access.dao.RepoTaskActor;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.HistoryTaskActor;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.entity.WorkItem;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.model.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Component;

/**
 * 查询服务实现类
 * @author yuqs
 * @since 1.0
 */
@Component
public class MgmtQuery extends MgmtAccess implements IMgmtQuery {
	@Autowired
	RepoOrder repoOrder;
	@Autowired
	RepoHistoryOrder repoHistoryOrder;
	@Autowired
	RepoTask repoTask;
	@Autowired
	RepoHistoryTask repoHistoryTask;
	@Autowired
	RepoTaskActor repoTaskActor;
	@Autowired
	RepoHistoryTaskActor repoHistoryTaskActor;
	@Autowired
	MgmtTask taskService;
	
	public Order getOrder(String orderId) {
		return repoOrder.findById(orderId).orElse(null);
	}
	
	public Task getTask(String taskId) {
		return repoTask.findById(taskId).orElse(null);
	}
	
	public String[] getTaskActorsByTaskId(String taskId) {
		List<TaskActor> actors = repoTaskActor.findlsTaskActorsByTaskId(taskId);
		if(actors == null || actors.isEmpty()) return null;
		String[] actorIds = new String[actors.size()];
		for(int i = 0; i < actors.size(); i++) {
			TaskActor ta = actors.get(i);
			actorIds[i] = ta.getActorId();
		}
		return actorIds;
	}
	
	public List<TaskActor> getTaskActorsByActorId(String actorId){
		return repoTaskActor.findlsTaskActorsByActorId(actorId);
	}
	
	public String[] getHistoryTaskActorsByTaskId(String taskId) {
		List<HistoryTaskActor> actors = repoHistoryTaskActor.findHistTaskActorsByTaskId(taskId);
		if(actors == null || actors.isEmpty()) return null;
		String[] actorIds = new String[actors.size()];
		for(int i = 0; i < actors.size(); i++) {
			HistoryTaskActor ta = actors.get(i);
			actorIds[i] = ta.getActorId();
		}
		return actorIds;
	}

	public HistoryOrder getHistOrder(String orderId) {
		return repoHistoryOrder.findById(orderId).orElse(null);
	}

	public HistoryTask getHistTask(String taskId) {
		return repoHistoryTask.findById(taskId).orElse(null);
	}
	
	public List<Task> getActiveTasks(QueryFilter filter) {
		AssertHelper.notNull(filter);
		return taskService.getActiveTasks(null, filter);
	}

	@Override
	public Page<Task> getActiveTasks(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<Order> getActiveOrders(QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<Order> getActiveOrders(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<HistoryOrder> getHistoryOrders(QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<HistoryOrder> getHistoryOrders(PageModel page,
			QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<HistoryTask> getHistoryTasks(QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<HistoryTask> getHistoryTasks(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<WorkItem> getWorkItems(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<HistoryOrder> getCCWorks(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Page<WorkItem> getHistoryWorkItems(PageModel page, QueryFilter filter) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T nativeQueryObject(Class<T> T, String sql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> nativeQueryList(Class<T> T, String sql, Object... args) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> nativeQueryList(PageModel page, Class<T> T, String sql,
			Object... args) {
		// TODO Auto-generated method stub
		return null;
	}
	
//	public Page<Task> getActiveTasks(Page<Task> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getActiveTasks(page, filter);
//	}
//	
//	public Page<Order> getActiveOrders(QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getActiveOrders(null, filter);
//	}
//	
//	public Page<Order> getActiveOrders(Page<Order> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getActiveOrders(page, filter);
//	}
//	
//	public Page<HistoryOrder> getHistoryOrders(QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getHistoryOrders(null, filter);
//	}
//
//	public Page<HistoryOrder> getHistoryOrders(Page<HistoryOrder> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getHistoryOrders(page, filter);
//	}
//
//	public Page<HistoryTask> getHistoryTasks(QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getHistoryTasks(null, filter);
//	}
//
//	public Page<HistoryTask> getHistoryTasks(Page<HistoryTask> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getHistoryTasks(page, filter);
//	}
//	
//	public Page<WorkItem> getWorkItems(Page<WorkItem> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getWorkItems(page, filter);
//	}
//	
//	public Page<HistoryOrder> getCCWorks(Page<HistoryOrder> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getCCWorks(page, filter);
//	}
//
//	public Page<WorkItem> getHistoryWorkItems(Page<WorkItem> page, QueryFilter filter) {
//		AssertHelper.notNull(filter);
//		return access().getHistoryWorkItems(page, filter);
//	}
//
//	public <T> T nativeQueryObject(Class<T> T, String sql, Object... args) {
//		AssertHelper.notEmpty(sql);
//		AssertHelper.notNull(T);
//		return access().queryObject(T, sql, args);
//	}
//
//	public <T> List<T> nativeQueryList(Class<T> T, String sql, Object... args) {
//		AssertHelper.notEmpty(sql);
//		AssertHelper.notNull(T);
//		return access().queryList(T, sql, args);
//	}
//
//	public <T> List<T> nativeQueryList(Page<T> page, Class<T> T, String sql,
//			Object... args) {
//		AssertHelper.notEmpty(sql);
//		AssertHelper.notNull(T);
//		return access().queryList(page, new QueryFilter(), T, sql, args);
//	}
	
	
	
}
