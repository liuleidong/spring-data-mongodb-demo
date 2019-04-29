package com.simple2l.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection = "access")
public class Access {
	@Id
	private String id;
	private String userId;
	private String level;
	private String group;
	
	public Access() {
		// TODO Auto-generated constructor stub
	}
	
	public Access(String level,String group) {
		// TODO Auto-generated constructor stub
		this.level = level;
		this.group = group;
	}
	
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getUserId() {
		return userId;
	}
	public void setUserId(String userId) {
		this.userId = userId;
	}
	public String getLevel() {
		return level;
	}
	public void setLevel(String level) {
		this.level = level;
	}
	public String getGroup() {
		return group;
	}
	public void setGroup(String group) {
		this.group = group;
	}
}
