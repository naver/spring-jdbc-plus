package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import org.springframework.data.repository.CrudRepository;

public interface BoardRepository extends CrudRepository<Board, Long>, BoardRepositoryCustom {
}
