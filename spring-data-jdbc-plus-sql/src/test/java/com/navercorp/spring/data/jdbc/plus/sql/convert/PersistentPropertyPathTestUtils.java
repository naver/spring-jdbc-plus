package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.mapping.PersistentPropertyPath;
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
		Class<?> baseType
	) {
		return context.findPersistentPropertyPaths(baseType, p -> p.isEntity()) //
			.filter(p -> p.toDotPath().equals(path)) //
			.stream() //
			.findFirst() //
			.orElseThrow(() -> new IllegalArgumentException(
				String.format("No path for %s based on %s", path, baseType)));
	}
}
