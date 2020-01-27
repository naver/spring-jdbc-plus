package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;

public class IterableExpandPadding {
	private static final int[] REGULAR_SIZES = {0, 1, 2, 3, 4, 8, 16, 32, 50, 100, 200, 300, 500, 1000, 1500, 2000};

	public static Object expandIfIterable(Object source) {
		return expandIfIterable(source, REGULAR_SIZES);
	}

	public static Object expandIfIterable(Object source, @Nullable int[] paddingBoundaries) {
		if (source == null) {
			return null;
		}

		if (paddingBoundaries == null) {
			paddingBoundaries = REGULAR_SIZES;
		}

		if (source instanceof Collection) {
			return CollectionExpandPadding.INSTANCE.expand((Collection<?>)source, paddingBoundaries);
		} else if (source.getClass().isArray()) {
			return ArrayExpandPadding.INSTANCE.expand((Object[])source, paddingBoundaries);
		}

		return source;
	}

	private static Object[] expandRegularSizePadding(Object[] source, int[] paddingBoundaries) {
		if (source == null) {
			return null;
		}

		int sourceSize = source.length;
		if (sourceSize <= 1) {
			return source;
		}

		int targetSize = findRegularSize(paddingBoundaries, sourceSize);
		if (targetSize == 0) {
			return Arrays.copyOf(source, 0);
		}

		Object[] result = Arrays.copyOf(source, targetSize);
		Object value = source[sourceSize - 1];
		for (int i = sourceSize; i < targetSize; i++) {
			result[i] = value;
		}

		return result;
	}

	private static Collection<?> expandRegularSizePadding(Collection<?> source, int[] paddingBoundaries) {
		if (source == null) {
			return null;
		}

		int sourceSize = source.size();
		if (sourceSize <= 1) {
			return source;
		}

		int targetSize = findRegularSize(paddingBoundaries, sourceSize);
		if (targetSize == 0) {
			return Collections.emptyList();
		}

		List<Object> result = new ArrayList<>(targetSize);
		result.addAll(source);

		Object value = result.get(sourceSize - 1);
		for (int i = sourceSize; i < targetSize; i++) {
			result.add(value);
		}
		return result;
	}

	private static int findRegularSize(int[] paddingBoundaries, int n) {
		if (n < 1) {
			return 0;
		}
		for (int size : paddingBoundaries) {
			if (n <= size) {
				return size;
			}
		}

		return (((n - 1) / 100) + 1) * 100;
	}

	public enum ArrayExpandPadding {
		INSTANCE;

		public Object[] expand(Object[] source) {
			return expandRegularSizePadding(source, REGULAR_SIZES);
		}

		public Object[] expand(Object[] source, int[] paddingBoundaries) {
			return expandRegularSizePadding(source, paddingBoundaries);
		}
	}

	public enum CollectionExpandPadding {
		INSTANCE;

		public Collection<?> expand(Collection<?> source) {
			return expandRegularSizePadding(source, REGULAR_SIZES);
		}

		public Collection<?> expand(Collection<?> source, int[] paddingBoundaries) {
			return expandRegularSizePadding(source, paddingBoundaries);
		}
	}
}
