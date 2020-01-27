package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import java.util.List;
import java.util.Optional;

import com.navercorp.spring.data.jdbc.plus.sql.guide.board.sql.BoardSql;
import com.navercorp.spring.data.jdbc.plus.sql.provider.EntityJdbcProvider;
import com.navercorp.spring.data.jdbc.plus.sql.support.JdbcRepositorySupport;

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
