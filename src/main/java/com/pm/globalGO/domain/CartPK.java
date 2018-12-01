package com.pm.globalGO.domain;

import java.io.Serializable;

public class CartPK implements Serializable{

	private String userid;
	private Long commodityid;
	
	public String getUserID() {
		return userid;
	}
	public void setUserid(String userID) {
		this.userid=userID;
	}
	public Long getCommodityid() {
		return commodityid;
	}
	public void setCommodityid(Long commodityID) {
		this.commodityid=commodityID;
	}
}
