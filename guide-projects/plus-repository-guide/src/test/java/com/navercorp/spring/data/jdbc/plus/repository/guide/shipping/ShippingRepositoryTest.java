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

import static org.assertj.core.api.Assertions.*;

import java.util.Optional;
import java.util.UUID;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Myeonghyeon Lee
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class ShippingRepositoryTest {
	@Autowired
	private ShippingRepository sut;

	private Shipping shipping = Shipping.builder()
		.id(UUID.randomUUID())
		.orderId(AggregateReference.to(1L))
		.receiverAddress("green factory")
		.memo("fragile")
		.build();

	@Test
	void insert() {
		Shipping actual = this.sut.insert(shipping);
		assertThat(actual.getId()).isEqualTo(shipping.getId());
	}

	@Test
	void update() {
		// given
		Shipping shipping = this.sut.insert(this.shipping);
		shipping.changeMemo("non-fragile");

		// when
		Shipping actual = this.sut.update(shipping);

		// then
		assertThat(actual.getId()).isEqualTo(shipping.getId());
		assertThat(actual.getMemo()).isEqualTo("non-fragile");
	}

	@Test
	void findById() {
		// given
		Shipping shipping = this.sut.insert(this.shipping);

		// when
		Optional<Shipping> actual = this.sut.findById(shipping.getId());

		// then
		Assertions.assertThat(actual).isNotEmpty();
		assertThat(actual.get().getId()).isEqualTo(shipping.getId());
	}
}
