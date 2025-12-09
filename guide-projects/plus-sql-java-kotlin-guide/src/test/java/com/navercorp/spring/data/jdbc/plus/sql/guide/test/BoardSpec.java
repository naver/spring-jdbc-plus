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

package com.navercorp.spring.data.jdbc.plus.sql.guide.test;

import static com.navercorp.fixturemonkey.api.expression.JavaGetterMethodPropertySelector.javaGetter;
import static com.navercorp.spring.data.jdbc.plus.sql.guide.test.ArbitrarySpec.UNIQUE_LONG;
import static java.util.stream.Collectors.toMap;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.jspecify.annotations.NonNull;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import com.navercorp.fixturemonkey.buildergroup.ArbitraryBuilderCandidate;
import com.navercorp.fixturemonkey.resolver.ArbitraryBuilderCandidateFactory;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Audit;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.AuditSecret;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Comment;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Config;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Label;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.LabelId;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Memo;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Post;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Tag;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
class BoardSpec {
	static final ArbitraryBuilderCandidate<Board> BOARD_INSTANCE = createBoard();
	static final ArbitraryBuilderCandidate<Label> LABEL_INSTANCE = createLabel();
	static final ArbitraryBuilderCandidate<LabelId> LABEL_ID_INSTANCE = createLabelId();
	static final ArbitraryBuilderCandidate<Post> POST_INSTANCE = createPost();
	static final ArbitraryBuilderCandidate<Tag> TAG_INSTANCE = createTag();
	static final ArbitraryBuilderCandidate<Comment> COMMENT_INSTANCE = createComment();
	static final ArbitraryBuilderCandidate<Audit> AUDIT_INSTANCE = createAudit();
	static final ArbitraryBuilderCandidate<AuditSecret> AUDIT_SECRET_INSTANCE = createAuditSecret();
	static final ArbitraryBuilderCandidate<Memo> MEMO_INSTANCE = createMemo();

	private static final Arbitrary<@NonNull String> BOARD_NAME_ARBITRARY =
		Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
	private static final Arbitrary<@NonNull String> TITLE_ARBITRARY =
		Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50);
	private static final Arbitrary<@NonNull String> CONTENT_ARBITRARY =
		Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(200);
	private static final Arbitrary<@NonNull String> PROJECT_NAME_ARBITRARY =
		Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(20);
	private static final Arbitrary<@NonNull Long> POST_NO_ARBITRARY =
		Arbitraries.longs().between(1L, 1000L);
	private static final Arbitrary<@NonNull Map<String, Config>> CONFIG_MAP_ARBITRARY = config();

	private static ArbitraryBuilderCandidate<Board> createBoard() {
		return ArbitraryBuilderCandidateFactory.of(Board.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Board::id))
					.set(javaGetter(Board::name), BOARD_NAME_ARBITRARY)
					.size(javaGetter(Board::labels), 1, 5)
					.size(javaGetter(Board::posts), 2, 3)
					.set(
						javaGetter(Board::posts).allIndex(Post.class).into(Post::configMap),
						CONFIG_MAP_ARBITRARY
					)
					.thenApply((board, boardArbitraryBuilder) -> {
						boardArbitraryBuilder
							.set(javaGetter(Board::labels), new LinkedHashSet(board.labels()))
							.set(
								javaGetter(Board::configMap),
								board.configMap().entrySet().stream()
									.collect(
										toMap(
											entry -> entry.getValue().configKey(),
											Entry::getValue
										)
									)
							);
					})
			);
	}

	private static ArbitraryBuilderCandidate<Label> createLabel() {
		return ArbitraryBuilderCandidateFactory.of(Label.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.set(javaGetter(Label::name), TITLE_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<LabelId> createLabelId() {
		return ArbitraryBuilderCandidateFactory.of(LabelId.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.set(javaGetter(LabelId::title), TITLE_ARBITRARY)
					.set(javaGetter(LabelId::projectName), PROJECT_NAME_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<Post> createPost() {
		return ArbitraryBuilderCandidateFactory.of(Post.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.set(javaGetter(Post::id), UNIQUE_LONG)
					.set(javaGetter(Post::postNo), POST_NO_ARBITRARY)
					.set(javaGetter(Post::title), TITLE_ARBITRARY)
					.set(javaGetter(Post::content), CONTENT_ARBITRARY)
					.size(javaGetter(Post::tags), 0, 5)
					.size(javaGetter(Post::comments), 0, 3)
			);
	}

	private static ArbitraryBuilderCandidate<Tag> createTag() {
		return ArbitraryBuilderCandidateFactory.of(Tag.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Tag::id))
					.set(javaGetter(Tag::content), CONTENT_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<Comment> createComment() {
		return ArbitraryBuilderCandidateFactory.of(Comment.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Comment::id))
					.set(javaGetter(Comment::content), CONTENT_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<Audit> createAudit() {
		return ArbitraryBuilderCandidateFactory.of(Audit.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(Audit::id))
					.set(javaGetter(Audit::name), BOARD_NAME_ARBITRARY)
			);
	}

	private static ArbitraryBuilderCandidate<AuditSecret> createAuditSecret() {
		return ArbitraryBuilderCandidateFactory.of(AuditSecret.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.setNull(javaGetter(AuditSecret::id))
					.set(javaGetter(AuditSecret::secret), Arbitraries.strings().alpha().ofMinLength(10).ofMaxLength(50))
			);
	}

	private static ArbitraryBuilderCandidate<Memo> createMemo() {
		return ArbitraryBuilderCandidateFactory.of(Memo.class)
			.builder(arbitraryBuilder ->
				arbitraryBuilder
					.set(javaGetter(Memo::memo), CONTENT_ARBITRARY)
			);
	}

	private static Arbitrary<Map<String, Config>> config() {
		return Arbitraries.maps(
			Arbitraries.create(UUID::randomUUID).map(UUID::toString),
			Arbitraries.strings().alpha().ofMinLength(5).ofMaxLength(50)
		).ofMinSize(0).ofMaxSize(5)
			.map(map -> {
				return map.entrySet().stream()
					.collect(
						toMap(
							Entry::getKey,
							entry -> Config.builder()
								.configKey(entry.getKey())
								.configValue(entry.getValue())
								.build()
						)
					);
			});
	}
}

