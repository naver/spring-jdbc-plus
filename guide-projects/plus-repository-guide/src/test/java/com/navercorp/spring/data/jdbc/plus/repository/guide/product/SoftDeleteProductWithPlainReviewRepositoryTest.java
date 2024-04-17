/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2024 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.repository.guide.product;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author Chanhyeong Cho
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class SoftDeleteProductWithPlainReviewRepositoryTest {
	@Autowired
	private SoftDeleteProductWithPlainReviewRepository sut;

	private final List<SoftDeleteProductWithPlainReview> products = Arrays.asList(
		SoftDeleteProductWithPlainReview.builder()
			.productName("product1")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.reviews(
				Set.of(
					PlainReview.builder()
						.contents("product1 - review1")
						.createdAt(Instant.now())
						.lastModifiedAt(Instant.now())
						.build(),
					PlainReview.builder()
						.contents("product1 - review2")
						.createdAt(Instant.now())
						.lastModifiedAt(Instant.now())
						.build()
				)
			)
			.build(),
		SoftDeleteProductWithPlainReview.builder()
			.productName("product2")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.reviews(
				Set.of(
					PlainReview.builder()
						.contents("product2 - review1")
						.createdAt(Instant.now())
						.lastModifiedAt(Instant.now())
						.build()
				)
			)
			.build(),
		SoftDeleteProductWithPlainReview.builder()
			.productName("product3")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.reviews(
				Set.of(
					PlainReview.builder()
						.contents("product3 - review1")
						.createdAt(Instant.now())
						.lastModifiedAt(Instant.now())
						.build()
				)
			)
			.build()
	);

	@Test
	void insertAll() {
		// when
		Iterable<SoftDeleteProductWithPlainReview> actual = this.sut.insertAll(products);

		// then
		then(actual).hasSize(products.size());
		actual.forEach(product -> {
			then(product.getId()).isNotNull();
			then(product.isVisible()).isTrue();
			then(product.getReviews()).isNotEmpty()
				.allSatisfy(
					review -> {
						then(review.getId()).isNotNull();
					}
				);
		});
	}

	@Test
	void deleteById() {
		// given
		SoftDeleteProductWithPlainReview product = this.sut.insert(products.get(0));

		// when
		this.sut.deleteById(product.getId());

		// then
		Optional<SoftDeleteProductWithPlainReview> markAsDeleted = this.sut.findById(product.getId());
		then(markAsDeleted).isNotEmpty()
			.hasValueSatisfying(p -> {
				then(p.isVisible()).isFalse();
				then(p.getReviews()).isEmpty();
			});
	}

	@Test
	void deleteAllByEntities() {
		// given
		Iterable<SoftDeleteProductWithPlainReview> insertedList = this.sut.insertAll(products);
		List<Long> ids = StreamSupport.stream(insertedList.spliterator(), false)
			.map(SoftDeleteProductWithPlainReview::getId)
			.toList();

		// when
		this.sut.deleteAll(insertedList);

		// then
		Iterable<SoftDeleteProductWithPlainReview> markAsDeletedList = this.sut.findAllById(ids);
		then(markAsDeletedList).hasSize(ids.size())
			.allSatisfy(product -> {
					then(product.isVisible()).isFalse();
					then(product.getReviews()).isEmpty();
				}
			);
	}

	@Test
	void deleteAll() {
		// given
		this.sut.insertAll(products);

		// when
		this.sut.deleteAll();

		// then
		Iterable<SoftDeleteProductWithPlainReview> markAsDeletedList = this.sut.findAll();
		then(markAsDeletedList).hasSize(products.size())
			.allSatisfy(product -> {
					then(product.isVisible()).isFalse();
					then(product.getReviews()).isEmpty();
				}
			);
	}
}
