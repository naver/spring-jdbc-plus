package com.navercorp.spring.data.jdbc.plus.support.convert;

import org.springframework.data.mapping.PersistentPropertyPath;
import org.springframework.data.mapping.PersistentPropertyPaths;
import org.springframework.data.relational.core.mapping.RelationalMappingContext;
import org.springframework.data.relational.core.mapping.RelationalPersistentProperty;

/**
 * COPY org.springframework.data.jdbc.core.mapping.PersistentPropertyPathTestUtils
 *
 * @author Myeonghyeon Lee
 */
@SuppressWarnings("checkstyle:linelength")
public class PersistentPropertyPathTestUtils {
	public static PersistentPropertyPath<RelationalPersistentProperty> getPath(
		RelationalMappingContext context,
		String path,
		Class<?> source
	) {
		PersistentPropertyPaths<?, RelationalPersistentProperty> persistentPropertyPaths = context
			.findPersistentPropertyPaths(source, p -> true);

		return persistentPropertyPaths
			.filter(p -> p.toDotPath().equals(path))
			.stream()
			.findFirst()
			.orElse(null);
	}
}
