package blaney.io.models;

import javax.persistence.Entity;

import io.blaney.models.BaseEntity;

@Entity
public class ExampleEntity extends BaseEntity {

	private static final long serialVersionUID = 1L;

	private String field1 = null;

	public String getField1() {
		return field1;
	}

	public void setField1(String field1) {
		this.field1 = field1;
	}
}