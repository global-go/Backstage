package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Commodity implements Serializable{
	private static final long serialVersionUID = 1L;
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long commodityid;
	
	@Column(nullable = false)
	private String commodityName;
	
	@Column(nullable = false)
	private double price;
	
	@Column(nullable = false)
	private int stock;
	
	@Column(nullable = false)
	private String classification;
	
	@Column(nullable = false)
	private String description;
	
	@Column(nullable = false)
	private int pictureNumber;
	
	public Commodity() {
		super();
	}
	
	public Commodity(Long commodityID,String commodityName,double price,int stock,String description,int pictureNumber) {
		super();
		this.commodityid=commodityID;
		this.commodityName=commodityName;
		this.price=price;
		this.stock=stock;
		this.description=description;
		this.pictureNumber=pictureNumber;
	}

	public Long getCommodityid() {
		return commodityid;
	}
	public void setCommodityid(Long commodityID) {
		this.commodityid=commodityID;
	}
	
	public String getCommodityName() {
		return commodityName;
	}	
	public void setCommodityName(String commodityName) {
		this.commodityName=commodityName;
	}	
	
	public double getPrice() {
		return price;
	}
	public void setPrice(double price) {
		this.price=price;
	}
	
	public int getStock() {
		return stock;
	}
	public void setStock(int stock){
		this.stock=stock;
	}
	
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description=description;
	}
	
	public int getPictureNumber()
	{
		return pictureNumber;
	}
	public void setPictureNumber(int pictureNumber) {
		this.pictureNumber=pictureNumber;
	}
}
