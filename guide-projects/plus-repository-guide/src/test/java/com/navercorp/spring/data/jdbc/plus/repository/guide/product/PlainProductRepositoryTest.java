package com.navercorp.spring.data.jdbc.plus.repository.guide.product;

import static org.assertj.core.api.BDDAssertions.*;

import java.time.Instant;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.data.relational.core.query.Query;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chanwool Jo
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class PlainProductRepositoryTest {
	@Autowired
	private PlainProductRepository sut;

	private final List<PlainProduct> products = Arrays.asList(
		PlainProduct.builder()
			.id(1L)
			.productName("product1")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.build(),
		PlainProduct.builder()
			.id(2L)
			.productName("product2")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.build()
	);

	@Test
	void streamAll() {
		// given
		sut.insertAll(products);

		// when
		try (Stream<PlainProduct> actual = sut.streamAll()) {
			// then
			then(actual).allSatisfy(it -> {
				then(it.getProductName()).startsWith("product");
			});
		}
	}

	@Test
	void streamAllByQuery() {
		// given
		sut.insertAll(products);
		Query query = Query.query(Criteria.where("productName").is("product1"));

		// when
		try (Stream<PlainProduct> actual = sut.streamAll(query)) {
			// then
			then(actual).allSatisfy(it -> {
				then(it.getProductName()).isEqualTo("product1");
			});
		}
	}

	@Test
	void streamAllWithSorting() {
		// given
		sut.insertAll(products);
		Sort sort = Sort.by(Sort.Order.desc("id"));

		// when
		try (Stream<PlainProduct> actual = sut.streamAll(sort)) {
			// then
			then(actual).isSortedAccordingTo(Comparator.comparing(PlainProduct::getId).reversed());
		}
	}

	@Test
	void streamAllByIds() {
		// given
		sut.insertAll(products);
		Set<Long> ids = Set.of(1L);

		// when
		try (Stream<PlainProduct> actual = sut.streamAllByIds(ids)) {
			// then
			then(actual).allSatisfy(it -> {
				then(it.getId()).isEqualTo(1L);
				then(it.getProductName()).isEqualTo("product1");
			});
		}
	}
}
