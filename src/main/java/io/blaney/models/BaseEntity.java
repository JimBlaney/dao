package io.blaney.models;

import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;

import io.blaney.models.generators.CurrentDateGenerator;
import io.blaney.models.generators.CurrentUserGenerator;

@MappedSuperclass
public abstract class BaseEntity implements Serializable {

	private static final long serialVersionUID = 1L;

	private static ThreadLocal<String> currentUser = new ThreadLocal<String>();

	public static final String getCurrentUser() {
		return currentUser.get();
	}
	
	public static final void setCurrentUser(String user) {
		currentUser.set(user);
	}
	
	public static final void resetCurrentUser() {
		currentUser.remove();
	}

	private String id = UUID.randomUUID().toString();
	private String createdBy = null;
	private Date createdOn = null;
	private String lastModifiedBy = null;
	private Date lastModifiedOn = null;

	@Id
	@Column(length = 36)
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	@Column
	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	@Column
	public Date getCreatedOn() {
		return createdOn;
	}

	public void setCreatedOn(Date createdOn) {
		this.createdOn = createdOn;
	}

	@Column
	public String getLastModifiedBy() {
		return lastModifiedBy;
	}

	public void setLastModifiedBy(String lastModifiedBy) {
		this.lastModifiedBy = lastModifiedBy;
	}

	@Column
	public Date getLastModifiedOn() {
		return lastModifiedOn;
	}

	public void setLastModifiedOn(Date lastModifiedOn) {
		this.lastModifiedOn = lastModifiedOn;
	}

	@PrePersist
	public void prePersist() {
		
		String user = new CurrentUserGenerator().generateValue(null, this);
		Date now = new CurrentDateGenerator().generateValue(null, this);
		
		this.setCreatedBy(user);
		this.setCreatedOn(now);
		
		this.setLastModifiedBy(user);
		this.setLastModifiedOn(now);
	}
	
	@PreUpdate
	public void preUpdate() {
		
		String user = new CurrentUserGenerator().generateValue(null, this);
		Date now = new CurrentDateGenerator().generateValue(null, this);
		
		this.setLastModifiedBy(user);
		this.setLastModifiedOn(now);
	}

	@Override
	public int hashCode() {
		return this.getId().hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		} else if (obj == null) {
			return false;
		} else if (!(obj instanceof BaseEntity)) {
			return false;
		} else {
			return this.getId().equals(((BaseEntity) obj).getId());
		}
	}
	
	@Override
	public String toString() {
		return String.format("%s: [%s]", this.getClass().getName(), this.getId());
	}
}
