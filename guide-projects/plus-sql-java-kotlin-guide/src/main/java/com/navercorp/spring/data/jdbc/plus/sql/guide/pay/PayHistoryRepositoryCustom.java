package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.util.List;

public interface PayHistoryRepositoryCustom {
	List<PayHistory> findByOrderNo(long orderId);
}
