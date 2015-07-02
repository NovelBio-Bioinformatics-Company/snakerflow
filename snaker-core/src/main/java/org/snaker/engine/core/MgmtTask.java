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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.snaker.engine.Assignment;
import org.snaker.engine.AssignmentHandler;
import org.snaker.engine.Completion;
import org.snaker.engine.IMgmtTask;
import org.snaker.engine.IPosition;
import org.snaker.engine.SnakerEngine;
import org.snaker.engine.SnakerException;
import org.snaker.engine.TaskAccessStrategy;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.RepoHistoryTask;
import org.snaker.engine.access.dao.RepoHistoryTaskActor;
import org.snaker.engine.access.dao.RepoOrder;
import org.snaker.engine.access.dao.RepoTask;
import org.snaker.engine.access.dao.RepoTaskActor;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.entity.HistoryTaskActor;
import org.snaker.engine.entity.Order;
import org.snaker.engine.entity.Process;
import org.snaker.engine.entity.Task;
import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.JsonHelper;
import org.snaker.engine.helper.PageUtil;
import org.snaker.engine.helper.QueryUtil;
import org.snaker.engine.helper.SpringFactoryService;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.impl.GeneralAccessStrategy;
import org.snaker.engine.model.CustomModel;
import org.snaker.engine.model.NodeModel;
import org.snaker.engine.model.PageModel;
import org.snaker.engine.model.ProcessModel;
import org.snaker.engine.model.TaskModel;
import org.snaker.engine.model.TaskModel.PerformType;
import org.snaker.engine.model.TaskModel.TaskType;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;

/**
 * 任务执行业务类
 * @author yuqs
 * @since 1.0
 */
@Component
public class MgmtTask extends MgmtAccess implements IMgmtTask {
	private static final String START = "start";

	//访问策略接口
	private TaskAccessStrategy strategy = null;
	/**
	 * 完成指定任务
	 */
	public Task complete(String taskId) {
		return complete(taskId, null, null);
	}

	/**
	 * 完成指定任务
	 */
	public Task complete(String taskId, String operator) {
		return complete(taskId, operator, null);
	}
	
	@Autowired
	RepoTask repoTask;
	@Autowired
	RepoHistoryTask repoHistoryTask;
	@Autowired
	RepoTaskActor repoTaskActor;
	@Autowired
	RepoHistoryTaskActor repoHistoryTaskActor;
	@Autowired
	RepoOrder repoOrder;
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	IPosition domainPositon;
	
