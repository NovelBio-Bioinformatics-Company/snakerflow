package org.snaker.engine.access.dao;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Surrogate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoSurrogate extends PagingAndSortingRepository<Surrogate, String> {

	
	Page<Surrogate> findAll(Pageable pageable);
//	@query(value="{'taskType':?0}")
//	Page<Surrogate> findAll(Pageable pageable, QueryFilter filter);
}
