package com.navercorp.spring.data.jdbc.plus.sql.convert;

import org.springframework.data.relational.core.dialect.AbstractDialect;
import org.springframework.data.relational.core.dialect.ArrayColumns;
import org.springframework.data.relational.core.dialect.LimitClause;
import org.springframework.data.relational.core.sql.IdentifierProcessing;
import org.springframework.util.Assert;
import org.springframework.util.ClassUtils;

/**
 * COPY org.springframework.data.jdbc.testing.AnsiDialect
 *
 * @author Myeonghyeon Lee
 */
public class AnsiDialect extends AbstractDialect {

	/**
	 * Singleton instance.
	 */
	public static final AnsiDialect INSTANCE = new AnsiDialect();
	private static final LimitClause LIMIT_CLAUSE = new LimitClause() {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.LimitClause#getLimit(long)
		 */
		@Override
		public String getLimit(long limit) {
			return String.format("FETCH FIRST %d ROWS ONLY", limit);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.LimitClause#getOffset(long)
		 */
		@Override
		public String getOffset(long offset) {
			return String.format("OFFSET %d ROWS", offset);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.LimitClause#getClause(long, long)
		 */
		@Override
		public String getLimitOffset(long limit, long offset) {
			return String.format("OFFSET %d ROWS FETCH FIRST %d ROWS ONLY", offset, limit);
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.LimitClause#getClausePosition()
		 */
		@Override
		public Position getClausePosition() {
			return Position.AFTER_ORDER_BY;
		}
	};
	private final AnsiArrayColumns arrayColumns = new AnsiArrayColumns();

	protected AnsiDialect() {
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.relational.core.dialect.Dialect#limit()
	 */
	@Override
	public LimitClause limit() {
		return LIMIT_CLAUSE;
	}

	/*
	 * (non-Javadoc)
	 * @see org.springframework.data.relational.core.dialect.Dialect#getArraySupport()
	 */
	@Override
	public ArrayColumns getArraySupport() {
		return arrayColumns;
	}

	@Override
	public IdentifierProcessing getIdentifierProcessing() {
		return IdentifierProcessing.ANSI;
	}

	static class AnsiArrayColumns implements ArrayColumns {

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.ArrayColumns#isSupported()
		 */
		@Override
		public boolean isSupported() {
			return true;
		}

		/*
		 * (non-Javadoc)
		 * @see org.springframework.data.relational.core.dialect.ArrayColumns#getArrayType(java.lang.Class)
		 */
		@Override
		public Class<?> getArrayType(Class<?> userType) {

			Assert.notNull(userType, "Array component type must not be null");

			return ClassUtils.resolvePrimitiveIfNecessary(userType);
		}
	}
}
