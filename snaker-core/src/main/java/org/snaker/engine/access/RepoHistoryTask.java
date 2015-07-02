package org.snaker.engine.access;

import java.util.List;

import org.snaker.engine.entity.HistoryTask;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;


public interface RepoHistoryTask extends PagingAndSortingRepository<HistoryTask, String> {

	@Query(value="{'orderId':?0}")
	List<HistoryTask> findByOrderId(String orderId);
}
