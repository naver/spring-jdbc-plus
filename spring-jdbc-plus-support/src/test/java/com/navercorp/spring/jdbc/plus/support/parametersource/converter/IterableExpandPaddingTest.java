/*
 * Spring JDBC Plus
 *
 * Copyright 2020-2025 NAVER Corp.
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

import static com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding.ArrayExpandPadding;
import static com.navercorp.spring.jdbc.plus.support.parametersource.converter.IterableExpandPadding.CollectionExpandPadding;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * @author Myeonghyeon Lee
 */
class IterableExpandPaddingTest {
	private static final Random RANDOM = new Random();

	private static String[] array(int size) {
		return RANDOM.ints(size)
			.mapToObj(String::valueOf)
			.toArray(String[]::new);
	}

	private static List<String> list(int size) {
		return RANDOM.ints(size)
			.mapToObj(String::valueOf)
			.toList();
	}

	@Test
	void expandIfIterable() {
		//noinspection DataFlowIssue
		assertThat(IterableExpandPadding.expandIfIterable(null, true)).isNull();

		Map<String, Object> map = Collections.singletonMap("key", "value");
		assertThat(IterableExpandPadding.expandIfIterable(map, true)).isSameAs(map);

		String text = "sample";
		assertThat(IterableExpandPadding.expandIfIterable(text, true)).isSameAs(text);

		List<String> list = Arrays.asList("1", "2", "3", "4", "5");
		assertThat(
			(Collection<?>)IterableExpandPadding.expandIfIterable(list, true)
		).hasSize(8);

		String[] array = new String[] {"1", "2", "3", "4", "5"};
		assertThat(
			(String[])IterableExpandPadding.expandIfIterable(array, true)
		).hasSize(8);
	}

	@Test
	@DisplayName("paddingBoundaries 를 넘겨서 padding 합니다.")
	void expandIfIterableWithBoundaries() {
		int[] paddingBoundaries = {5, 10};
		Map<String, Object> map = Collections.singletonMap("key", "value");
		assertThat(IterableExpandPadding.expandIfIterable(map, true, paddingBoundaries)).isSameAs(map);

		String text = "sample";
		assertThat(IterableExpandPadding.expandIfIterable(text, true, paddingBoundaries)).isSameAs(text);

		List<String> list = Arrays.asList("1", "2", "3", "4", "5", "6");
		assertThat(
			(Collection<?>)IterableExpandPadding.expandIfIterable(list, true, paddingBoundaries)
		).hasSize(10);

		String[] array = new String[] {"1", "2", "3", "4"};
		assertThat(
			(String[])IterableExpandPadding.expandIfIterable(array, true, paddingBoundaries)
		).hasSize(5);
	}

	@Test
	@DisplayName("list 가 1 이면, paddingBoundaries 와 무관하게 그대로 반환합니다. ")
	void returnSourceIfSourceSizeOne() {
		int[] paddingBoundaries = {5, 10};
		Map<String, Object> map = Collections.singletonMap("key", "value");
		assertThat(IterableExpandPadding.expandIfIterable(map, true, paddingBoundaries)).isSameAs(map);

		String text = "sample";
		assertThat(IterableExpandPadding.expandIfIterable(text, true, paddingBoundaries)).isSameAs(text);

		List<String> list = Collections.singletonList("1");
		assertThat(
			(Collection<?>)IterableExpandPadding.expandIfIterable(list, true, paddingBoundaries)
		).hasSize(1);

		String[] array = new String[] {"1"};
		assertThat(
			(String[])IterableExpandPadding.expandIfIterable(array, true, paddingBoundaries)
		).hasSize(1);
	}

