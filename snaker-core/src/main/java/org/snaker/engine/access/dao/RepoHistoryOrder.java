package org.snaker.engine.access.dao;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.HistoryOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoHistoryOrder extends PagingAndSortingRepository<HistoryOrder, String> {

//	/**
//	 * 分页查询历史流程实例
//	 * @param page 分页对象
//	 * @param filter 查询过滤器
//	 * @return List<HistoryOrder> 历史流程实例集合
//	 */
//	public Page<HistoryOrder> getHistoryOrders(Pageable pageable, QueryFilter filter);
	
//	/**
//	 * 根据orderId查找
//	 * @return
//	 */
//	public HistoryOrder findByOrderId(String orderId);

}
