package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.util.List;

import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.sql.PayHistorySql;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;

public class PayHistoryRepositoryImpl extends JdbcRepositorySupport<PayHistory>
	implements PayHistoryRepositoryCustom {

	private final PayHistorySql sqls;

	public PayHistoryRepositoryImpl(EntityJdbcProvider entityJdbcProvider) {
		super(PayHistory.class, entityJdbcProvider);
		this.sqls = sqls(PayHistorySql::new);
	}

	@Override
	public List<PayHistory> findByOrderNo(long orderId) {
		return find(
			sqls.selectByOrderNo(),
			mapParameterSource()
				.addValue("orderId", orderId)
		);
	}
}
