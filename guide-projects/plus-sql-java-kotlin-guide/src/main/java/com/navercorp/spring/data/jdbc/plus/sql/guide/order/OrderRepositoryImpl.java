package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import java.util.List;

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql.OrderSql;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait;

public class OrderRepositoryImpl extends JdbcRepositorySupport<Order>
	implements OrderRepositoryCustom, SingleValueSelectTrait {

	private final OrderSql sqls;

	public OrderRepositoryImpl(EntityJdbcProvider entityJdbcProvider) {
		super(Order.class, entityJdbcProvider);
		this.sqls = sqls(OrderSql::new);
	}

	@Override
	public List<Order> findByPurchaserNo(String purchaserNo) {
		String sql = this.sqls.selectByPurchaserNo();
		return find(sql, mapParameterSource()
			.addValue("purchaserNo", purchaserNo));
	}

	@Override
	public List<Order> search(OrderCriteria criteria) {
		String sql = this.sqls.search(criteria);
		return find(sql, mapParameterSource()
			.addValue("purchaserNo", criteria.purchaserNo)
			.addValue("status", criteria.status.name()));
	}

	@Override
	public long countByPurchaserNo(String purchaserNo) {
		String sql = this.sqls.countByPurchaserNo();
		return selectSingleValue(sql, mapParameterSource()
				.addValue("purchaserNo", purchaserNo),
			Long.class);
	}
}
