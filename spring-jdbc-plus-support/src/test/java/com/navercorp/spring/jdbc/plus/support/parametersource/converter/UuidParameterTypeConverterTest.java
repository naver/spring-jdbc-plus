package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import static org.assertj.core.api.Assertions.*;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class UuidParameterTypeConverterTest {
	private static UUID getUuidFromBytes(byte[] bytes) {
		ByteBuffer byteBuffer = ByteBuffer.wrap(bytes);
		long high = byteBuffer.getLong();
		long low = byteBuffer.getLong();
		return new UUID(high, low);
	}

	@Test
	void UuidToByte() {
		// given
		UuidParameterTypeConverter.UuidToByteTypeConverter sut = UuidParameterTypeConverter.UuidToByteTypeConverter.INSTANCE;
		UUID source = UUID.randomUUID();

		// when
		byte[] actual = sut.convert(source);

		// then
		assertThat(getUuidFromBytes(actual)).isEqualTo(source);
	}

	@Test
	void UuidToString() {
		// given
		UuidParameterTypeConverter.UuidToStringTypeConverter sut = UuidParameterTypeConverter.UuidToStringTypeConverter.INSTANCE;
		UUID source = UUID.randomUUID();

		// when
		String actual = sut.convert(source);

		// then
		assertThat(actual).isEqualTo(source.toString());
		assertThat(UUID.fromString(actual)).isEqualTo(source);
	}
}
