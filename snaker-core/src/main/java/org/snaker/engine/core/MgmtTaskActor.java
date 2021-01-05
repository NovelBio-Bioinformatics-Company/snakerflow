package org.snaker.engine.core;

import org.snaker.engine.IMgmtTaskActor;
import org.snaker.engine.access.dao.RepoTaskActor;
import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.helper.PageUtil;
import org.snaker.engine.helper.QueryUtil;
import org.snaker.engine.model.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
public class MgmtTaskActor implements IMgmtTaskActor {

	@Autowired
	MongoTemplate mongoTemplate;
	@Autowired
	RepoTaskActor repoTaskActor;
	
	@Override
	public Object queryTaskActorByPage(PageModel pageModel, TaskActor queryModel) {
		Query query = QueryUtil.instance().fillQueryBean(queryModel).build();
		return PageUtil.changeToEasyuiPage(pageModel, mongoTemplate, query, TaskActor.class);
	}

	@Override
	public TaskActor find(String id) {
		return repoTaskActor.findById(id).orElse(null);
	}

	@Override
	public void save(TaskActor taskActordb) {
		repoTaskActor.save(taskActordb);
	}

	@Override
	public void delete(String id) {
		repoTaskActor.deleteById(id);
	}

}