	@Test
	@DisplayName("padding 을 채워 균일한 크기의 Array 로 변환합니다.")
	void arrayConverter() {
		ArrayExpandPadding sut = ArrayExpandPadding.INSTANCE;

		@SuppressWarnings("DataFlowIssue") String[] actualNull = (String[])sut.expand(null);
		assertThat(actualNull).isNull();

		String[] actualEmpty = (String[])sut.expand(new String[0]);
		assertThat(actualEmpty).isEmpty();

		String[] actual1 = (String[])sut.expand(array(1));
		assertThat(actual1).hasSize(1);

		String[] actual4 = (String[])sut.expand(array(4));
		assertThat(actual4).hasSize(4);

		String[] actual8 = (String[])sut.expand(array(7));
		assertThat(actual8).hasSize(8);
		assertThat(actual8[7]).isEqualTo(actual8[6]);

		String[] actual16 = (String[])sut.expand(array(12));
		assertThat(actual16).hasSize(16);
		assertThat(actual16[12]).isEqualTo(actual16[11]);
		assertThat(actual16[13]).isEqualTo(actual16[11]);
		assertThat(actual16[14]).isEqualTo(actual16[11]);
		assertThat(actual16[15]).isEqualTo(actual16[11]);

		String[] actual32 = (String[])sut.expand(array(30));
		assertThat(actual32).hasSize(32);
		assertThat(actual32[30]).isEqualTo(actual32[29]);
		assertThat(actual32[31]).isEqualTo(actual32[29]);

		String[] actual50 = (String[])sut.expand(array(40));
		assertThat(actual50).hasSize(50);
		assertThat(actual50[40]).isEqualTo(actual50[39]);
		assertThat(actual50[41]).isEqualTo(actual50[39]);
		assertThat(actual50[45]).isEqualTo(actual50[39]);
		assertThat(actual50[49]).isEqualTo(actual50[39]);

		String[] actual100 = (String[])sut.expand(array(70));
		assertThat(actual100).hasSize(100);
		assertThat(actual100[70]).isEqualTo(actual100[69]);
		assertThat(actual100[75]).isEqualTo(actual100[69]);
		assertThat(actual100[80]).isEqualTo(actual100[69]);
		assertThat(actual100[90]).isEqualTo(actual100[69]);
		assertThat(actual100[99]).isEqualTo(actual100[69]);

		String[] size100 = (String[])sut.expand(array(100));
		assertThat(size100).hasSize(100);

		String[] actual200 = (String[])sut.expand(array(101));
		assertThat(actual200).hasSize(200);
		assertThat(actual200[101]).isEqualTo(actual200[100]);
		assertThat(actual200[150]).isEqualTo(actual200[100]);
		assertThat(actual200[170]).isEqualTo(actual200[100]);
		assertThat(actual200[190]).isEqualTo(actual200[100]);
		assertThat(actual200[199]).isEqualTo(actual200[100]);

		String[] size300 = (String[])sut.expand(array(201));
		assertThat(size300).hasSize(300);
		assertThat(size300[201]).isEqualTo(size300[200]);
		assertThat(size300[250]).isEqualTo(size300[200]);
		assertThat(size300[270]).isEqualTo(size300[200]);
		assertThat(size300[290]).isEqualTo(size300[200]);
		assertThat(size300[299]).isEqualTo(size300[200]);
	}

	@Test
	@DisplayName("paddingBoundaries 에 맞춰 padding 채워 균일한 크기의 Array 로 변환합니다.")
	void arrayConverterWithPaddingBoundaries() {
		int[] paddingBoundaries = {5, 10};
		ArrayExpandPadding sut = ArrayExpandPadding.INSTANCE;

		@SuppressWarnings("DataFlowIssue") String[] actualNull = (String[])sut.expand(null, paddingBoundaries);
		assertThat(actualNull).isNull();

		String[] actualEmpty = (String[])sut.expand(new String[0], paddingBoundaries);
		assertThat(actualEmpty).isEmpty();

		String[] actual5 = (String[])sut.expand(array(4), paddingBoundaries);
		assertThat(actual5).hasSize(5);

		String[] actual10 = (String[])sut.expand(array(7), paddingBoundaries);
		assertThat(actual10).hasSize(10);
		assertThat(actual10[7]).isEqualTo(actual10[6]);

		String[] actual100 = (String[])sut.expand(array(12), paddingBoundaries);
		assertThat(actual100).hasSize(12);
		assertThat(actual100[11]).isEqualTo(actual100[11]);
	}

