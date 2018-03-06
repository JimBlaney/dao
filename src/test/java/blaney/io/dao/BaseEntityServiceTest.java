package blaney.io.dao;

import blaney.io.models.ExampleEntity;
import io.blaney.dao.BaseEntityService;
import io.blaney.dao.BaseEntityServiceFactory;
import io.blaney.models.BaseEntity;
import junit.framework.TestCase;

public class BaseEntityServiceTest extends TestCase {

	private BaseEntityService<ExampleEntity> service = null;

	@Override
	protected void setUp() throws Exception {
		BaseEntity.setCurrentUser("TEST");
		service = BaseEntityServiceFactory.forClass(ExampleEntity.class);
	}

	@Override
	protected void tearDown() throws Exception {
		BaseEntity.resetCurrentUser();
		service = null;
	}
	
	public void testCreate() {
		
		ExampleEntity entity = new ExampleEntity();
		entity.setField1("TEST");
		
		entity = service.save(entity);
		
		assertNotNull(entity.getCreatedBy());
		
		service.delete(entity);
	}
	
	public void testUpdate() {
		
		ExampleEntity entity = new ExampleEntity();
		entity.setField1("TEST1");
		
		entity = service.save(entity);
		
		try {
			Thread.sleep(10);
		} catch (Exception e) { }
		
		entity.setField1("TEST2");
		
		entity = service.save(entity);
		
		assert(entity.getCreatedOn() != entity.getLastModifiedOn());
		
		service.delete(entity);
	}
	
	public void testDelete() {
		
		ExampleEntity entity = new ExampleEntity();
		entity.setField1("TEST1");
		
		entity = service.save(entity);
		
		assert(service.getCount() == 1L);
		
		service.delete(entity);
		
		assert(service.getCount() == 0L);
	}
}
