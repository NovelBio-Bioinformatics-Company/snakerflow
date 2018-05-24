package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Task;
import org.snaker.engine.model.PageModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoTask extends PagingAndSortingRepository<Task, String> {

	/**
	 * 根据父任务id查询所有子任务
	 * @param parentTaskId 父任务id
	 * @return List<Task> 活动任务集合
	 */
	@Query(value="{parentTaskId:?0}")
	public List<Task> getNextActiveTasks(String parentTaskId);
	
	/**
	 * 根据流程实例id、任务名称获取
	 * @param orderId 流程实例id
	 * @param taskName 任务名称
	 * @param parentTaskId 父任务id
	 * @return List<Task> 活动任务集合
	 */
	@Query(value="{orderId:?0, taskName:?1,parentTaskId:?2 }")
	public List<Task> getNextActiveTasks(String orderId, String taskName, String parentTaskId);

	@Query(value="{actorIds:{$in: ?0}}")
	public List<Task> findTaskByActorIds(Pageable pageable, List<String> lsActorId);
}
