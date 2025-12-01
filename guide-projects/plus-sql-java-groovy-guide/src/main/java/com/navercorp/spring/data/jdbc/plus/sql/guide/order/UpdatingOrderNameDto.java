package com.navercorp.spring.data.jdbc.plus.sql.guide.order;

import org.jspecify.annotations.Nullable;
import org.springframework.util.ObjectUtils;

import com.navercorp.spring.data.jdbc.plus.sql.guide.support.EmptyStringToNullTraits;

public record UpdatingOrderNameDto(
	Long id,
	@Nullable String name
) implements EmptyStringToNullTraits {
	@Override
	public EmptyStringToNullTraits emptyStringToNull() {
		return new UpdatingOrderNameDto(
			id,
			ObjectUtils.isEmpty(name) ? null : name
		);
	}
}
