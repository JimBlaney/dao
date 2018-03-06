package io.blaney.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.TypedQuery;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.blaney.models.BaseEntity;

public final class BaseEntityService<T extends BaseEntity> {

	private static final Logger log = LoggerFactory.getLogger(BaseEntityService.class);

	private static final EntityManagerFactory entityManagerFactory;
	private static final String persistenceUnitName;

	static {
		String puName = null;
		try {
			String packagePath = BaseEntityService.class.getPackage().getName().replace('.', ',');

			Properties daoProps = new Properties();
			daoProps.load(
					BaseEntityService.class.getClassLoader().getResourceAsStream(packagePath + "/dao.properties"));
			puName = daoProps.getProperty("persistenceUnit.name");
		} catch (Exception e) {
			log.error("failed to read persistenceUnit.name from dao.properties");
		} finally {
			persistenceUnitName = puName;
		}

		entityManagerFactory = Persistence.createEntityManagerFactory(persistenceUnitName);
	}

	public static final EntityManagerFactory getEntityManagerFactory() {
		return entityManagerFactory;
	}

	public static final void shutdown() {
		entityManagerFactory.close();
	}

	private final Class<T> entityClass;

	public BaseEntityService(final Class<T> clazz) {
		this.entityClass = clazz;
	}

	public final T save(final T entity) {
		return doTransaction(BaseEntityService::getEntityManagerFactory, entityManager -> {
			return entityManager.merge(entity);
		});
	}

	public final void delete(final T entity) {
		doTransaction(BaseEntityService::getEntityManagerFactory, entityManager -> {
			entityManager.remove(entityManager.contains(entity) ? entity : entityManager.merge(entity));
		});
	}

	public final T find(String id) {
		return doTransactionless(BaseEntityService::getEntityManagerFactory, entityManager -> {
			return entityManager.find(entityClass, id);
		});
	}

	public final List<T> find() {
		return find(null, null);
	}

	public final List<T> find(final String query, final Map<String, Object> params) {
		return doTransactionless(BaseEntityService::getEntityManagerFactory, entityManager -> {
			StringBuilder builder = new StringBuilder("select e from ").append(entityClass.getName()).append(" e");
			if (query != null && query.length() > 0) {
				builder.append(" where ").append(query);
			}
			TypedQuery<T> tq = entityManager.createQuery(builder.toString(), entityClass);
			if (params != null) {
				for (Entry<String, Object> param : params.entrySet()) {
					tq.setParameter(param.getKey(), param.getValue());
				}
			}
			return tq.getResultList();
		});
	}

	public final long getCount() {
		return getCount(null, null);
	}

	public final long getCount(final String query, final Map<String, Object> params) {
		return doTransactionless(BaseEntityService::getEntityManagerFactory, entityManager -> {
			StringBuilder builder = new StringBuilder("select count(e) from ").append(entityClass.getName())
					.append(" e");
			if (query != null && query.length() > 0) {
				builder.append(" where ").append(query);
			}
			TypedQuery<Long> tq = entityManager.createQuery(builder.toString(), Long.class);
			if (params != null) {
				for (Entry<String, Object> param : params.entrySet()) {
					tq.setParameter(param.getKey(), param.getValue());
				}
			}
			return tq.getSingleResult();
		});
	}

	public final <V> List<V> getDistinctValues(final String propertyName, final Class<V> propertyClass) {
		return getDistinctValues(propertyName, propertyClass, null, null);
	}

	public final <V> List<V> getDistinctValues(final String propertyName, final Class<V> propertyClass,
			final String query, final Map<String, Object> params) {
		return doTransactionless(BaseEntityService::getEntityManagerFactory, entityManager -> {
			StringBuilder builder = new StringBuilder("select distinct(e.").append(propertyName).append(") from ")
					.append(entityClass.getName()).append(" e");
			if (query != null && query.length() > 0) {
				builder.append(" where ").append(query);
			}
			TypedQuery<V> tq = entityManager.createQuery(builder.toString(), propertyClass);
			if (params != null) {
				for (Entry<String, Object> param : params.entrySet()) {
					tq.setParameter(param.getKey(), param.getValue());
				}
			}
			return tq.getResultList();
		});
	}

	public final static <V> V doTransaction(final Supplier<EntityManagerFactory> factorySupplier,
			final Function<EntityManager, V> function) {
		V result = null;
		EntityManager entityManager = null;
		EntityTransaction tx = null;
		try {
			entityManager = factorySupplier.get().createEntityManager();
			tx = entityManager.getTransaction();
			tx.begin();
			result = function.apply(entityManager);
			if (!tx.getRollbackOnly()) {
				tx.commit();
			} else {
				try {
					tx.rollback();
				} catch (Exception e) {
					log.error("failed to rollback", e);
				}
			}
		} catch (Throwable t) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();
				} catch (Exception e) {
					log.error("failed to rollback", e);
				}
			}
			throw t;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return result;
	}

	public final static void doTransaction(final Supplier<EntityManagerFactory> factorySupplier,
			final Consumer<EntityManager> function) {
		EntityManager entityManager = null;
		EntityTransaction tx = null;
		try {
			entityManager = factorySupplier.get().createEntityManager();
			tx = entityManager.getTransaction();
			tx.begin();
			function.accept(entityManager);
			if (!tx.getRollbackOnly()) {
				tx.commit();
			} else {
				try {
					tx.rollback();
				} catch (Exception e) {
					log.error("failed to rollback", e);
				}
			}
		} catch (Throwable t) {
			if (tx != null && tx.isActive()) {
				try {
					tx.rollback();
				} catch (Exception e) {
					log.error("failed to rollback", e);
				}
			}
			throw t;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}

	public final static <V> V doTransactionless(final Supplier<EntityManagerFactory> factorySupplier,
			final Function<EntityManager, V> function) {
		V result = null;
		EntityManager entityManager = null;
		try {
			entityManager = factorySupplier.get().createEntityManager();
			result = function.apply(entityManager);
		} catch (Throwable t) {
			throw t;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
		return result;
	}

	public final static void doTransactionless(final Supplier<EntityManagerFactory> factorySupplier,
			final Consumer<EntityManager> function) {
		EntityManager entityManager = null;
		try {
			entityManager = factorySupplier.get().createEntityManager();
			function.accept(entityManager);
		} catch (Throwable t) {
			throw t;
		} finally {
			if (entityManager != null) {
				entityManager.close();
			}
		}
	}
}
