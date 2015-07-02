package org.snaker.engine.core;

import java.util.List;

import org.snaker.engine.IMgmtHistoryTask;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.RepoHistoryTask;
import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.helper.QueryUtil;
import org.snaker.engine.model.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MgmtHistoryTask implements IMgmtHistoryTask {
	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	RepoHistoryTask repoHistoryTask;
	/**
	 * 根据参与者分页查询已完成的历史任务
	 * @param page 分页对象
	 * @param filter 查询过滤器
	 * @return List<HistoryTask> 历史任务集合
	 */
	public List<HistoryTask> getHistoryTasks(Pageable pageable, QueryFilter queryFilter){
		Query query = QueryUtil.instance().fillQueryBean(queryFilter).build();
		return mongoTemplate.find(query.with(pageable), HistoryTask.class);
	}
	@Override
	public Object queryHistoryTaskByPage(PageModel pageModel, HistoryTask queryModel) {
		Query query = QueryUtil.instance().fillQueryBean(queryModel).build();
		return mongoTemplate.find(query.with(pageModel.bePageable()), HistoryTask.class);
	}
	@Override
	public HistoryTask find(String id) {
		return repoHistoryTask.findOne(id);
	}
	@Override
	public void save(HistoryTask historyTaskdb) {
		repoHistoryTask.save(historyTaskdb);
	}
	@Override
	public void delete(String id) {
		repoHistoryTask.delete(id);
	}
	@Override
	public List<HistoryTask> getHistoryTask(String orderId) {
		return repoHistoryTask.findByOrderId(orderId);
	}
	
}
