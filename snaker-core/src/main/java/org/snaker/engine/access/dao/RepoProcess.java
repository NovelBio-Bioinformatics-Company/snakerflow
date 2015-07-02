package org.snaker.engine.access.dao;

import java.util.List;

import org.snaker.engine.access.QueryFilter;
import org.snaker.engine.entity.Process;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.PagingAndSortingRepository;



public interface RepoProcess extends PagingAndSortingRepository<Process, String> {

	public Page<Process> findAll(Pageable pageable);
	
	
	@Query(value="{'name':?0}")
	public List<Process> findByName(String name);
}
