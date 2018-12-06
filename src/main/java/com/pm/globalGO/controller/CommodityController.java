package com.pm.globalGO.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pm.globalGO.domain.Commodity;
import com.pm.globalGO.domain.CommodityRepository;
import com.pm.globalGO.domain.Commodity_Picture;
import com.pm.globalGO.domain.Commodity_PictureRepository;
import com.pm.globalGO.domain.PictureRepository;
import com.pm.globalGO.domain.User;
import com.pm.globalGO.domain.UserRepository;


@RestController
public class CommodityController{
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private CommodityRepository commodityRepository;
	@Autowired
	private Commodity_PictureRepository commodity_pictureRepository;
	@Autowired
	private PictureRepository pictureRepository;
	
	//上架商品
	@ResponseBody
	@PostMapping(path = "/v1/admin/commodity")
	public String addCommodity(@RequestBody String jsonstr) {
		System.out.println("add commodity");
		
		JSONObject jsonRet = new JSONObject();
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token = jsonObject.getInteger("token");
		String name = jsonObject.getString("name");
		String category = jsonObject.getString("category");
		double price = jsonObject.getDouble("price");
		int stock = jsonObject.getInteger("stock");
		String description = jsonObject.getString("description");
		
		JSONArray images = jsonObject.getJSONArray("images");//图片数组
	
		
		String userID = UserController.getUserIDByToken(token);
		if (userID==null) {
			jsonRet.put("code",-1);
			jsonRet.put("errMessage","token失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		User user=userRepository.findByUserid(userID);
		
		if (user.getType().equals("admin")) {
			Commodity commodity=new Commodity();
			commodity.setCommodityName(name);
			commodity.setCategory(category);
			commodity.setPrice(price);
			commodity.setStock(stock);
			commodity.setDescription(description);
			commodity.setPictureNumber(images.size());
			commodityRepository.save(commodity);
			
			Long commodityid=commodity.getCommodityid();		
			
			JSONArray imageArray = new JSONArray();
			
			for(int i=0;i<images.size();i++) {
				Long imageid=images.getLongValue(i);
				Commodity_Picture commodity_picture=new Commodity_Picture();
				commodity_picture.setCommodityid(commodityid);
				commodity_picture.setPictureorder(i);
				commodity_picture.setPictureid(imageid);
				commodity_pictureRepository.save(commodity_picture);
				JSONObject imageItem=new JSONObject();
				imageItem.put("id", commodity_picture.getPictureorder());
				imageItem.put("url",pictureRepository.findByPictureid(commodity_picture.getPictureid()).getPictureUrl());
				imageArray.add(imageItem);
			}
			jsonRet.put("code",0);
			jsonRet.put("errMessage","");
			JSONObject commodityInfo = new JSONObject();
			commodityInfo.put("id",commodity.getCommodityid());
			commodityInfo.put("name",commodity.getCommodityName());
			commodityInfo.put("categoty", commodity.getCategory());
			commodityInfo.put("price", commodity.getPrice());
			commodityInfo.put("stock", commodity.getStock());
			commodityInfo.put("description", commodity.getDescription());
			commodityInfo.put("images",imageArray);
			jsonRet.put("commodity", commodityInfo);
		}
		else {
			//在响应json中加入错误信息
			jsonRet.put("code",-1);
			jsonRet.put("errMessage","不具备权限");
		}
		return jsonRet.toJSONString();
	}
	
	
	
	//修改商品
		@ResponseBody
		@PutMapping(path = "/v1/admin/commodity/{commodityid}")
		public String modifyCommodity(
				@PathVariable(name = "commodityid") Long commodityid,
				@RequestBody String jsonstr) {
			System.out.println("modify commodity");
			
			JSONObject jsonRet = new JSONObject();
			JSONObject jsonObject = JSONObject.parseObject(jsonstr);
			int token = jsonObject.getInteger("token");
			String name = jsonObject.getString("name");
			String category = jsonObject.getString("category");
			double price = jsonObject.getDouble("price");
			int stock = jsonObject.getInteger("stock");
			String description = jsonObject.getString("description");
			
			JSONArray images = jsonObject.getJSONArray("images");//图片数组
		
			
			String userID = UserController.getUserIDByToken(token);
			if (userID==null) {
				jsonRet.put("code",-1);
				jsonRet.put("errMessage","token失效,请重新登陆");
				return jsonRet.toJSONString();
			}
			User user=userRepository.findByUserid(userID);
			
			if (user.getType().equals("admin")) {
				Commodity commodity= commodityRepository.findByCommodityid(commodityid);
				commodity.setCommodityName(name);
				commodity.setCategory(category);
				commodity.setPrice(price);
				commodity.setStock(stock);
				commodity.setDescription(description);
				commodity.setPictureNumber(images.size());
				commodityRepository.save(commodity);
				
				commodity_pictureRepository.deleteByCommodityid(commodityid);
				JSONArray imageArray = new JSONArray();
				
				for(int i=0;i<images.size();i++) {
					Long imageid=images.getLongValue(i);
					Commodity_Picture commodity_picture=new Commodity_Picture();
					commodity_picture.setCommodityid(commodityid);
					commodity_picture.setPictureorder(i);
					commodity_picture.setPictureid(imageid);
					commodity_pictureRepository.save(commodity_picture);
					JSONObject imageItem=new JSONObject();
					imageItem.put("id", commodity_picture.getPictureorder());
					imageItem.put("url",pictureRepository.findByPictureid(commodity_picture.getPictureid()).getPictureUrl());
					imageArray.add(imageItem);
				}
				jsonRet.put("code",0);
				jsonRet.put("errMessage","");
				JSONObject commodityInfo = new JSONObject();
				commodityInfo.put("id",commodity.getCommodityid());
				commodityInfo.put("name",commodity.getCommodityName());
				commodityInfo.put("category", commodity.getCategory());
				commodityInfo.put("price", commodity.getPrice());
				commodityInfo.put("stock", commodity.getStock());
				commodityInfo.put("description", commodity.getDescription());
				commodityInfo.put("images",imageArray);
				jsonRet.put("commodity", commodityInfo);
			}
			else {
				//在响应json中加入错误信息
				jsonRet.put("code",-1);
				jsonRet.put("errMessage","不具备权限");
			}
			return jsonRet.toJSONString();
		}
		
		//删除商品
		@ResponseBody
		@DeleteMapping(path = "/v1/admin/commodity/{commodityid}")
		public String deleteCommodity(
				@PathVariable(name = "commodityid") Long commodityid,
				@RequestParam("token") int token) {
			System.out.println("delete commodity");
			
			JSONObject jsonRet = new JSONObject();
			String userID = UserController.getUserIDByToken(token);
			if (userID==null) {
				jsonRet.put("code",-1);
				jsonRet.put("errMessage","token失效,请重新登陆");
				return jsonRet.toJSONString();
			}
			
			User user=userRepository.findByUserid(userID);
			if (user.getType().equals("admin")) {
				Commodity commodity= commodityRepository.findByCommodityid(commodityid);
				commodity.setStock(-1);
				commodityRepository.save(commodity);
				jsonRet.put("code",0);
				jsonRet.put("errMessage","");
			}
			else {
				//在响应json中加入错误信息
				jsonRet.put("code",-1);
				jsonRet.put("errMessage","不具备权限");
			}
			return jsonRet.toJSONString();
		}
		
	//首页信息
	@ResponseBody
	@GetMapping(path = "/v1/index/info")
	public String index() {
		System.out.println("IndexInfo");
		JSONObject jsonRet = new JSONObject();
		
		List<Commodity> commodities=commodityRepository.findAll();
		JSONArray list=new JSONArray();
		
		for(int i=0;i<commodities.size();i++) {
			JSONObject listitem=new JSONObject();
			Commodity commodity=commodities.get(i);
			if (commodity.getStock()<0)
				continue;
			
			listitem.put("id", commodity.getCommodityid());
			listitem.put("name",commodity.getCommodityName());
			listitem.put("category", commodity.getCategory());
			listitem.put("price",commodity.getPrice());
			listitem.put("stock",commodity.getStock());
			listitem.put("description",commodity.getDescription());
			
			JSONArray images=new JSONArray();
			List<Commodity_Picture> pictures=commodity_pictureRepository.findByCommodityid(commodity.getCommodityid());
			for(int j=0;j<pictures.size();j++) {
				JSONObject image=new JSONObject();
				Commodity_Picture picture = pictures.get(j);
				image.put("id",picture.getPictureid());
			    image.put("order",picture.getPictureorder());
			    image.put("url",pictureRepository.findByPictureid(picture.getPictureid()).getPictureUrl());
			    images.add(image);
			}
			listitem.put("images",images);
			list.add(listitem);
		}
		jsonRet.put("commodities",list);
		jsonRet.put("code", 0);
		jsonRet.put("errMessage", "");
		
		
		return jsonRet.toJSONString();
	}
	
		
}