	/**
	 * 完成指定任务
	 * 该方法仅仅结束活动任务，并不能驱动流程继续执行
	 * @see SnakerEngineImpl#executeTask(String, String, java.util.Map)
	 */
	public Task complete(String taskId, String operator, Map<String, Object> args) {
		Task task = repoTask.findOne(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		task.setVariable(JSON.toJSONString(args));
		if(!isAllowed(task, operator)) {
			throw new SnakerException("当前参与者[" + operator + "]不允许执行任务[taskId=" + taskId + "]");
		}
		HistoryTask history = new HistoryTask(task);
		history.setFinishTime(DateHelper.getTime());
		history.setTaskState(STATE_FINISH);
		history.setOperator(operator);
		if(history.getActorIds() == null) {
			List<TaskActor> actors = repoTaskActor.findlsTaskActorsByTaskId(task.getId());
			Set<String> setActorIds = new HashSet<>();
			for(int i = 0; i < actors.size(); i++) {
				setActorIds.add(actors.get(i).getActorId());
			}
			history.setActorIds(setActorIds);
		}
		repoHistoryTask.save(history);
		repoTask.delete(task);
		
		//add by fans.fan 20150616 增加对任务参与者的处理.
		List<TaskActor> lsTaskActor = repoTaskActor.findlsTaskActorsByTaskId(taskId);
		for (TaskActor taskActor : lsTaskActor) {
			HistoryTaskActor histHistoryTaskActor = new HistoryTaskActor();
			BeanUtils.copyProperties(taskActor, histHistoryTaskActor);
			repoHistoryTaskActor.save(histHistoryTaskActor);
			repoTaskActor.delete(taskActor);
		}
		//end by fans.fan 
		
      Completion completion = getCompletion();
      if(completion != null) {
          completion.complete(history);
        }
		return task;
	}

	/**
	 * 更新任务对象的finish_Time、operator、expire_Time、version、variable
	 * @param task 任务对象
	 */
	public void updateTask(Task task) {
		repoTask.save(task);
	}

	/**
	 * 任务历史记录方法
	 * @param execution 执行对象
	 * @param model 自定义节点模型
	 * @return 历史任务对象
	 */
	public HistoryTask history(Execution execution, CustomModel model) {
		HistoryTask historyTask = new HistoryTask();
		historyTask.setId(StringHelper.getPrimaryKey());
		historyTask.setOrderId(execution.getOrder().getId());
		String currentTime = DateHelper.getTime();
		historyTask.setCreateTime(currentTime);
		historyTask.setFinishTime(currentTime);
		historyTask.setDisplayName(model.getDisplayName());
		historyTask.setTaskName(model.getName());
		historyTask.setTaskState(STATE_FINISH);
		historyTask.setTaskType(TaskType.Record.ordinal());
		historyTask.setParentTaskId(execution.getTask() == null ?
				START : execution.getTask().getId());
		historyTask.setVariable(JsonHelper.toJson(execution.getArgs()));
		repoHistoryTask.save(historyTask);
		return historyTask;
	}
	
	/**
	 * 提取指定任务，设置完成时间及操作人，状态不改变
	 */
	public Task take(String taskId, String operator) {
		Task task = repoTask.findOne(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(!isAllowed(task, operator)) {
			throw new SnakerException("当前参与者[" + operator + "]不允许提取任务[taskId=" + taskId + "]");
		}
		task.setOperator(operator);
		task.setFinishTime(DateHelper.getTime());
		repoTask.save(task);
		return task;
	}

    /**
     * 唤醒指定的历史任务
     */
    public Task resume(String taskId, String operator) {
        HistoryTask histTask = repoHistoryTask.findOne(taskId);
        AssertHelper.notNull(histTask, "指定的历史任务[id=" + taskId + "]不存在");
        boolean isAllowed = true;
        if(StringHelper.isNotEmpty(histTask.getOperator())) {
            isAllowed = histTask.getOperator().equals(operator);
        }
        if(isAllowed) {
            Task task = histTask.undoTask();
            task.setId(StringHelper.getPrimaryKey());
            task.setCreateTime(DateHelper.getTime());
            repoTask.save(task);
            assignTask(task.getId(), task.getOperator());
            return task;
        } else {
            throw new SnakerException("当前参与者[" + operator + "]不允许唤醒历史任务[taskId=" + taskId + "]");
        }
    }
	
	/**
	 * 向指定任务添加参与者
	 */
	public void addTaskActor(String taskId, String... actors) {
		addTaskActor(taskId, null, actors);
	}
	
	/**
	 * 向指定任务添加参与者
	 * 该方法根据performType类型判断是否需要创建新的活动任务
	 */
	public void addTaskActor(String taskId, Integer performType, String... actors) {
		Task task = repoTask.findOne(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(!task.isMajor()) return;
		if(performType == null) performType = task.getPerformType();
		if(performType == null) performType = 0;
		switch(performType) {
		case 0:
			assignTask(task.getId(), actors);
			Map<String, Object> data = task.getVariableMap();
			String oldActor = (String)data.get(Task.KEY_ACTOR);
			data.put(Task.KEY_ACTOR, oldActor + "," + StringHelper.getStringByArray(actors));
			task.setVariable(JsonHelper.toJson(data));
			repoTask.save(task);
			break;
		case 1:
			try {
				for(String actor : actors) {
					Task newTask = (Task)task.clone();
					newTask.setId(StringHelper.getPrimaryKey());
					newTask.setCreateTime(DateHelper.getTime());
					newTask.setOperator(actor);
					Map<String, Object> taskData = task.getVariableMap();
					taskData.put(Task.KEY_ACTOR, actor);
					task.setVariable(JsonHelper.toJson(taskData));
					repoTask.save(newTask);
					assignTask(newTask.getId(), actor);
				}
			} catch(CloneNotSupportedException ex) {
				throw new SnakerException("任务对象不支持复制", ex.getCause());
			}
			break;
		default :
			break;
		}
	}
	
	/**
	 * 向指定任务移除参与者
	 */
	public void removeTaskActor(String taskId, String... actors) {
		Task task = repoTask.findOne(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		if(actors == null || actors.length == 0) return;
		if(task.isMajor()) {
			List<TaskActor> lstaskActors = repoTaskActor.findlsTaskActorsByTaskId(taskId);
			List<String> lsactors = Arrays.asList(actors);
			for (TaskActor taskActor : lstaskActors) {
				if (lsactors.contains(taskActor.getActorId())) {
					repoTaskActor.delete(taskActor);
				}
			}
			removeTaskActor(task.getId(), actors);
			Map<String, Object> taskData = task.getVariableMap();
			String actorStr = (String)taskData.get(Task.KEY_ACTOR);
			if(StringHelper.isNotEmpty(actorStr)) {
				String[] actorArray = actorStr.split(",");
				StringBuilder newActor = new StringBuilder(actorStr.length());
				boolean isMatch;
				for(String actor : actorArray) {
					isMatch = false;
					if(StringHelper.isEmpty(actor)) continue;
					for(String removeActor : actors) {
						if(actor.equals(removeActor)) {
							isMatch = true;
							break;
						}
					}
					if(isMatch) continue;
					newActor.append(actor).append(",");
				}
				newActor.deleteCharAt(newActor.length() - 1);
				taskData.put(Task.KEY_ACTOR, newActor.toString());
				task.setVariable(JsonHelper.toJson(taskData));
				repoTask.save(task);
			}
		}
	}
	
	/**
	 * 撤回指定的任务
	 */
	public Task withdrawTask(String taskId, String operator) {
		HistoryTask hist = repoHistoryTask.findOne(taskId);
		AssertHelper.notNull(hist, "指定的历史任务[id=" + taskId + "]不存在");
		List<Task> tasks;
		if(hist.isPerformAny()) {
			tasks = repoTask.getNextActiveTasks(hist.getId());
		} else {
			tasks = repoTask.getNextActiveTasks(hist.getOrderId(), 
					hist.getTaskName(), hist.getParentTaskId());
		}
		if(tasks == null || tasks.isEmpty()) {
			throw new SnakerException("后续活动任务已完成或不存在，无法撤回.");
		}
		for(Task task : tasks) {
			repoTask.delete(task);
		}
		
		Task task = hist.undoTask();
		task.setId(StringHelper.getPrimaryKey());
		task.setCreateTime(DateHelper.getTime());
		repoTask.save(task);
		assignTask(task.getId(), task.getOperator());
		return task;
	}
	
	/**
	 * 驳回任务
	 */
	public Task rejectTask(ProcessModel model, Task currentTask) {
		String parentTaskId = currentTask.getParentTaskId();
		if(StringHelper.isEmpty(parentTaskId) || parentTaskId.equals(START)) {
			throw new SnakerException("上一步任务ID为空，无法驳回至上一步处理");
		}
		NodeModel current = model.getNode(currentTask.getTaskName());
		HistoryTask history = repoHistoryTask.findOne(parentTaskId);
		NodeModel parent = model.getNode(history.getTaskName());
		if(!NodeModel.canRejected(current, parent)) {
			throw new SnakerException("无法驳回至上一步处理，请确认上一步骤并非fork、join、suprocess以及会签任务");
		}

		Task task = history.undoTask();
		task.setId(StringHelper.getPrimaryKey());
		task.setCreateTime(DateHelper.getTime());
		task.setOperator(history.getOperator());
		repoTask.save(task);
		assignTask(task.getId(), task.getOperator());
		return task;
	}

	/**
	 * 对指定的任务分配参与者。参与者可以为用户、部门、角色
	 * @param taskId 任务id
	 * @param actorIds 参与者id集合
	 */
	private void assignTask(String taskId, String... actorIds) {
		if(actorIds == null || actorIds.length == 0) return;
		for(String actorId : actorIds) {
			//修复当actorId为null的bug
			if(StringHelper.isEmpty(actorId)) continue;
			TaskActor taskActor = new TaskActor();
			taskActor.setTaskId(taskId);
			taskActor.setActorId(actorId);
			repoTaskActor.save(taskActor);
		}
	}
	
	/**
	 * 根据已有任务、任务类型、参与者创建新的任务
	 * 适用于转派，动态协办处理
	 */
	public List<Task> createNewTask(String taskId, int taskType, String... actors) {
		Task task = repoTask.findOne(taskId);
		AssertHelper.notNull(task, "指定的任务[id=" + taskId + "]不存在");
		List<Task> tasks = new ArrayList<Task>();
		try {
			Task newTask = (Task)task.clone();
			newTask.setTaskType(taskType);
			newTask.setCreateTime(DateHelper.getTime());
			newTask.setParentTaskId(taskId);
			tasks.add(saveTask(newTask, actors));
		} catch (CloneNotSupportedException e) {
			throw new SnakerException("任务对象不支持复制", e.getCause());
		}
		return tasks;
	}

    /**
     * 获取任务模型
     * @param taskId 任务id
     * @return TaskModel
     */
    public TaskModel getTaskModel(String taskId) {
        Task task = repoTask.findOne(taskId);
        AssertHelper.notNull(task);
        Order order = repoOrder.findOne(task.getOrderId());
        AssertHelper.notNull(order);
        Process process = SpringFactoryService.getBean(MgmtProcess.class).getProcessById(order.getProcessId());
        ProcessModel model = process.getModel();
        NodeModel nodeModel = model.getNode(task.getTaskName());
        AssertHelper.notNull(nodeModel, "任务id无法找到节点模型.");
        if(nodeModel instanceof TaskModel) {
            return (TaskModel)nodeModel;
        } else {
            throw new IllegalArgumentException("任务id找到的节点模型不匹配");
        }
    }

    /**
	 * 由DBAccess实现类创建task，并根据model类型决定是否分配参与者
	 * <b>这个方法非常重要!!! fans.fan添加该注释 </b>
	 * @param taskModel 模型
	 * @param execution 执行对象
	 * @return List<Task> 任务列表
	 */
	public List<Task> createTask(TaskModel taskModel, Execution execution) {
		List<Task> tasks = new ArrayList<Task>();
		
		Map<String, Object> args = execution.getArgs();
		if(args == null) args = new HashMap<String, Object>();
		Date expireDate = DateHelper.processTime(args, taskModel.getExpireTime());
		Date remindDate = DateHelper.processTime(args, taskModel.getReminderTime());
		String form = (String)args.get(taskModel.getForm());
		String actionUrl = StringHelper.isEmpty(form) ? taskModel.getForm() : form;
		
		String[] actors = getTaskActors(taskModel, execution);
		args.put(Task.KEY_ACTOR, StringHelper.getStringByArray(actors));
		Task task = createTaskBase(taskModel, execution);
		task.setActionUrl(actionUrl);
		task.setExpireDate(expireDate);
		task.setExpireTime(DateHelper.parseTime(expireDate));
		task.setVariable(JsonHelper.toJson(args));
		
		if(taskModel.isPerformAny()) {
			//任务执行方式为参与者中任何一个执行即可驱动流程继续流转，该方法只产生一个task
			task = saveTask(task, actors);
			task.setRemindDate(remindDate);
			tasks.add(task);
		} else if(taskModel.isPerformAll()){
			//任务执行方式为参与者中每个都要执行完才可驱动流程继续流转，该方法根据参与者个数产生对应的task数量
			for(String actor : actors) {
                Task singleTask;
                try {
                    singleTask = (Task) task.clone();
                } catch (CloneNotSupportedException e) {
                    singleTask = task;
                }
                singleTask = saveTask(singleTask, actor);
                singleTask.setRemindDate(remindDate);
                tasks.add(singleTask);
			}
		}
		return tasks;
	}
	
	/**
	 * 根据模型、执行对象、任务类型构建基本的task对象
	 * @param model 模型
	 * @param execution 执行对象
	 * @return Task任务对象
	 */
	private Task createTaskBase(TaskModel model, Execution execution) {
		Task task = new Task();
		task.setOrderId(execution.getOrder().getId());
		task.setTaskName(model.getName());
		task.setDisplayName(model.getDisplayName());
		task.setCreateTime(DateHelper.getTime());
		if(model.isMajor()) {
			task.setTaskType(TaskType.Major.ordinal());
		} else {
			task.setTaskType(TaskType.Aidant.ordinal());
		}
		task.setParentTaskId(execution.getTask() == null ? 
				START : execution.getTask().getId());
		task.setModel(model);
		return task;
	}
	
	/**
	 * 由DBAccess实现类持久化task对象
	 */
	private Task saveTask(Task task, String... actors) {
		task.setId(StringHelper.getPrimaryKey());
		task.setPerformType(PerformType.ANY.ordinal());
		Set<String> setActorIds = new HashSet<>();
		Collections.addAll(setActorIds, actors);
		task.setActorIds(setActorIds);
		repoTask.save(task);
		assignTask(task.getId(), actors);
		return task;
	}

	/**
	 * 根据Task模型的assignee、assignmentHandler属性以及运行时数据，确定参与者
	 * @param model 模型
	 * @param execution 执行对象
	 * @return 参与者数组
	 */
	private String[] getTaskActors(TaskModel model, Execution execution) {
		Object assigneeObject = null;
        AssignmentHandler handler = model.getAssignmentHandlerObject();
		if(StringHelper.isNotEmpty(model.getAssignee())) {
			assigneeObject = execution.getArgs().get(model.getAssignee());
		} else if(handler != null) {
            if(handler instanceof Assignment) {
                assigneeObject = ((Assignment)handler).assign(model, execution);
            } else {
                assigneeObject = handler.assign(execution);
            }
		}
		String[] taskActors = getTaskActors(assigneeObject == null ? model.getAssignee() : assigneeObject);
		//add by fans.fan 
		return domainPositon.findEmployeeIdsByPositonFnumbers(taskActors);
		//end by fans.fan
	}

	/**
	 * 根据taskmodel指定的assignee属性，从args中取值
	 * 将取到的值处理为String[]类型。
	 * @param actors 参与者对象
	 * @return 参与者数组
	 */
	private String[] getTaskActors(Object actors) {
		if(actors == null) return null;
		String[] results;
		if(actors instanceof String) {
			//如果值为字符串类型，则使用逗号,分隔
			return ((String)actors).split(",");
        } else if(actors instanceof List){
            //jackson会把stirng[]转成arraylist，此处增加arraylist的逻辑判断,by 红豆冰沙2014.11.21
			List<?> list = (List)actors;
			results = new String[list.size()];
			for(int i = 0; i < list.size(); i++) {
				results[i] = (String)list.get(i);
			}
            return results;
		} else if(actors instanceof Long) {
			//如果为Long类型，则返回1个元素的String[]
			results = new String[1];
			results[0] = String.valueOf((Long)actors);
			return results;
		} else if(actors instanceof Integer) {
			//如果为Integer类型，则返回1个元素的String[]
			results = new String[1];
			results[0] = String.valueOf((Integer)actors);
			return results;
		} else if(actors instanceof String[]) {
			//如果为String[]类型，则直接返回
			return (String[])actors;
		} else {
			//其它类型，抛出不支持的类型异常
			throw new SnakerException("任务参与者对象[" + actors + "]类型不支持."
					+ "合法参数示例:Long,Integer,new String[]{},'10000,20000',List<String>");
		}
	}
	
	/**
	 * 判断当前操作人operator是否允许执行taskId指定的任务
	 */
	public boolean isAllowed(Task task, String operator) {
		if(StringHelper.isNotEmpty(operator)) {
			if(SnakerEngine.ADMIN.equalsIgnoreCase(operator)
					|| SnakerEngine.AUTO.equalsIgnoreCase(operator)) {
				return true;
			}
			if(StringHelper.isNotEmpty(task.getOperator())) {
				return operator.equals(task.getOperator());
			}
		}
		List<TaskActor> actors = repoTaskActor.findlsTaskActorsByTaskId(task.getId());
		if(actors == null || actors.isEmpty()) return true;
		return !StringHelper.isEmpty(operator)
				&& getStrategy().isAllowed(operator, actors);
	}

	public void setStrategy(TaskAccessStrategy strategy) {
		this.strategy = strategy;
	}

	public TaskAccessStrategy getStrategy() {
        if(strategy != null) {
            return strategy;
        }
		strategy = SpringFactoryService.getBean(GeneralAccessStrategy.class);
		return strategy;
	}
	
	/**
	 * 分页查询活动任务列表
	 * @param page 分页对象
	 * @param filter 查询过滤器
	 * @return List<Task> 活动任务集合
	 */
	public List<Task> getActiveTasks(Pageable pageable, QueryFilter queryFilter){
		Query query = QueryUtil.instance().fillQueryBean(queryFilter).build();
		return mongoTemplate.find(query.with(pageable), Task.class);
	}

	@Override
	public List<Task> getlsTaskByActorIds(PageModel pageModel,String actorId) {
		pageModel.setSort("createTime");
		List lsActorId = new ArrayList<>();
		lsActorId.add(actorId);
		return repoTask.findTaskByActorIds(pageModel.bePageable(),lsActorId);
	}

	@Override
	public Object queryTaskByPage(PageModel pageModel, Task queryModel) {
		Query query = QueryUtil.instance().fillQueryBean(queryModel).build();
		return PageUtil.changeToEasyuiPage(pageModel, mongoTemplate, query, Task.class);
	}

	@Override
	public Task find(String id) {
		return repoTask.findOne(id);
	}

	@Override
	public void save(Task taskdb) {
		repoTask.save(taskdb);
	}

	@Override
	public void delete(String id) {
		repoTask.delete(id);
	}
}
