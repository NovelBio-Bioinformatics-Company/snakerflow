/* Copyright 2013-2015 www.snakerflow.com.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.snaker.engine.core;

import java.util.List;

import org.snaker.engine.IMgmtManager;
import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.access.dao.RepoSurrogate;
import org.snaker.engine.entity.Surrogate;
import org.snaker.engine.helper.AssertHelper;
import org.snaker.engine.helper.DateHelper;
import org.snaker.engine.helper.StringHelper;
import org.snaker.engine.model.PageModel;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

/**
 * 管理服务类
 * @author yuqs
 * @since 1.4
 */
@Component
public class ManagerService extends MgmtAccess implements IMgmtManager {
	@Autowired
	RepoSurrogate repoSurrogate;
	
	@Autowired
	MgmtSurrogate mgmtSurrogate;
	
	public void saveOrUpdate(Surrogate surrogate) {
		AssertHelper.notNull(surrogate);
		surrogate.setState(STATE_ACTIVE);
		if(StringHelper.isEmpty(surrogate.getId())) {
			surrogate.setId(StringHelper.getPrimaryKey());
			repoSurrogate.save(surrogate);
		} else {
			repoSurrogate.save(surrogate);
		}
	}

	public void deleteSurrogate(String id) {
		Surrogate surrogate = getSurrogate(id);
		AssertHelper.notNull(surrogate);
		repoSurrogate.delete(surrogate);
	}

	public Surrogate getSurrogate(String id) {
		return repoSurrogate.findOne(id);
	}
	
	public List<Surrogate> getSurrogate(QueryFilter queryFilter) {
		AssertHelper.notNull(queryFilter);
		return mgmtSurrogate.querySurrogate(new PageModel().bePageable(), queryFilter);
//		return repoSurrogate.getSurrogate(new PageModel().bePageable(), filter);
	}

	public List<Surrogate> getSurrogate(Pageable page, QueryFilter queryFilter) {
		AssertHelper.notNull(queryFilter);
		return mgmtSurrogate.querySurrogate(page, queryFilter);
//		return repoSurrogate.getSurrogate(page, filter);
	}
	
	public String getSurrogate(String operator, String processName) {
		AssertHelper.notEmpty(operator);
		QueryFilter filter = new QueryFilter().
				setOperator(operator).
				setOperateTime(DateHelper.getTime());
		if(StringHelper.isNotEmpty(processName)) {
			filter.setName(processName);
		}
		List<Surrogate> surrogates = getSurrogate(filter);
		if(surrogates == null || surrogates.isEmpty()) return operator;
		StringBuffer buffer = new StringBuffer(50);
		for(Surrogate surrogate : surrogates) {
			String result = getSurrogate(surrogate.getSurrogateName(), processName);
			buffer.append(result).append(",");
		}
		buffer.deleteCharAt(buffer.length() - 1);
		return buffer.toString();
	}

	@Override
	public List<Surrogate> getSurrogate(PageModel pageModel, QueryFilter queryFilter) {
		return mgmtSurrogate.querySurrogate(pageModel.bePageable(), queryFilter);
	}
}
