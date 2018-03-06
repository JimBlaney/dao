package io.blaney.models.generators;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

public class CurrentDateGenerator implements ValueGenerator<Date> {

	@Override
	public Date generateValue(Session session, Object owner) {

		return Calendar.getInstance(TimeZone.getTimeZone("GMT")).getTime();
	}
}
