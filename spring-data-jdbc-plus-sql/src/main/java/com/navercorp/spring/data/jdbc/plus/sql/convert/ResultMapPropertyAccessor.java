package com.navercorp.spring.data.jdbc.plus.sql.convert;

import java.util.Map;

import org.springframework.expression.EvaluationContext;
import org.springframework.expression.PropertyAccessor;
import org.springframework.expression.TypedValue;
import org.springframework.lang.Nullable;

/**
 * Refer org.springframework.data.jdbc.core.convert.ResultSetAccessorPropertyAccessor
 */
class ResultMapPropertyAccessor implements PropertyAccessor {

	static final PropertyAccessor INSTANCE = new ResultMapPropertyAccessor();

	@Override
	public Class<?>[] getSpecificTargetClasses() {
		return new Class<?>[] { Map.class };
	}

	@Override
	public boolean canRead(EvaluationContext context, @Nullable Object target, String name) {
		return target instanceof Map<?, ?> map && map.containsKey(name);
	}

	@Override
	public TypedValue read(EvaluationContext context, @Nullable Object target, String name) {

		if (target == null) {
			return TypedValue.NULL;
		}

		Object value = ((Map<?, ?>) target).get(name);

		if (value == null) {
			return TypedValue.NULL;
		}

		return new TypedValue(value);
	}

	@Override
	public boolean canWrite(EvaluationContext context, Object target, String name) {
		return false;
	}

	@Override
	public void write(EvaluationContext context, Object target, String name, Object newValue) {
		throw new UnsupportedOperationException();
	}
}
