package com.navercorp.spring.data.jdbc.plus.sql.guide.board;

import java.util.Set;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.MappedCollection;
import org.springframework.data.relational.core.mapping.Table;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
@Table("post")
public class PostDto {
	@Id
	Long id;

	@Column
	Board.Post post;

	@MappedCollection(idColumn = "board_id")
	Set<Board.Label> labels;
}
