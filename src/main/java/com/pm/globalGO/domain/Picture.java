package com.pm.globalGO.domain;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
@Entity
public class Picture implements Serializable{
	
	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long pictureid;
	
	@Column(nullable = false)
	private String pictureUrl;

	public Picture() {
		super();
	}
	
	public Picture(Long pictureIndex,String pictureURL) {
		super();
		this.pictureid=pictureIndex;
		this.pictureUrl=pictureURL;
	}
	public Long getPictureid() {
		return pictureid;
	}
	public void setPictureid(Long pictureIndex) {
		this.pictureid=pictureIndex;
	}
	public String getPictureUrl() {
		return pictureUrl;
	}
	public void setPictureUrl(String pictureURL) {
		this.pictureUrl=pictureURL;
	}
}
