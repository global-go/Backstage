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
	private Long commodityid;
	
	@Id
	private int pictureorder;
	
	@Column(nullable = false)
	private Long pictureid;
	
	public Commodity_Picture() {
		super();
	}
	public Commodity_Picture(Long commodityID,int pictureOrder,Long pictureIndex) {
		super();
		this.commodityid=commodityID;
		this.pictureorder=pictureOrder;
		this.pictureid=pictureIndex;
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
	public Long getPictureid() {
		return pictureid;
	}
	public void setPictureid(Long pictureIndex) {
		this.pictureid=pictureIndex;
	}
}
