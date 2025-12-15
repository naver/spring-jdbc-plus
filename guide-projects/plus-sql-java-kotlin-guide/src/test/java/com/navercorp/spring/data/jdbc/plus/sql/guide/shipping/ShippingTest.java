package com.navercorp.spring.data.jdbc.plus.sql.guide.shipping;

import static org.assertj.core.api.BDDAssertions.*;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class ShippingTest {
	@Autowired
	ShippingRepository sut;

	private Shipping sample = Shipping.builder()
		.shippingNo(UUID.randomUUID().toString())
		.orderNo(AggregateReference.to(940329L))
		.senderAddress("any string")
		.receiverAddress("any string")
		.memo("Hello World")
		.build();

	@Test
	void findById() {
		sut.insert(sample);

		var actual = sut.findById(sample.shippingNo());

		then(actual).isPresent();
	}
}
