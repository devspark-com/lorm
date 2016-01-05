package org.devspark.aws.lorm;

import java.util.HashMap;
import java.util.Map;

import org.devspark.aws.lorm.EntityManager;
import org.devspark.aws.lorm.Repository;
import org.devspark.aws.lorm.mapping.EntityToItemMapper;
import org.devspark.aws.lorm.mapping.ItemToEntityMapper;
import org.devspark.aws.lorm.schema.validation.EntitySchemaSupport;
import org.devspark.aws.lorm.test.model.Category;
import org.devspark.aws.lorm.test.model.Expense;
import org.devspark.aws.lorm.test.model.Merchant;
import org.devspark.aws.lorm.test.model.Reporter;

public class DummyEntityManager implements EntityManager {
	private final Map<Class<?>, Repository<?>> repositories;

	private final static EntityManager entityManager;

	static {
		entityManager = new DummyEntityManager();
	}

	private DummyEntityManager() {
		repositories = new HashMap<Class<?>, Repository<?>>();
		addEntity(Expense.class, null, null, null);
		addEntity(Category.class, null, null, null);
		addEntity(Merchant.class, null, null, null);
		addEntity(Reporter.class, null, null, null);
	}

	@Override
	public <T> void addEntity(Class<T> entityClass,
			EntityToItemMapper entityToItemMapper,
			ItemToEntityMapper<T> itemToEntityMapper,
			EntitySchemaSupport entitySchemaSupport) {
		repositories.put(entityClass, new DummyRepository<T>(entityClass));
	}

	public static EntityManager get() {
		return entityManager;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Repository<T> getRepository(Class<T> entityClass) {
		return (Repository<T>) repositories.get(entityClass);
	}

}