	@Test
	@DisplayName("padding 을 채워 균일한 크기의 collection 으로 변환합니다.")
	@SuppressWarnings("unchecked")
	void collectionConverter() {
		CollectionExpandPadding sut = CollectionExpandPadding.INSTANCE;

		@SuppressWarnings("DataFlowIssue") List<String> actualNull = (List<String>)sut.expand(null);
		assertThat(actualNull).isNull();

		List<String> actualEmpty = (List<String>)sut.expand(Collections.emptyList());
		assertThat(actualEmpty).isEmpty();

		List<String> actual1 = (List<String>)sut.expand(list(1));
		assertThat(actual1).hasSize(1);

		List<String> actual4 = (List<String>)sut.expand(list(4));
		assertThat(actual4).hasSize(4);

		List<String> actual8 = (List<String>)sut.expand(list(7));
		assertThat(actual8).hasSize(8);
		assertThat(actual8.get(7)).isEqualTo(actual8.get(6));

		List<String> actual16 = (List<String>)sut.expand(list(12));
		assertThat(actual16).hasSize(16);
		assertThat(actual16.get(12)).isEqualTo(actual16.get(11));
		assertThat(actual16.get(13)).isEqualTo(actual16.get(11));
		assertThat(actual16.get(14)).isEqualTo(actual16.get(11));
		assertThat(actual16.get(15)).isEqualTo(actual16.get(11));

		List<String> actual32 = (List<String>)sut.expand(list(30));
		assertThat(actual32).hasSize(32);
		assertThat(actual32.get(30)).isEqualTo(actual32.get(29));
		assertThat(actual32.get(31)).isEqualTo(actual32.get(29));

		List<String> actual50 = (List<String>)sut.expand(list(40));
		assertThat(actual50).hasSize(50);
		assertThat(actual50.get(40)).isEqualTo(actual50.get(39));
		assertThat(actual50.get(41)).isEqualTo(actual50.get(39));
		assertThat(actual50.get(45)).isEqualTo(actual50.get(39));
		assertThat(actual50.get(49)).isEqualTo(actual50.get(39));

		List<String> actual100 = (List<String>)sut.expand(list(70));
		assertThat(actual100).hasSize(100);
		assertThat(actual100.get(70)).isEqualTo(actual100.get(69));
		assertThat(actual100.get(75)).isEqualTo(actual100.get(69));
		assertThat(actual100.get(80)).isEqualTo(actual100.get(69));
		assertThat(actual100.get(90)).isEqualTo(actual100.get(69));
		assertThat(actual100.get(99)).isEqualTo(actual100.get(69));

		List<String> size100 = (List<String>)sut.expand(list(100));
		assertThat(size100).hasSize(100);

		List<String> actual200 = (List<String>)sut.expand(list(101));
		assertThat(actual200).hasSize(200);
		assertThat(actual200.get(101)).isEqualTo(actual200.get(100));
		assertThat(actual200.get(150)).isEqualTo(actual200.get(100));
		assertThat(actual200.get(170)).isEqualTo(actual200.get(100));
		assertThat(actual200.get(190)).isEqualTo(actual200.get(100));
		assertThat(actual200.get(199)).isEqualTo(actual200.get(100));

		List<String> size300 = (List<String>)sut.expand(list(201));
		assertThat(size300).hasSize(300);
		assertThat(size300.get(201)).isEqualTo(size300.get(200));
		assertThat(size300.get(250)).isEqualTo(size300.get(200));
		assertThat(size300.get(270)).isEqualTo(size300.get(200));
		assertThat(size300.get(290)).isEqualTo(size300.get(200));
		assertThat(size300.get(299)).isEqualTo(size300.get(200));
	}

	@Test
	@DisplayName("paddingBoundaries 에 맞춰 padding 채워 균일한 크기의 Collection 으로 변환합니다.")
	@SuppressWarnings("unchecked")
	void collectionConverterWithPaddingBoundaries() {
		int[] paddingBoundaries = {5, 10};
		CollectionExpandPadding sut = CollectionExpandPadding.INSTANCE;

		@SuppressWarnings("DataFlowIssue") List<String> actualNull = (List<String>)sut.expand(null, paddingBoundaries);
		assertThat(actualNull).isNull();

		List<String> actualEmpty = (List<String>)sut.expand(Collections.emptyList(), paddingBoundaries);
		assertThat(actualEmpty).isEmpty();

		List<String> actual5 = (List<String>)sut.expand(list(4), paddingBoundaries);
		assertThat(actual5).hasSize(5);

		List<String> actual10 = (List<String>)sut.expand(list(7), paddingBoundaries);
		assertThat(actual10).hasSize(10);
		assertThat(actual10.get(7)).isEqualTo(actual10.get(6));

		List<String> actual100 = (List<String>)sut.expand(list(12), paddingBoundaries);
		assertThat(actual100).hasSize(12);
	}
}
