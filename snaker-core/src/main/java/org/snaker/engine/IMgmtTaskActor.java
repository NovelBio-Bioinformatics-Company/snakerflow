package org.snaker.engine;

import org.snaker.engine.entity.TaskActor;
import org.snaker.engine.model.PageModel;

public interface IMgmtTaskActor {

	Object queryTaskActorByPage(PageModel pageModel, TaskActor queryModel);

	TaskActor find(String id);

	void save(TaskActor taskActordb);

	void delete(String id);

}
