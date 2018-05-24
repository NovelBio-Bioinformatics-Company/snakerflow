package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.entity.Order;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoOrder extends PagingAndSortingRepository<Order, String>{

	@Query(value="{businessId:?0}")
	List<Order> findByBusinessId(String businessId);
}
