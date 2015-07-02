package org.snaker.engine.core;

import java.util.List;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Surrogate;
import org.snaker.engine.helper.QueryUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Component;

@Component
public class MgmtSurrogate {
	@Autowired
	MongoTemplate mongoTemplate;

	public List<Surrogate> querySurrogate(Pageable pageable, QueryFilter queryFilter) {
		Query query = QueryUtil.instance().fillQueryBean(queryFilter).build();
//		mongoTemplate.findAll(Surrogate.class);
		return mongoTemplate.find(query.with(pageable), Surrogate.class);
	}
}
