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

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.snaker.engine.Completion;
import org.snaker.engine.IMgmtOrder;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.RepoHistoryTask;
import org.snaker.engine.access.dao.RepoCCOrder;
import org.snaker.engine.access.dao.RepoHistoryOrder;
import org.snaker.engine.access.dao.RepoOrder;
import org.snaker.engine.access.dao.RepoTask;
import org.snaker.engine.entity.CCOrder;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.helper.PageUtil;
import org.snaker.engine.helper.QueryUtil;
import org.snaker.engine.helper.SpringFactoryService;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.PageModel;
import org.snaker.engine.model.ProcessModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

/**
 * 流程实例业务类
 * @author yuqs
 * @since 1.0
 */
@Component
public class MgmtOrder extends MgmtAccess implements IMgmtOrder {
	
	@Autowired
	RepoOrder repoOrder;
	@Autowired
	RepoCCOrder repoCCOrder;
	@Autowired
	RepoHistoryOrder repoHistoryOrder;
	@Autowired
	RepoTask repoTask;
	@Autowired
	RepoHistoryTask repoHistoryTask;
	@Autowired
	MgmtTask taskService;
	@Autowired
	MgmtHistoryTask mgmtHistoryTask;
	@Autowired
	MongoTemplate mongoTemplate;
	
	/**
	 * 创建活动实例
	 * @see org.snaker.engine.core.MgmtOrder#createOrder(Process, String, Map, String, String)
	 */
	public Order createOrder(Process process, String operator, Map<String, Object> args) {
		return createOrder(process, operator, args, null, null);
	}
	
	/**
	 * 创建活动实例
	 */
	public Order createOrder(Process process, String operator, Map<String, Object> args, 
			String parentId, String parentNodeName) {
		Order order = new Order();
		order.setId(StringHelper.getPrimaryKey());
		order.setParentId(parentId);
		order.setParentNodeName(parentNodeName);
		order.setCreateTime(DateHelper.getTime());
		order.setLastUpdateTime(order.getCreateTime());
		order.setCreator(operator);
		order.setLastUpdator(order.getCreator());
		order.setProcessId(process.getId());
		ProcessModel model = process.getModel();
		if(model != null && args != null) {
			if(StringHelper.isNotEmpty(model.getExpireTime())) {
				String expireTime = DateHelper.parseTime(args.get(model.getExpireTime()));
				order.setExpireTime(expireTime);
			}
            String orderNo = (String)args.get(SnakerEngine.ID);
            if(StringHelper.isNotEmpty(orderNo)) {
                order.setOrderNo(orderNo);
            } else {
                order.setOrderNo(model.getGenerator().generate(model));
            }
		}

		order.setVariable(JsonHelper.toJson(args));
		saveOrder(order);
		return order;
	}

    /**
     * 向活动实例临时添加全局变量数据
     * @param orderId 实例id
     * @param args 变量数据
     */
    public void addVariable(String orderId, Map<String, Object> args) {
        Order order = repoOrder.findOne(orderId);
        Map<String, Object> data = order.getVariableMap();
        data.putAll(args);
        order.setVariable(JsonHelper.toJson(data));
        repoOrder.save(order);
    }

    /**
	 * 创建实例的抄送
	 */
	public void createCCOrder(String orderId, String creator, String... actorIds) {
		for (String actorId : actorIds) {
			CCOrder ccorder = new CCOrder();
			ccorder.setOrderId(orderId);
			ccorder.setActorId(actorId);
			ccorder.setCreator(creator);
			ccorder.setStatus(STATE_ACTIVE);
			ccorder.setCreateTime(DateHelper.getTime());
			repoCCOrder.save(ccorder);
		}
	}
	
	/**
	 * 流程实例数据会保存至活动实例表、历史实例表
	 */
	public void saveOrder(Order order) {
		HistoryOrder history = new HistoryOrder(order);
		history.setOrderState(STATE_ACTIVE);
		repoOrder.save(order);
		repoHistoryOrder.save(history);
	}
	
	/**
	 * 更新活动实例的last_Updator、last_Update_Time、expire_Time、version、variable
	 */
	public void updateOrder(Order order) {
		repoOrder.save(order);
	}
	
	/**
	 * 更新抄送记录状态为已阅
	 */
	public void updateCCStatus(String orderId, String... actorIds) {
		List<CCOrder> ccorders = repoCCOrder.findByOrderId(orderId);
		AssertHelper.notNull(ccorders);
		List<String> lsOrderids =  Arrays.asList(actorIds);
		
		for (CCOrder ccorder : ccorders) {
			if (!lsOrderids.contains(ccorder.getActorId())) {
				continue;
			}
			ccorder.setStatus(STATE_FINISH);
			ccorder.setFinishTime(DateHelper.getTime());
			repoCCOrder.save(ccorder);
		}
	}
	
