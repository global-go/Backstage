package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Entity
@IdClass(Commodity_PicturePK.class)
public class Commodity_Picture implements Serializable{
	
	private static final long serialVersionUID = 1L;
	@Id
	private Long commodityID;
	
	@Id
	private int pictureOrder;
	
	@Column(nullable = false)
	private Long pictureIndex;
	
	public Commodity_Picture() {
		super();
	}
	public Commodity_Picture(Long commodityID,int pictureOrder,Long pictureIndex) {
		super();
		this.commodityID=commodityID;
		this.pictureOrder=pictureOrder;
		this.pictureIndex=pictureIndex;
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
	public Long getPictureIndex() {
		return pictureIndex;
	}
	public void setPictureIndex(Long pictureIndex) {
		this.pictureIndex=pictureIndex;
	}
}
