package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import static java.util.Comparator.*;
import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Audit;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.AuditSecret;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Comment;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Config;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Label;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Memo;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Post;
import com.navercorp.spring.data.jdbc.plus.sql.guide.board.Board.Tag;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
public class BoardRepositoryTest {
	@Autowired
	private BoardRepository sut;

	private List<Board> boards = Arrays.asList(
		Board.builder()
			.name("repo")
			.memo(Memo.builder().memo("board1 memo").build())
			.labels(new HashSet<>(Arrays.asList(
				Label.builder().name("label1").build(),
				Label.builder().name("label2").build(),
				Label.builder().name("label3").build()
			)))
			.posts(Arrays.asList(
				Post.builder()
					.postNo(1L)
					.title("first post")
					.content("hello world")
					.tags(new HashSet<>(Arrays.asList(
						Tag.builder().content("tag1").build(),
						Tag.builder().content("tag2").build()
					)))
					.comments(Arrays.asList(
						Comment.builder()
							.content("comment1")
							.audit(Audit.builder()
								.name("naver2")
								.memo(null)
								.secret(AuditSecret.builder()
									.secret("secret1")
									.build())
								.build())
							.build(),
						Comment.builder()
							.content("comment2")
							.audit(Audit.builder()
								.name("naver2")
								.memo(Memo.builder().memo("board post1 comment2 audit memo").build())
								.secret(AuditSecret.builder()
									.secret("secret2")
									.build())
								.build())
							.build()
					))
					.configMap(Stream.of(
						Config.builder().configKey("board1 post1 config-key").configValue("board1 post1 config-value").build(),
						Config.builder().configKey("board1 post1 config-key2").configValue("board1 post1 config-value2").build(),
						Config.builder().configKey("board1 post1 config-key3").configValue("board1 post1 config-value3").build())
						.collect(Collectors.toMap(Config::getConfigKey, config -> config)))
					.audit(Audit.builder()
						.name("naver1")
						.memo(Memo.builder().memo("board post1 audit memo").build())
						.secret(AuditSecret.builder()
							.secret("secret1")
							.build())
						.build())
					.memo(Memo.builder().memo("board post 1 memo").build())
					.build(),
				Post.builder()
					.postNo(2L)
					.title("second post")
					.content("hello world2")
					.tags(new HashSet<>(Arrays.asList(
						Tag.builder().content("tag3").build(),
						Tag.builder().content("tag4").build()
					)))
					.comments(Arrays.asList(
						Comment.builder()
							.content("comment3")
							.audit(Audit.builder()
								.name("naver3")
								.memo(Memo.builder().memo("board post2 comment1 memo").build())
								.secret(AuditSecret.builder()
									.secret("secret3")
									.build())
								.build())
							.build(),
						Comment.builder()
							.content("comment4")
							.audit(Audit.builder()
								.name("naver3")
								.memo(null)
								.secret(AuditSecret.builder()
									.secret("secret3")
									.build())
								.build())
							.build()
					))
					.configMap(null)
					.audit(Audit.builder()
						.name("naver2")
						.memo(null)
						.secret(AuditSecret.builder()
							.secret("secret2")
							.build())
						.build())
					.build()
			))
			.configMap(Stream.of(
				Config.builder().configKey("board1 config-key").configValue("board1 config-value").build(),
				Config.builder().configKey("board1 config-key2").configValue("board1 config-value2").build())
				.collect(Collectors.toMap(Config::getConfigKey, config -> config)))
			.audit(Audit.builder()
				.name("naver")
				.memo(Memo.builder().memo("board1 audit memo").build())
				.secret(AuditSecret.builder()
					.secret("secret1")
					.build())
				.build())
			.build(),
		Board.builder()
			.name("repo3")
			.labels(new HashSet<>())
			.posts(Arrays.asList(
				Post.builder()
					.postNo(5L)
					.title("fifth post")
					.content("hello world5")
					.tags(new HashSet<>())
					.comments(new ArrayList<>())
					.audit(null)
					.memo(Memo.builder().memo("board2 post1 memo").build())
					.build(),
				Post.builder()
					.postNo(6L)
					.title("sixth post")
					.content("hello world6")
					.tags(new HashSet<>(Arrays.asList(
						Tag.builder().content("tag7").build(),
						Tag.builder().content("tag8").build()
					)))
					.comments(Arrays.asList(
						Comment.builder()
							.content("comment8")
							.audit(null)
							.build(),
						Comment.builder()
							.content("comment8")
							.audit(Audit.builder()
								.name("naver5")
								.memo(Memo.builder().memo("board2 comment2 audit memo").build())
								.secret(null)
								.build())
							.build()
					))
					.audit(null)
					.build()
			))
			.configMap(null)
			.audit(null)
			.build(),
		Board.builder()
			.name("repo2")
			.labels(new HashSet<>(Arrays.asList(
				Label.builder().name("label4").build(),
				Label.builder().name("label5").build(),
				Label.builder().name("label6").build()
			)))
			.posts(Arrays.asList(
				Post.builder()
					.postNo(3L)
					.title("third post")
					.content("hello world3")
					.tags(new HashSet<>(Arrays.asList(
						Tag.builder().content("tag5").build(),
						Tag.builder().content("tag6").build()
					)))
					.comments(Arrays.asList(
						Comment.builder()
							.content("comment5")
							.audit(Audit.builder()
								.name("naver4")
								.secret(AuditSecret.builder()
									.secret("secret4")
									.build())
								.build())
							.build(),
						Comment.builder()
							.content("comment6")
							.audit(Audit.builder()
								.name("naver4")
								.secret(AuditSecret.builder()
									.secret("secret4")
									.build())
								.build())
							.build()
					))
					.audit(Audit.builder()
						.name("naver3")
						.secret(AuditSecret.builder()
							.secret("secret3")
							.build())
						.build())
					.memo(Memo.builder().memo("board2 post1 memo").build())
					.build(),
				Post.builder()
					.postNo(4L)
					.title("fourth post")
					.content("hello world4")
					.tags(new HashSet<>(Arrays.asList(
						Tag.builder().content("tag7").build(),
						Tag.builder().content("tag8").build()
					)))
					.comments(Arrays.asList(
						Comment.builder()
							.content("comment7")
							.audit(Audit.builder()
								.name("naver5")
								.memo(null)
								.secret(AuditSecret.builder()
									.secret("secret5")
									.build())
								.build())
							.build(),
						Comment.builder()
							.content("comment8")
							.audit(Audit.builder()
								.name("naver5")
								.memo(Memo.builder().memo("board2 comment2 audit memo").build())
								.secret(AuditSecret.builder()
									.secret("secret5")
									.build())
								.build())
							.build()
					))
					.audit(Audit.builder()
						.name("naver4")
						.memo(null)
						.secret(AuditSecret.builder()
							.secret("secret4")
							.build())
						.build())
					.memo(null)
					.build()
			))
			.audit(Audit.builder()
				.name("naver2")
				.memo(null)
				.secret(AuditSecret.builder()
					.secret("secret2")
					.build())
				.build())
			.build()
	);

