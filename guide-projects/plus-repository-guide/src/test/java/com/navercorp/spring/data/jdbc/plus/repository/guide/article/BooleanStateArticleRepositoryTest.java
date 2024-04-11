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

package com.navercorp.spring.data.jdbc.plus.repository.guide.article;

import static org.assertj.core.api.BDDAssertions.then;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
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
public class BooleanStateArticleRepositoryTest {
	@Autowired
	private BooleanStateArticleRepository sut;

	private final List<BooleanStateArticle> articles = Arrays.asList(
		BooleanStateArticle.builder()
			.writerId("userId1")
			.contents("First, Hello, World!")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.build(),
		BooleanStateArticle.builder()
			.writerId("userId2")
			.contents("Second, Hello, World!")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.build(),
		BooleanStateArticle.builder()
			.writerId("userId3")
			.contents("Third, Hello, World!")
			.createdAt(Instant.now())
			.lastModifiedAt(Instant.now())
			.visible(true)
			.build()
	);

	@Test
	void insertAll() {
		// when
		Iterable<BooleanStateArticle> actual = this.sut.insertAll(articles);

		// then
		actual.forEach(order -> {
			then(order.getId()).isNotNull();
			then(order.isVisible()).isTrue();
		});
	}

	@Test
	void update() {
		// given
		BooleanStateArticle article = this.sut.insert(articles.get(0));
		BooleanStateArticle changed = article.toBuilder()
			.contents(article.getContents() + " Changed content")
			.lastModifiedAt(Instant.now())
			.build();

		// when
		BooleanStateArticle actual = this.sut.update(changed);

		// then
		then(actual.getContents())
			.isEqualTo("First, Hello, World! Changed content");
	}

	@Test
	void deleteById() {
		// given
		BooleanStateArticle article = this.sut.insert(articles.get(0));

		// when
		this.sut.deleteById(article.getId());

		// then
		Optional<BooleanStateArticle> markAsDeleted = this.sut.findById(article.getId());
		then(markAsDeleted).isNotEmpty()
			.hasValueSatisfying(it -> then(it.isVisible()).isFalse());
	}

	@Test
	void deleteAllByEntities() {
		// given
		Iterable<BooleanStateArticle> insertedList = this.sut.insertAll(articles);
		List<Long> ids = StreamSupport.stream(insertedList.spliterator(), false)
			.map(BooleanStateArticle::getId)
			.toList();

		// when
		this.sut.deleteAll(insertedList);

		// then
		Iterable<BooleanStateArticle> markAsDeletedList = this.sut.findAllById(ids);
		then(markAsDeletedList).hasSize(ids.size())
			.allSatisfy(it ->
				then(it.isVisible()).isFalse()
			);
	}

	@Test
	void deleteAll() {
		// given
		this.sut.insertAll(articles);

		// when
		this.sut.deleteAll();

		// then
		Iterable<BooleanStateArticle> markAsDeletedList = this.sut.findAll();
		then(markAsDeletedList).hasSize(articles.size())
			.allSatisfy(it ->
				then(it.isVisible()).isFalse()
			);
	}
}
