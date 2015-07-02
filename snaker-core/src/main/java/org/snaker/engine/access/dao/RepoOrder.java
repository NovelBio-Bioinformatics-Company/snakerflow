package org.snaker.engine.access.dao;

import org.snaker.engine.entity.Order;
import org.springframework.data.repository.PagingAndSortingRepository;

public interface RepoOrder extends PagingAndSortingRepository<Order, String>{

}