	@Test
	void findById() {
		// given
		sut.saveAll(boards);

		// when
		Optional<Board> actual1 = this.sut.findById(boards.get(0).getId());
		Optional<Board> actual2 = this.sut.findById(boards.get(1).getId());
		Optional<Board> actual3 = this.sut.findById(boards.get(2).getId());

		// then
		Assertions.assertThat(actual1).isPresent();
		assertEquals(actual1.get(), boards.get(0));
		Assertions.assertThat(actual2).isPresent();
		assertEquals(actual2.get(), boards.get(1));
		Assertions.assertThat(actual3).isPresent();
		assertEquals(actual3.get(), boards.get(2));
	}

	@Test
	void findGraphById() {
		// given
		sut.saveAll(boards);

		// when
		Optional<Board> actual1 = this.sut.findGraphById(boards.get(0).getId());
		Optional<Board> actual2 = this.sut.findGraphById(boards.get(1).getId());
		Optional<Board> actual3 = this.sut.findGraphById(boards.get(2).getId());

		// then
		Assertions.assertThat(actual1).isPresent();
		assertEquals(actual1.get(), boards.get(0));
		Assertions.assertThat(actual2).isPresent();
		assertEquals(actual2.get(), boards.get(1));
		Assertions.assertThat(actual3).isPresent();
		assertEquals(actual3.get(), boards.get(2));
	}

