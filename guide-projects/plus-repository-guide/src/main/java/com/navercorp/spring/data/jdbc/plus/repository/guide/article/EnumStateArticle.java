package com.navercorp.spring.data.jdbc.plus.repository.guide.article;

import java.time.Instant;

import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import com.navercorp.spring.jdbc.plus.commons.annotations.SoftDeleteColumn;

@Table("enum_state_articles")
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class EnumStateArticle {

	@Id
	private final Long id;
	private final String writerId;
	private final String contents;
	private final Instant createdAt;
	private final Instant lastModifiedAt;

	@Version
	private final int version;

	@SoftDeleteColumn.String(valueAsDeleted = "CLOSE")
	@Column("article_state")
	private final State state;

	public boolean closed() {
		return state == State.CLOSE;
	}

	@Getter
	public enum State {
		OPEN("op"), CLOSE("cl");

		private final String code;

		State(String code) {
			this.code = code;
		}
	}
}
