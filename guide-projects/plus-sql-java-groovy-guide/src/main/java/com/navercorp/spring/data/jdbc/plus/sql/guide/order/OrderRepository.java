package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import org.springframework.data.repository.CrudRepository;

public interface OrderRepository extends CrudRepository<Order, Long>, OrderRepositoryCustom {
}
