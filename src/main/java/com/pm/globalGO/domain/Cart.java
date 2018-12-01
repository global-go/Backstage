package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(CartPK.class)
public class Cart implements Serializable{

	private static final long serialVersionUID = 1L;
	@Id
	private String userid;
	
	@Id
	private Long commodityid;
	
	@Column(nullable = false)
	private int cartNumber;
	
	public Cart() {
		super();
	}
	
	public Cart(String userID,Long commodityID,int cartNumber)
	{
		super();
		this.userid=userID;
		this.commodityid=commodityID;
		this.cartNumber=cartNumber;
	}
	public String getUserid() {
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
	public int getCartNumber() {
		return cartNumber;
	}
	public void setCartNumber(int cartNumber) {
		this.cartNumber=cartNumber;
	}
}
