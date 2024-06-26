package com.savvato.tribeapp.entities;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;

@Entity
@IdClass(UserRoleMapId.class)
@Table(name="user_user_role_map")
public class UserRoleMap {

	@Id
	private Long userId;
	
	public Long getUserId() {
		return userId;
	}
	
	public void setUserId(Long userId) {
		this.userId = userId;
	}
	
	@Id
	private Long userRoleId;
	
	public Long getUserRoleId() {
		return userRoleId;
	}
	
	public void setUserRoleId(Long userRoleId) {
		this.userRoleId = userRoleId;
	}
	
	/////
	public UserRoleMap(Long userId, Long userRoleId) {
		this.userId = userId;
		this.userRoleId = userRoleId;
	}
	
	public UserRoleMap() {
		
	}
}
