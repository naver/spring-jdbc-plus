/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import static com.navercorp.spring.data.jdbc.plus.sql.guide.test.ArbitrarySpec.*;
import static java.util.stream.Collectors.*;
import static org.assertj.core.api.BDDAssertions.*;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Label;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Post;

/**
 * @author Myeonghyeon Lee
 */
@SuppressWarnings("ALL")
@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BoardRepositoryTest {
	@Autowired
	private BoardRepository sut;

	List<Board> boards = fixtureMonkey.giveMe(Board.class, 3);

	/**
	 * Seems related to https://github.com/spring-projects/spring-data-relational/issues/2054
	 */
	@Disabled
	@Test
	void findById() {
		// given
		List<Board> savedBoards = sut.saveAll(boards).stream()
			.map(Board::sort)
			.toList();

		// when
		Optional<Board> actual1 = this.sut.findById(savedBoards.get(0).id());
		Optional<Board> actual2 = this.sut.findById(savedBoards.get(1).id());
		Optional<Board> actual3 = this.sut.findById(savedBoards.get(2).id());

		// then
		then(actual1).isPresent().hasValue(savedBoards.get(0));
		then(actual2).isPresent().hasValue(savedBoards.get(1));
		then(actual3).isPresent().hasValue(savedBoards.get(2));
	}

	@Test
	void findGraphById() {
		// given
		List<Board> savedBoards = sut.saveAll(boards).stream()
			.map(Board::sort)
			.toList();

		// when
		Optional<Board> actual1 = this.sut.findGraphById(savedBoards.get(0).id());
		Optional<Board> actual2 = this.sut.findGraphById(savedBoards.get(1).id());
		Optional<Board> actual3 = this.sut.findGraphById(savedBoards.get(2).id());

		// then
		then(actual1).isPresent().hasValue(savedBoards.get(0));
		then(actual2).isPresent().hasValue(savedBoards.get(1));
		then(actual3).isPresent().hasValue(savedBoards.get(2));
	}

	@Test
	void deleteByName() {
		// given
		Board sample = sut.save(boards.get(0));

		// when
		this.sut.deleteByName(sample.name());

		// then
		then(this.sut.findById(sample.id())).isEmpty();
	}

	@Test
	void findAllGraph() {
		// given
		List<Board> savedBoards = sut.saveAll(boards).stream()
			.map(Board::sort)
			.toList();

		// when
		List<Board> actual = this.sut.findAllGraph();

		// then
		then(actual).isEqualTo(savedBoards);
	}

	@Test
	void findPostDtoByPostId() {
		// given
		List<Board> savedBoards = sut.saveAll(boards).stream()
			.map(Board::sort)
			.toList();

		List<Post> savedPosts = savedBoards.stream()
			.map(Board::posts)
			.flatMap(List::stream)
			.toList();

		Set<Label> savedLabels = savedBoards.stream()
			.map(Board::labels)
			.flatMap(Set::stream)
			.collect(toSet());

		savedPosts.forEach(expected -> {
			// when
			Optional<PostDto> actual = this.sut.findPostDtoByPostId(expected.id());

			// then
			then(actual).isPresent().hasValueSatisfying(dto -> {
				then(dto.id()).isEqualTo(expected.id());
				then(dto.post()).isEqualTo(expected);
				dto.labels().forEach(label -> {
					then(label).isIn(savedLabels);
				});
			});
		});
	}
}
