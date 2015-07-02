package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.entity.HistoryTaskActor;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoHistoryTaskActor extends PagingAndSortingRepository<HistoryTaskActor, String>{

	
	/**
	 * 根据任务id查询所有历史任务参与者集合
	 * @param taskId 历史任务id
	 * @return List<HistoryTaskActor> 历史任务参与者集合
	 */
	public List<HistoryTaskActor> findHistTaskActorsByTaskId(String taskId);
}
