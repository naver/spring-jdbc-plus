package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import java.util.List;

public interface OrderRepositoryCustom {
	List<Order> findByPurchaserNo(String purchaserNo);

	List<Order> search(OrderCriteria criteria);

	long countByPurchaserNo(String purchaserNo);
}
