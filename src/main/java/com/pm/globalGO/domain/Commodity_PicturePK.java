package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
public class Commodity_PicturePK implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = false)
	private Long commodityID;
	@Column(nullable = false)
	private int pictureOrder;
	
	public Commodity_PicturePK() {
		
	}
	
	public Long getCommodityID() {
		return commodityID;
	}
	public void setCommodityID(Long commodityID) {
		this.commodityID=commodityID;
	}
	public int getPictureOrder() {
		return pictureOrder;
	}
	public void setPictureOrder(int pictureOrder) {
		this.pictureOrder=pictureOrder;
	}
}
