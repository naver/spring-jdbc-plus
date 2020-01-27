package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.mapping.MappingException;
import org.springframework.lang.Nullable;
import org.springframework.util.LinkedCaseInsensitiveMap;

/**
 * Wrapper value object for a {@link java.sql.ResultSet} to be able to access raw values by
 * {@link org.springframework.data.relational.core.mapping.RelationalPersistentProperty} references. Provides fast
 * lookup of columns by name, including for absent columns.
 *
 * @author Jens Schauder
 * @author Mark Paluch
 * @since 2.0
 *
 * Copy org.springframework.data.jdbc.core.convert.ResultSetAccessoor
 * Verified: c0803ddafef7a4bc4ec070df6581d46c4d59ff4a
 */
class ResultSetAccessor {

	private static final Logger LOG = LoggerFactory.getLogger(ResultSetAccessor.class);

	private final ResultSet resultSet;

	private final Map<String, Integer> indexLookUp;

	ResultSetAccessor(ResultSet resultSet) {

		this.resultSet = resultSet;
		this.indexLookUp = indexColumns(resultSet);
	}

	private static Map<String, Integer> indexColumns(ResultSet resultSet) {

		try {

			ResultSetMetaData metaData = resultSet.getMetaData();
			int columnCount = metaData.getColumnCount();

			Map<String, Integer> index = new LinkedCaseInsensitiveMap<>(columnCount);

			for (int i = 1; i <= columnCount; i++) {

				String label = metaData.getColumnLabel(i);

				if (index.containsKey(label)) {
					LOG.warn("ResultSet contains {} multiple times", label);
					continue;
				}

				index.put(label, i);
			}

			return index;
		} catch (SQLException se) {
			throw new MappingException("Cannot obtain result metadata", se);
		}
	}

	/**
	 * Returns the value if the result set contains the {@code columnName}.
	 *
	 * @param columnName the column name (label).
	 * @return
	 * @see ResultSet#getObject(int)
	 */
	@Nullable
	public Object getObject(String columnName) {

		try {

			int index = findColumnIndex(columnName);
			return index > 0 ? resultSet.getObject(index) : null;
		} catch (SQLException o_O) {
			throw new MappingException(String.format("Could not read value %s from result set!", columnName), o_O);
		}
	}

	private int findColumnIndex(String columnName) {
		return indexLookUp.getOrDefault(columnName, -1);
	}

	/**
	 * Returns {@literal true} if the result set contains the {@code columnName}.
	 *
	 * @param columnName the column name (label).
	 * @return
	 */
	public boolean hasValue(String columnName) {
		return indexLookUp.containsKey(columnName);
	}
}
