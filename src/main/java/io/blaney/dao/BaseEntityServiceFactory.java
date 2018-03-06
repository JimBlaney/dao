package io.blaney.dao;

import io.blaney.models.BaseEntity;

public final class BaseEntityServiceFactory {

	public static <T extends BaseEntity> BaseEntityService<T> forClass(Class<T> clazz) {
		return new BaseEntityService<T>(clazz);
	}
}
