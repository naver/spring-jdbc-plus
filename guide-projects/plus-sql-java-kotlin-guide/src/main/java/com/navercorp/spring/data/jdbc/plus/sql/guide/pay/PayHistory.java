package com.navercorp.spring.data.jdbc.plus.sql.guide.pay;

import java.util.concurrent.atomic.AtomicLong;

import org.jspecify.annotations.Nullable;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.Table;

import lombok.With;

import com.navercorp.spring.data.jdbc.plus.sql.guide.pay.PayHistory.PayHistoryId;

@Table("n_pay_hist")
public record PayHistory(
	@Id
	@With
	@Embedded.Empty
	PayHistoryId id,

	long orderId,

	@Column("pay_amt")
	long amount
) implements Persistable<PayHistoryId> {
	public static PayHistory from(Pay pay) {
		return new PayHistory(
			PayHistoryId.from(pay),
			pay.orderId(),
			pay.amount().longValue()
		);
	}

	@Override
	public PayHistoryId getId() {
		return id;
	}

	@Override
	public boolean isNew() {
		return id.payHistoryNo == null;
	}

	public PayHistory generateId() {
		return new PayHistory(id.generate(), orderId, amount);
	}

	public record PayHistoryId(
		@Column("pay_no")
		long payNo,

		@With
		@Nullable
		@Column("pay_hist_no")
		Long payHistoryNo
	) {
		private static final AtomicLong SEQUENCE = new AtomicLong(0);

		public PayHistoryId generate() {
			return new PayHistoryId(payNo, SEQUENCE.incrementAndGet());
		}

		static PayHistoryId from(Pay pay) {
			if (pay.id() == null) {
				throw new IllegalStateException("pay id is null");
			}

			return new PayHistoryId(
				pay.id(),
				null
			);
		}
	}
}
