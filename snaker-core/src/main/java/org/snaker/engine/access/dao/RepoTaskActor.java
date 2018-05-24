package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.entity.TaskActor;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoTaskActor extends PagingAndSortingRepository<TaskActor, String> {

	/**
	 * 根据任务id查询所有活动任务参与者集合
	 * @param taskId 活动任务id
	 * @return List<TaskActor> 活动任务参与者集合
	 */
	@Query(value="{taskId:?0}")
	public List<TaskActor> findlsTaskActorsByTaskId(String taskId);
	
	/**
	 * 根据任务参与者,查询所有对象.
	 * @param actorId
	 * @return
	 */
	@Query(value="{actorId:?0}")
	public List<TaskActor> findlsTaskActorsByActorId(String actorId);
}
