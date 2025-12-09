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

package com.navercorp.spring.data.jdbc.plus.sql.guide.board

import org.assertj.core.api.BDDAssertions.then
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import java.util.stream.Collectors

/**
 * @author Myeonghyeon Lee
 */
@SpringBootTest
@Transactional
@ActiveProfiles("test")
class BoardRepositoryTest {
    @Autowired
    lateinit var sut: BoardRepository

    private var boards: List<Board> = listOf(
        Board(
            name = "repo",
            memo = Memo(memo = "board1 memo"),
            labels = linkedSetOf(
                Label(name = "label1"),
                Label(name = "label2"),
                Label(name = "label3")
            ),
            posts = listOf(
                Post(
                    postNo = 1,
                    title = "first post",
                    content = "hello world",
                    tags = linkedSetOf(
                        Tag(content = "tag1"),
                        Tag(content = "tag2")
                    ),
                    comments = listOf(
                        Comment(
                            content = "comment1",
                            audit = Audit(
                                name = "naver2",
                                memo = null,
                                secret = AuditSecret(
                                    secret = "secret1"
                                )
                            )
                        ),
                        Comment(
                            content = "comment2",
                            audit = Audit(
                                name = "naver2",
                                memo = Memo("board post1 comment2 audit memo"),
                                secret = AuditSecret(secret = "secret2")
                            )
                        )
                    ),
                    configMap = mapOf(
                        "board1 post1 config-key" to
                            Config(
                                configKey = "board1 post1 config-key",
                                configValue = "board1 post1 config-value"
                            ),
                        "board1 post1 config-key2" to
                            Config(
                                configKey = "board1 post1 config-key2",
                                configValue = "board1 post1 config-value2"
                            ),
                        "board1 post1 config-key3" to
                            Config(
                                configKey = "board1 post1 config-key3",
                                configValue = "board1 post1 config-value3"
                            )
                    ),
                    audit = Audit(
                        name = "naver1",
                        memo = Memo(memo = "board post1 audit memo"),
                        secret = AuditSecret(secret = "secret1")
                    ),
                    memo = Memo("board post 1 memo")
                ),
                Post(
                    postNo = 2,
                    title = "second post",
                    content = "hello world2",
                    tags = linkedSetOf(
                        Tag(content = "tag3"),
                        Tag(content = "tag4")
                    ),
                    comments = listOf(
                        Comment(
                            content = "comment3",
                            audit = Audit(
                                name = "naver3",
                                memo = Memo(memo = "board post2 comment1 memo"),
                                secret = AuditSecret(
                                    secret = "secret3"
                                )
                            )
                        ),
                        Comment(
                            content = "comment4",
                            audit = Audit(
                                name = "naver3",
                                memo = null,
                                secret = AuditSecret(secret = "secret3")
                            )
                        )
                    ),
                    configMap = mapOf(),
                    audit = Audit(
                        name = "naver2",
                        memo = null,
                        secret = AuditSecret(secret = "secret2")
                    ),
                    memo = null
                )
            ),
            configMap = mapOf(
                "board1 config-key" to Config(configKey = "board1 config-key", configValue = "board1 config-value"),
                "board1 config-key2" to Config(configKey = "board1 config-key2", configValue = "board1 config-value2")
            ),
            audit = Audit(
                name = "naver",
                memo = Memo(memo = "board1 audit memo"),
                secret = AuditSecret(secret = "secret1")
            )
        ),
        Board(
            name = "repo3",
            memo = null,
            labels = setOf(),
            posts = listOf(
                Post(
                    postNo = 5,
                    title = "fifth post",
                    content = "hello world5",
                    tags = setOf(),
                    comments = listOf(),
                    configMap = mapOf(),
                    audit = null,
                    memo = Memo("board2 post1 memo")
                ),
                Post(
                    postNo = 6,
                    title = "sixth post",
                    content = "hello world6",
                    tags = linkedSetOf(
                        Tag(content = "tag7"),
                        Tag(content = "tag8")
                    ),
                    comments = listOf(
                        Comment(
                            content = "comment8",
                            audit = null
                        ),
                        Comment(
                            content = "comment8",
                            audit = Audit(
                                name = "naver5",
                                memo = Memo("board2 comment2 audit memo"),
                                secret = null
                            )
                        )
                    ),
                    configMap = mapOf(),
                    audit = null,
                    memo = null
                )
            ),
            configMap = mapOf(),
            audit = null
        ),
        Board(
            name = "repo2",
            memo = null,
            labels = setOf(
                Label(name = "label4"),
                Label(name = "label5"),
                Label(name = "label6")
            ),
            posts = listOf(
                Post(
                    postNo = 3,
                    title = "third post",
                    content = "hello world3",
                    tags = setOf(
                        Tag(content = "tag5"),
                        Tag(content = "tag6")
                    ),
                    comments = listOf(
                        Comment(
                            content = "comment5",
                            audit = Audit(
                                name = "naver4",
                                memo = null,
                                secret = AuditSecret(
                                    secret = "secret4"
                                )
                            )
                        ),
                        Comment(
                            content = "comment6",
                            audit = Audit(
                                name = "naver4",
                                memo = null,
                                secret = AuditSecret(secret = "secret4")
                            )
                        )
                    ),
                    configMap = mapOf(),
                    audit = Audit(
                        name = "naver3",
                        memo = null,
                        secret = AuditSecret(secret = "secret3")
                    ),
                    memo = Memo("board2 post1 memo")
                ),
                Post(
                    postNo = 4,
                    title = "fourth post",
                    content = "hello world4",
                    tags = setOf(
                        Tag(content = "tag7"),
                        Tag(content = "tag8")
                    ),
                    comments = listOf(
                        Comment(
                            content = "comment7",
                            audit = Audit(
                                name = "naver5",
                                memo = null,
                                secret = AuditSecret(
                                    secret = "secret5"
                                )
                            )
                        ),
                        Comment(
                            content = "comment8",
                            audit = Audit(
                                name = "naver5",
                                memo = Memo(memo = "board2 comment2 audit memo"),
                                secret = AuditSecret(secret = "secret5")
                            )
                        )
                    ),
                    configMap = mapOf(),
                    audit = Audit(
                        name = "naver4",
                        memo = null,
                        secret = AuditSecret(secret = "secret4")
                    ),
                    memo = null
                )
            ),
            configMap = mapOf(
                "board1 config-key" to Config(configKey = "board1 config-key", configValue = "board1 config-value"),
                "board1 config-key2" to Config(configKey = "board1 config-key2", configValue = "board1 config-value2")
            ),
            audit = Audit(
                name = "naver",
                memo = Memo(memo = "board1 audit memo"),
                secret = AuditSecret(secret = "secret1")
            )
        )
    )

