package com.pm.globalGO.domain;

import java.io.Serializable;
public class Order_CommodityPK implements Serializable {
	private Long orderid;
	private Long commodityid;
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
}