	@Test
	void findAllGraph() {
		// given
		sut.saveAll(boards);

		// when
		List<Board> actual = this.sut.findAllGraph();

		// then
		Assertions.assertThat(actual).hasSize(3);
		assertEquals(actual.get(0), boards.get(0));
		assertEquals(actual.get(1), boards.get(1));
		assertEquals(actual.get(2), boards.get(2));
	}

	@Test
	void findPostDtoByPostId() {
		// given
		sut.saveAll(boards);

		// when
		Optional<PostDto> actual1 = this.sut.findPostDtoByPostId(boards.get(0).getPosts().get(0).getId());
		Optional<PostDto> actual2 = this.sut.findPostDtoByPostId(boards.get(0).getPosts().get(1).getId());
		Optional<PostDto> actual3 = this.sut.findPostDtoByPostId(boards.get(1).getPosts().get(0).getId());
		Optional<PostDto> actual4 = this.sut.findPostDtoByPostId(boards.get(1).getPosts().get(1).getId());
		Optional<PostDto> actual5 = this.sut.findPostDtoByPostId(boards.get(2).getPosts().get(0).getId());
		Optional<PostDto> actual6 = this.sut.findPostDtoByPostId(boards.get(2).getPosts().get(1).getId());

		// then
		Assertions.assertThat(actual1).isPresent();
		assertThat(actual1.get().getId()).isEqualTo(boards.get(0).getPosts().get(0).getId());
		assertEquals(actual1.get().getPost(), boards.get(0).getPosts().get(0));
		assertLabelsEquals(
			actual1.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(0).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));