    @Test
    fun findById() {
        // given
        boards = sut.saveAll(boards)

        // when
        val actual1 = sut.findById(boards[0].id!!)
        val actual2 = sut.findById(boards[1].id!!)
        val actual3 = sut.findById(boards[2].id!!)

        // then
        then(actual1).isNotNull
        assertEquals(actual1.get(), boards[0])
        then(actual2).isNotNull
        assertEquals(actual2.get(), boards[1])
        then(actual3).isNotNull
        assertEquals(actual3.get(), boards[2])
    }

    @Test
    fun findGraphById() {
        // given
        boards = sut.saveAll(boards)

        // when
        val actual1: Board? = sut.findGraphById(boards[0].id!!)
        val actual2: Board? = sut.findGraphById(boards[1].id!!)
        val actual3: Board? = sut.findGraphById(boards[2].id!!)

        // then
        then(actual1).isNotNull
        assertEquals(actual1!!, boards[0])
        then(actual2).isNotNull
        assertEquals(actual2!!, boards[1])
        then(actual3).isNotNull
        assertEquals(actual3!!, boards[2])
    }

    @Test
    fun findAllGraph() {
        // given
        boards = sut.saveAll(boards)

        // when
        val actual = sut.findAllGraph()

        // then
        assertEquals(actual[0], boards[0])
        assertEquals(actual[1], boards[1])
        assertEquals(actual[2], boards[2])
    }

