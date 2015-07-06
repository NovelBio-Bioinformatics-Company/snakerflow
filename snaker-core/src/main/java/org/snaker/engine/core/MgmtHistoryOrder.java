package org.snaker.engine.core;

import java.util.List;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.dao.RepoHistoryOrder;
import org.snaker.engine.entity.HistoryOrder;
import org.snaker.engine.helper.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MgmtHistoryOrder {
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	RepoHistoryOrder repoHistoryOrder;
	
	/**
	 * 分页查询历史流程实例
	 * @param page 分页对象
	 * @param filter 查询过滤器
	 * @return List<HistoryOrder> 历史流程实例集合
	 */
	public List<HistoryOrder> getHistoryOrders(Pageable pageable, QueryFilter queryFilter){
		Query query = QueryUtil.instance().fillQueryBean(queryFilter).build();
		return mongoTemplate.find(query.with(pageable), HistoryOrder.class);
	}
	
	public HistoryOrder findOne(String id){
		return repoHistoryOrder.findOne(id);
	}
}
