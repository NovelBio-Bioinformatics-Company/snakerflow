package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.entity.CCOrder;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoCCOrder extends PagingAndSortingRepository<CCOrder, String> {

	public List<CCOrder> findByOrderId(String orderId);
}
