package org.snaker.engine;

import java.util.List;

import org.snaker.engine.entity.HistoryTask;
import org.snaker.engine.model.PageModel;

public interface IMgmtHistoryTask {

	Object queryHistoryTaskByPage(PageModel pageModel, HistoryTask queryModel);

	HistoryTask find(String id);

	void save(HistoryTask historyTaskdb);

	void delete(String id);
	
	List<HistoryTask> getHistoryTask(String orderId);

}
