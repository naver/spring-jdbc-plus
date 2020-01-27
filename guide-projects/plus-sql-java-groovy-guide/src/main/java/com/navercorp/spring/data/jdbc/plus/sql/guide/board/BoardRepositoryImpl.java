/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
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

import java.util.List;
import java.util.Optional;

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.sql.BoardSql;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;

/**
 * @author Myeonghyeon Lee
 */
public class BoardRepositoryImpl extends JdbcRepositorySupport<Board> implements BoardRepositoryCustom {
	private final BoardSql sqls;

	protected BoardRepositoryImpl(EntityJdbcProvider entityJdbcProvider) {
		super(Board.class, entityJdbcProvider);
		this.sqls = sqls(BoardSql::new);
	}

	@Override
	public Optional<Board> findGraphById(Long id) {
		return findOne(this.sqls.selectGraphById(), mapParameterSource()
			.addValue("boardId", id));
	}

	@Override
	public List<Board> findAllGraph() {
		return find(this.sqls.selectAllGraph(), mapParameterSource());
	}

	@Override
	public Optional<PostDto> findPostDtoByPostId(Long postId) {
		return findOne(this.sqls.selectPostDtoByPostId(), mapParameterSource()
				.addValue("postId", postId),
			PostDto.class);
	}
}
