package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.util.List;

import org.springframework.data.repository.ListCrudRepository;

import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory.PayHistoryId;

public interface PayHistoryRepository
	extends ListCrudRepository<PayHistory, PayHistoryId>, PayHistoryRepositoryCustom {

	List<PayHistory> findByOrderId(Long orderId);
}
