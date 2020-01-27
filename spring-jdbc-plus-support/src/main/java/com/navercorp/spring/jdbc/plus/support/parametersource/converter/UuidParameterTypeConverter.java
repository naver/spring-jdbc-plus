package com.navercorp.spring.jdbc.plus.support.parametersource.converter;

import java.nio.ByteBuffer;
import java.util.UUID;

import org.springframework.core.convert.converter.Converter;

public class UuidParameterTypeConverter {
	public enum UuidToByteTypeConverter implements Converter<UUID, byte[]> {
		INSTANCE;

		@Override
		public byte[] convert(UUID source) {
			ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
			bb.putLong(source.getMostSignificantBits());
			bb.putLong(source.getLeastSignificantBits());
			return bb.array();
		}
	}

	public enum UuidToStringTypeConverter implements Converter<UUID, String> {
		INSTANCE;

		@Override
		public String convert(UUID source) {
			return source.toString();
		}
	}
}
