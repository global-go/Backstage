package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(Order_CommodityPK.class)
public class Order_Commodity implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Id
	@GeneratedValue
	private Long orderid;
	
	@Id
	@GeneratedValue
	private Long commodityid;
	
	@Column(nullable = false)
	private double transactionPrice;
	
	@Column(nullable = false)
	private int transactionNumber;

	public Order_Commodity() {
		super();
	}
	public Order_Commodity(Long orderID,Long commodityID,double transactionPrice,int transactionNumber) {
		super();
		this.orderid=orderID;
		this.commodityid=commodityID;
		this.transactionPrice=transactionPrice;
		this.transactionNumber=transactionNumber;
	}
	public Long getOrderid() {
		return orderid;
	}
	public void setOrderid(Long orderID) {
		this.orderid=orderID;
	}
	public Long getCommodityid() {
		return commodityid;
	}
	public void setCommodityid(Long commodityID) {
		this.commodityid=commodityID;
	}
	public double getTransactionPrice() {
		return transactionPrice;
	}
	public void setTransactionPrice(double transactionPrice) {
		this.transactionPrice=transactionPrice;
	}
	public int getTransactionNumber() {
		return transactionNumber;
	}
	public void setTransactionNumber(int transactionNumber) {
		this.transactionNumber=transactionNumber;
	}
}
