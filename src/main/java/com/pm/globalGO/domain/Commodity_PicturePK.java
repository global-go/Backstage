package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
public class Commodity_PicturePK implements Serializable{
	private static final long serialVersionUID = 1L;
	
	@Column(nullable = false)
	private Long commodityid;
	@Column(nullable = false)
	private int pictureorder;
	
	public Commodity_PicturePK() {
		
	}
	
	public Long getCommodityid() {
		return commodityid;
	}
	public void setCommodityid(Long commodityID) {
		this.commodityid=commodityID;
	}
	public int getPictureorder() {
		return pictureorder;
	}
	public void setPictureorder(int pictureOrder) {
		this.pictureorder=pictureOrder;
	}
}
