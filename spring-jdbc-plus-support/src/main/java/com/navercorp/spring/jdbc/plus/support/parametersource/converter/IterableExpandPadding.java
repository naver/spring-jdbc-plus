/*
 * Spring JDBC Plus
 *
 * Copyright 2020-present NAVER Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.lang.Nullable;

/**
 * The type Iterable expand padding.
 *
 * @author Myeonghyeon Lee
 */
public class IterableExpandPadding {
	private static final int[] REGULAR_SIZES = {0, 1, 2, 3, 4, 8, 16, 32, 50, 100, 200, 300, 500, 1000, 1500, 2000};

	/**
	 * Expand if iterable object.
	 *
	 * @param source the source
	 * @return the object
	 */
	public static Object expandIfIterable(Object source) {
		return expandIfIterable(source, REGULAR_SIZES);
	}

	/**
	 * Expand if iterable object.
	 *
	 * @param source            the source
	 * @param paddingBoundaries the padding boundaries
	 * @return the object
	 */
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

	private static int findRegularSize(int[] paddingBoundaries, int num) {
		if (num < 1) {
			return 0;
		}
		for (int size : paddingBoundaries) {
			if (num <= size) {
				return size;
			}
		}

		return num;
	}

	/**
	 * The enum Array expand padding.
	 */
	public enum ArrayExpandPadding {
		/**
		 * Instance array expand padding.
		 */
		INSTANCE;

		/**
		 * Expand object [ ].
		 *
		 * @param source the source
		 * @return the object [ ]
		 */
		public Object[] expand(Object[] source) {
			return expandRegularSizePadding(source, REGULAR_SIZES);
		}

		/**
		 * Expand object [ ].
		 *
		 * @param source            the source
		 * @param paddingBoundaries the padding boundaries
		 * @return the object [ ]
		 */
		public Object[] expand(Object[] source, int[] paddingBoundaries) {
			return expandRegularSizePadding(source, paddingBoundaries);
		}
	}

	/**
	 * The enum Collection expand padding.
	 */
	public enum CollectionExpandPadding {
		/**
		 * Instance collection expand padding.
		 */
		INSTANCE;

		/**
		 * Expand collection.
		 *
		 * @param source the source
		 * @return the collection
		 */
		public Collection<?> expand(Collection<?> source) {
			return expandRegularSizePadding(source, REGULAR_SIZES);
		}

		/**
		 * Expand collection.
		 *
		 * @param source            the source
		 * @param paddingBoundaries the padding boundaries
		 * @return the collection
		 */
		public Collection<?> expand(Collection<?> source, int[] paddingBoundaries) {
			return expandRegularSizePadding(source, paddingBoundaries);
		}
	}
}
