package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import java.util.List;
import java.util.Optional;

public interface BoardRepositoryCustom {
	Optional<Board> findGraphById(Long id);

	List<Board> findAllGraph();

	Optional<PostDto> findPostDtoByPostId(Long postId);
}