	/**
	 * 删除指定的抄送记录
	 */
	public void deleteCCOrder(String orderId, String actorId) {
		List<CCOrder> ccorders = repoCCOrder.findByOrderId(orderId);
		AssertHelper.notNull(ccorders);
		for (CCOrder ccorder : ccorders) {
			if (actorId.equals(ccorder.getActorId())) {
				repoCCOrder.delete(ccorder);
				break;
			}
		}
	}

	/**
	 * 删除活动流程实例数据，更新历史流程实例的状态、结束时间
	 */
	public void complete(String orderId) {
		Order order = repoOrder.findOne(orderId);
		HistoryOrder history = repoHistoryOrder.findOne(orderId);
		history.setOrderState(STATE_FINISH);
		history.setEndTime(DateHelper.getTime());

		repoHistoryOrder.save(history);
		repoOrder.delete(order);
		Completion completion = getCompletion();
		if (completion != null) {
			completion.complete(history);
		}
	}
	
	/**
	 * 强制中止流程实例
	 * @see org.snaker.engine.core.MgmtOrder#terminate(String, String)
	 */
	public void terminate(String orderId) {
		terminate(orderId, null);
	}

	/**
	 * 强制中止活动实例,并强制完成活动任务
	 */
	public void terminate(String orderId, String operator) {
		SnakerEngine engine = SpringFactoryService.getBean(SnakerEngineImpl.class);
		List<Task> tasks = engine.query().getActiveTasks(new QueryFilter().setOrderId(orderId));
		for (Task task : tasks) {
			engine.task().complete(task.getId(), operator);
		}
		Order order = repoOrder.findOne(orderId);
		HistoryOrder history = new HistoryOrder(order);
		history.setOrderState(STATE_TERMINATION);
		history.setEndTime(DateHelper.getTime());

		repoHistoryOrder.save(history);
		repoOrder.delete(order);
		Completion completion = getCompletion();
		if (completion != null) {
			completion.complete(history);
		}
	}

    /**
     * 激活已完成的历史流程实例
     * @param orderId 实例id
     * @return 活动实例对象
     */
    public Order resume(String orderId) {
        HistoryOrder historyOrder = repoHistoryOrder.findOne(orderId);
        Order order = historyOrder.undo();
        repoOrder.save(order);
        historyOrder.setOrderState(STATE_ACTIVE);
        repoHistoryOrder.save(historyOrder);

        SnakerEngine engine = SpringFactoryService.getBean(SnakerEngineImpl.class);
        List<HistoryTask> histTasks = mgmtHistoryTask.getHistoryTasks(null,
                new QueryFilter().setOrderId(orderId));
        if(histTasks != null ) {
            HistoryTask histTask = histTasks.get(0);
            engine.task().resume(histTask.getId(), histTask.getOperator());
        }
        return order;
    }

	/**
	 * 级联删除指定流程实例的所有数据：
	 * 1.wf_order,wf_hist_order
	 * 2.wf_task,wf_hist_task
	 * 3.wf_task_actor,wf_hist_task_actor
	 * 4.wf_cc_order
	 * @param id 实例id
	 */
	public void cascadeRemove(String id) {
		HistoryOrder historyOrder = repoHistoryOrder.findOne(id);
		AssertHelper.notNull(historyOrder);
		List<Task> activeTasks = taskService.getActiveTasks(null, new QueryFilter().setOrderId(id));
		List<HistoryTask> historyTasks = mgmtHistoryTask.getHistoryTasks(null, new QueryFilter().setOrderId(id));
		for(Task task : activeTasks) {
			repoTask.delete(task);
		}
		for(HistoryTask historyTask : historyTasks) {
			repoHistoryTask.delete(historyTask);
		}
		List<CCOrder> ccOrders = repoCCOrder.findByOrderId(id);
		for(CCOrder ccOrder : ccOrders) {
			repoCCOrder.delete(ccOrder);
		}

		Order order = repoOrder.findOne(id);
		repoHistoryOrder.delete(historyOrder);
		if(order != null) {
			repoOrder.delete(order);
		}
	}

	@Override
	public Object queryOrderByPage(PageModel pageModel, Order queryModel) {
		Query query = QueryUtil.instance().fillQueryBean(queryModel).build();
		return PageUtil.changeToEasyuiPage(pageModel, mongoTemplate, query, Order.class, Order.class);
	}

	@Override
	public Order find(String id) {
		return repoOrder.findOne(id);
	}

	@Override
	public void save(Order orderdb) {
		repoOrder.save(orderdb);
	}

	@Override
	public void delete(String id) {
		repoOrder.delete(id);
	}
}
