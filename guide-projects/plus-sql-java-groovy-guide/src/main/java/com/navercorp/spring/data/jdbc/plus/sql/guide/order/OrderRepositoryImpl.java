/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2021 NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import java.util.List;

import com.navercorp.spring.data.jdbc.plus.sql.guide.order.sql.OrderSql;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;
import com.navercorp.spring.data.jdbc.plus.sql.support.trait.SingleValueSelectTrait;

/**
 * @author Myeonghyeon Lee
 */
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
			.addValue("purchaserNo", criteria.getPurchaserNo())
			.addValue("status", criteria.getStatus().name()));
	}

	@Override
	public long countByPurchaserNo(String purchaserNo) {
		String sql = this.sqls.countByPurchaserNo();
		return selectSingleValue(sql, mapParameterSource()
				.addValue("purchaserNo", purchaserNo),
			Long.class);
	}

	@Override
	public List<Order> findByPurchaserNoAndStatusAndPrice(OrderCriteria criteria, Long price) {
		String sql = this.sqls.selectByPurchaserNoAndStatusAndPrice();
		return find(sql, compositeSqlParameterSource(
			beanParameterSource("criteria.", criteria),
			mapParameterSource()
				.addValue("price", price)
		));
	}
}
