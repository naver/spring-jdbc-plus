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

package com.navercorp.spring.data.jdbc.plus.repository.guide.shipping;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jdbc.core.mapping.AggregateReference;

import com.navercorp.spring.data.jdbc.plus.repository.JdbcRepository;
import com.navercorp.spring.data.jdbc.plus.repository.guide.order.Order;

/**
 * @author Myeonghyeon Lee
 */
public interface ShippingRepository extends JdbcRepository<Shipping, UUID> {
	Optional<Shipping> findByOrderId(AggregateReference<Order, Long> order);
}