    @Test
    fun findPostDtoByPostId() {
        // given
        @Suppress("UNCHECKED_CAST")
        boards = sut.saveAll(boards) as List<Board>

        // when
        val actual1: PostDto? = sut.findPostDtoByPostId(boards[0].posts[0].id!!)
        val actual2: PostDto? = sut.findPostDtoByPostId(boards[0].posts[1].id!!)
        val actual3: PostDto? = sut.findPostDtoByPostId(boards[1].posts[0].id!!)
        val actual4: PostDto? = sut.findPostDtoByPostId(boards[1].posts[1].id!!)
        val actual5: PostDto? = sut.findPostDtoByPostId(boards[2].posts[0].id!!)
        val actual6: PostDto? = sut.findPostDtoByPostId(boards[2].posts[1].id!!)

        // then
        then(actual1).isNotNull
        then(actual1!!.id).isEqualTo(boards[0].posts[0].id)
        assertEquals(actual1.post, boards[0].posts[0])
        assertLabelsEquals(
            actual1.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[0].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
        then(actual2).isNotNull
        then(actual2!!.id).isEqualTo(boards[0].posts[1].id)
        assertEquals(actual2.post, boards[0].posts[1])
        assertLabelsEquals(
            actual2.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[0].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
        then(actual3).isNotNull
        then(actual3!!.id).isEqualTo(boards[1].posts[0].id)
        assertEquals(actual3.post, boards[1].posts[0])
        assertLabelsEquals(
            actual3.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[1].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
        then(actual4).isNotNull
        then(actual4!!.id).isEqualTo(boards[1].posts[1].id)
        assertEquals(actual4.post, boards[1].posts[1])
        assertLabelsEquals(
            actual4.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[1].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
        then(actual5).isNotNull
        then(actual5!!.id).isEqualTo(boards[2].posts[0].id)
        assertEquals(actual5.post, boards[2].posts[0])
        assertLabelsEquals(
            actual5.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[2].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
        then(actual6).isNotNull
        then(actual6!!.id).isEqualTo(boards[2].posts[1].id)
        assertEquals(actual6.post, boards[2].posts[1])
        assertLabelsEquals(
            actual6.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList()),
            boards[2].labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        )
    }

    private fun assertEquals(actual: Board, target: Board) {
        then(actual.id).isEqualTo(target.id)
        then(actual.name).isEqualTo(target.name)
        if (actual.memo == null) {
            then(target.memo).isNull()
        } else {
            then(actual.memo.memo).isEqualTo(target.memo!!.memo)
        }
        if (actual.audit == null) {
            then(target.audit).isNull()
        } else {
            then(actual.audit.id).isEqualTo(target.audit!!.id)
            then(actual.audit.name).isEqualTo(target.audit.name)
            if (actual.audit.memo == null) {
                then(target.audit.memo).isNull()
            } else {
                then(actual.audit.memo.memo).isEqualTo(target.audit.memo!!.memo)
            }
            if (actual.audit.secret == null) {
                then(target.audit.secret!!).isNull()
            } else {
                // id not set origin object
                // then(actual.audit.secret.id).isEqualTo(target.audit.secret!!.id)
                then(actual.audit.secret.secret).isEqualTo(target.audit.secret!!.secret)
            }
        }
        if (target.configMap.isEmpty()) {
            then(actual.configMap).isEmpty()
        } else {
            val actualConfigs: List<Map.Entry<String, Config>> = actual.configMap.entries
                .stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .collect(Collectors.toList())
            val targetConfigs: List<Map.Entry<String, Config>> = target.configMap.entries
                .stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .collect(Collectors.toList())
            then(actualConfigs.size).isEqualTo(targetConfigs.size)
            for (i in actualConfigs.indices) {
                then(actualConfigs[i].key).isEqualTo(targetConfigs[i].key)
                then(actualConfigs[i].value.id).isEqualTo(targetConfigs[i].value.id)
                then(actualConfigs[i].value.configKey).isEqualTo(targetConfigs[i].value.configKey)
                then(actualConfigs[i].value.configValue).isEqualTo(targetConfigs[i].value.configValue)
            }
        }
        val actualLabels: List<Label> =
            actual.labels.stream().sorted(Comparator.comparingLong { obj: Label -> obj.id!! })
                .collect(Collectors.toList())
        val boardLabels: List<Label> = actual.labels.sortedBy { it.id }
        assertLabelsEquals(actualLabels, boardLabels)
        val actualPosts: List<Post> = actual.posts
        val boardPosts: List<Post> = target.posts
        then(actualPosts.size).isEqualTo(boardPosts.size)
        for (i in actualPosts.indices) {
            assertEquals(actualPosts[i], boardPosts[i])
        }
    }

    private fun assertLabelsEquals(actualLabels: List<Label>, targetLabels: List<Label>) {
        then(actualLabels.size).isEqualTo(targetLabels.size)
        for (i in actualLabels.indices) {
            then(actualLabels[i].id).isEqualTo(targetLabels[i].id)
            then(actualLabels[i].name).isEqualTo(targetLabels[i].name)
        }
    }

    private fun assertEquals(actualPost: Post, targetPost: Post) {
        then(actualPost.id).isEqualTo(targetPost.id)
        then(actualPost.postNo).isEqualTo(targetPost.postNo)
        then(actualPost.title).isEqualTo(targetPost.title)
        then(actualPost.content).isEqualTo(targetPost.content)
        if (actualPost.memo == null) {
            then(targetPost.memo).isNull()
        } else {
            then(actualPost.memo.memo).isEqualTo(targetPost.memo!!.memo)
        }
        if (actualPost.audit == null) {
            then(targetPost.audit).isNull()
        } else {
            // id not set origin object
            // then(actualPost.audit.id).isEqualTo(targetPost.audit!!.id)
            then(actualPost.audit.name).isEqualTo(targetPost.audit!!.name)
            if (actualPost.audit.memo == null) {
                then(targetPost.audit.memo).isNull()
            } else {
                then(actualPost.audit.memo.memo).isEqualTo(targetPost.audit.memo!!.memo)
            }
            if (actualPost.audit.secret == null) {
                then(targetPost.audit.secret).isNull()
            } else {
                // id not set origin object
                // then(actualPost.audit.secret.id).isEqualTo(targetPost.audit.secret!!.id)
                then(actualPost.audit.secret.secret).isEqualTo(targetPost.audit.secret!!.secret)
            }
        }
        val actualTags: List<Tag> = actualPost.tags.sortedBy { it.id }
        val postTags: List<Tag> = targetPost.tags.sortedBy { it.id }
        then(actualTags.size).isEqualTo(postTags.size)
        for (i in actualTags.indices) {
            then(actualTags[i].id).isEqualTo(postTags[i].id)
            then(actualTags[i].content).isEqualTo(postTags[i].content)
        }
        val actualComments: List<Comment> = actualPost.comments
        val postComments: List<Comment> = targetPost.comments
        then(actualComments.size).isEqualTo(postComments.size)
        for (i in actualTags.indices) {
            // id not set origin object
            // then(actualComments[i].id).isEqualTo(postComments[i].id)
            then(actualComments[i].content).isEqualTo(postComments[i].content)
            if (actualComments[i].audit == null) {
                then(postComments[i].audit).isNull()
            } else {
                // id not set origin object
                // then(actualComments[i].audit!!.id).isEqualTo(postComments[i].audit!!.id)
                then(actualComments[i].audit!!.name).isEqualTo(postComments[i].audit!!.name)
                if (actualComments[i].audit!!.memo == null) {
                    then(postComments[i].audit!!.memo).isNull()
                } else {
                    then(actualComments[i].audit!!.memo!!.memo).isEqualTo(postComments[i].audit!!.memo!!.memo)
                }
                if (actualComments[i].audit!!.secret == null) {
                    then(postComments[i].audit!!.secret).isNull()
                } else {
                    // id not set origin object
                    // then(actualComments[i].audit!!.secret!!.id).isEqualTo(postComments[i].audit!!.secret!!.id)
                    then(actualComments[i].audit!!.secret!!.secret).isEqualTo(postComments[i].audit!!.secret!!.secret)
                }
            }
        }
        if (targetPost.configMap.isEmpty()) {
            then(actualPost.configMap).isEmpty()
        } else {
            val actualConfigs: List<Map.Entry<String, Config>> = actualPost.configMap.entries
                .stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .collect(Collectors.toList())
            val targetConfigs: List<Map.Entry<String, Config>> = targetPost.configMap.entries
                .stream()
                .sorted(java.util.Map.Entry.comparingByKey())
                .collect(Collectors.toList())
            then(actualConfigs.size).isEqualTo(targetConfigs.size)
            for (i in actualConfigs.indices) {
                then(actualConfigs[i].key).isEqualTo(targetConfigs[i].key)
                // id not set origin object
                // then(actualConfigs[i].value.id).isEqualTo(targetConfigs[i].value.id)
                then(actualConfigs[i].value.configKey).isEqualTo(targetConfigs[i].value.configKey)
                then(actualConfigs[i].value.configValue).isEqualTo(targetConfigs[i].value.configValue)
            }
        }
    }
}
