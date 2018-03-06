package io.blaney.models.generators;

import org.hibernate.Session;
import org.hibernate.tuple.ValueGenerator;

import io.blaney.models.BaseEntity;

public class CurrentUserGenerator implements ValueGenerator<String> {

	@Override
	public String generateValue(Session session, Object owner) {
		return BaseEntity.getCurrentUser();
	}
}