		Assertions.assertThat(actual2).isPresent();
		assertThat(actual2.get().getId()).isEqualTo(boards.get(0).getPosts().get(1).getId());
		assertEquals(actual2.get().getPost(), boards.get(0).getPosts().get(1));
		assertLabelsEquals(
			actual2.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(0).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));

		Assertions.assertThat(actual3).isPresent();
		assertThat(actual3.get().getId()).isEqualTo(boards.get(1).getPosts().get(0).getId());
		assertEquals(actual3.get().getPost(), boards.get(1).getPosts().get(0));
		assertLabelsEquals(
			actual3.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(1).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));

		Assertions.assertThat(actual4).isPresent();
		assertThat(actual4.get().getId()).isEqualTo(boards.get(1).getPosts().get(1).getId());
		assertEquals(actual4.get().getPost(), boards.get(1).getPosts().get(1));
		assertLabelsEquals(
			actual4.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(1).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));

		Assertions.assertThat(actual5).isPresent();
		assertThat(actual5.get().getId()).isEqualTo(boards.get(2).getPosts().get(0).getId());
		assertEquals(actual5.get().getPost(), boards.get(2).getPosts().get(0));
		assertLabelsEquals(
			actual5.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(2).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));

		Assertions.assertThat(actual6).isPresent();
		assertThat(actual6.get().getId()).isEqualTo(boards.get(2).getPosts().get(1).getId());
		assertEquals(actual6.get().getPost(), boards.get(2).getPosts().get(1));
		assertLabelsEquals(
			actual6.get().getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()),
			boards.get(2).getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList()));
	}

	private void assertEquals(Board actual, Board target) {
		assertThat(actual.getId()).isEqualTo(target.getId());
		assertThat(actual.getName()).isEqualTo(target.getName());

		if (actual.getMemo() == null) {
			assertThat(target.getMemo()).isNull();
		} else {
			assertThat(actual.getMemo().getMemo()).isEqualTo(target.getMemo().getMemo());
		}

		if (actual.getAudit() == null) {
			assertThat(target.getAudit()).isNull();
		} else {
			assertThat(actual.getAudit().getId()).isEqualTo(target.getAudit().getId());
			assertThat(actual.getAudit().getName()).isEqualTo(target.getAudit().getName());

			if (actual.getAudit().getMemo() == null) {
				assertThat(target.getAudit().getMemo()).isNull();
			} else {
				assertThat(actual.getAudit().getMemo().getMemo()).isEqualTo(target.getAudit().getMemo().getMemo());
			}

			if (actual.getAudit().getSecret() == null) {
				assertThat(target.getAudit().getSecret()).isNull();
			} else {
				assertThat(actual.getAudit().getSecret().getId()).isEqualTo(target.getAudit().getSecret().getId());
				assertThat(actual.getAudit().getSecret().getSecret()).isEqualTo(target.getAudit().getSecret().getSecret());
			}
		}

		if (target.getConfigMap() == null || target.getConfigMap().isEmpty()) {
			assertThat(actual.getConfigMap()).isEmpty();
		} else {
			List<Map.Entry<String, Config>> actualConfigs = actual.getConfigMap().entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toList());
			List<Map.Entry<String, Config>> targetConfigs = target.getConfigMap().entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toList());

			assertThat(actualConfigs.size()).isEqualTo(targetConfigs.size());
			for (int i = 0; i < actualConfigs.size(); i++) {
				assertThat(actualConfigs.get(i).getKey()).isEqualTo(targetConfigs.get(i).getKey());
				assertThat(actualConfigs.get(i).getValue().getId()).isEqualTo(targetConfigs.get(i).getValue().getId());
				assertThat(actualConfigs.get(i).getValue().getConfigKey()).isEqualTo(targetConfigs.get(i).getValue().getConfigKey());
				assertThat(actualConfigs.get(i).getValue().getConfigValue()).isEqualTo(targetConfigs.get(i).getValue().getConfigValue());
			}
		}

		List<Label> actualLabels = actual.getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList());
		List<Label> boardLabels = actual.getLabels().stream().sorted(comparingLong(Label::getId)).collect(Collectors.toList());
		assertLabelsEquals(actualLabels, boardLabels);

		List<Post> actualPosts = actual.getPosts();
		List<Post> boardPosts = target.getPosts();

		assertThat(actualPosts.size()).isEqualTo(boardPosts.size());

		for (int i = 0; i < actualPosts.size(); i++) {
			assertEquals(actualPosts.get(i), boardPosts.get(i));
		}
	}

	private void assertLabelsEquals(List<Label> actualLabels, List<Label> targetLabels) {
		assertThat(actualLabels.size()).isEqualTo(targetLabels.size());

		for (int i = 0; i < actualLabels.size(); i++) {
			assertThat(actualLabels.get(i).getId()).isEqualTo(targetLabels.get(i).getId());
			assertThat(actualLabels.get(i).getName()).isEqualTo(targetLabels.get(i).getName());
		}
	}

	private void assertEquals(Post actualPost, Post targetPost) {
		assertThat(actualPost.getId()).isEqualTo(targetPost.getId());
		assertThat(actualPost.getPostNo()).isEqualTo(targetPost.getPostNo());
		assertThat(actualPost.getTitle()).isEqualTo(targetPost.getTitle());
		assertThat(actualPost.getContent()).isEqualTo(targetPost.getContent());

		if (actualPost.getMemo() == null) {
			assertThat(targetPost.getMemo()).isNull();
		} else {
			assertThat(actualPost.getMemo().getMemo()).isEqualTo(targetPost.getMemo().getMemo());
		}

		if (actualPost.getAudit() == null) {
			assertThat(targetPost.getAudit()).isNull();
		} else {
			assertThat(actualPost.getAudit().getId()).isEqualTo(targetPost.getAudit().getId());
			assertThat(actualPost.getAudit().getName()).isEqualTo(targetPost.getAudit().getName());

			if (actualPost.getAudit().getMemo() == null) {
				assertThat(targetPost.getAudit().getMemo()).isNull();
			} else {
				assertThat(actualPost.getAudit().getMemo().getMemo()).isEqualTo(targetPost.getAudit().getMemo().getMemo());
			}

			if (actualPost.getAudit().getSecret() == null) {
				assertThat(targetPost.getAudit().getSecret()).isNull();
			} else {
				assertThat(actualPost.getAudit().getSecret().getId()).isEqualTo(targetPost.getAudit().getSecret().getId());
				assertThat(actualPost.getAudit().getSecret().getSecret()).isEqualTo(targetPost.getAudit().getSecret().getSecret());
			}
		}

		List<Tag> actualTags = actualPost.getTags().stream().sorted(comparingLong(Tag::getId)).collect(Collectors.toList());
		List<Tag> postTags = targetPost.getTags().stream().sorted(comparingLong(Tag::getId)).collect(Collectors.toList());

		assertThat(actualTags.size()).isEqualTo(postTags.size());

		for (int i = 0; i < actualTags.size(); i++) {
			assertThat(actualTags.get(i).getId()).isEqualTo(postTags.get(i).getId());
			assertThat(actualTags.get(i).getContent()).isEqualTo(postTags.get(i).getContent());
		}

		List<Comment> actualComments = actualPost.getComments();
		List<Comment> postComments = targetPost.getComments();

		assertThat(actualComments.size()).isEqualTo(postComments.size());

		for (int i = 0; i < actualTags.size(); i++) {
			assertThat(actualComments.get(i).getId()).isEqualTo(postComments.get(i).getId());
			assertThat(actualComments.get(i).getContent()).isEqualTo(postComments.get(i).getContent());

			if (actualComments.get(i).getAudit() == null) {
				assertThat(postComments.get(i).getAudit()).isNull();
			} else {
				assertThat(actualComments.get(i).getAudit().getId()).isEqualTo(postComments.get(i).getAudit().getId());
				assertThat(actualComments.get(i).getAudit().getName()).isEqualTo(postComments.get(i).getAudit().getName());

				if (actualComments.get(i).getAudit().getMemo() == null) {
					assertThat(postComments.get(i).getAudit().getMemo()).isNull();
				} else {
					assertThat(actualComments.get(i).getAudit().getMemo().getMemo()).isEqualTo(postComments.get(i).getAudit().getMemo().getMemo());
				}

				if (actualComments.get(i).getAudit().getSecret() == null) {
					assertThat(postComments.get(i).getAudit().getSecret()).isNull();
				} else {
					assertThat(actualComments.get(i).getAudit().getSecret().getId()).isEqualTo(postComments.get(i).getAudit().getSecret().getId());
					assertThat(actualComments.get(i).getAudit().getSecret().getSecret()).isEqualTo(postComments.get(i).getAudit().getSecret().getSecret());
				}
			}
		}

		if (targetPost.getConfigMap() == null || targetPost.getConfigMap().isEmpty()) {
			assertThat(actualPost.getConfigMap()).isEmpty();
		} else {
			List<Map.Entry<String, Config>> actualConfigs = actualPost.getConfigMap().entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toList());
			List<Map.Entry<String, Config>> targetConfigs = targetPost.getConfigMap().entrySet()
				.stream()
				.sorted(Map.Entry.comparingByKey())
				.collect(Collectors.toList());

			assertThat(actualConfigs.size()).isEqualTo(targetConfigs.size());
			for (int i = 0; i < actualConfigs.size(); i++) {
				assertThat(actualConfigs.get(i).getKey()).isEqualTo(targetConfigs.get(i).getKey());
				assertThat(actualConfigs.get(i).getValue().getId()).isEqualTo(targetConfigs.get(i).getValue().getId());
				assertThat(actualConfigs.get(i).getValue().getConfigKey()).isEqualTo(targetConfigs.get(i).getValue().getConfigKey());
				assertThat(actualConfigs.get(i).getValue().getConfigValue()).isEqualTo(targetConfigs.get(i).getValue().getConfigValue());
			}
		}
	}
}
