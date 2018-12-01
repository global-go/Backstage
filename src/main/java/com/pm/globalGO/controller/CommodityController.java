package com.pm.globalGO.controller;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pm.globalGO.domain.Commodity;
import com.pm.globalGO.domain.CommodityRepository;
import com.pm.globalGO.domain.Commodity_Picture;
import com.pm.globalGO.domain.Commodity_PictureRepository;
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
	
	
		//上架商品
		@ResponseBody
		@PostMapping(path = "/v1/admin/commodity")
		public String addCommodity(@RequestBody String jsonstr) {
			System.out.println("add commodity");
			
			JSONObject jsonRet = new JSONObject();
			JSONObject jsonObject = JSONObject.parseObject(jsonstr);
			int token = jsonObject.getInteger("token");
			String name = jsonObject.getString("name");
			double price = jsonObject.getDouble("price");
			int stock = jsonObject.getInteger("stock");
			String description = jsonObject.getString("description");
			
			JSONArray images = jsonObject.getJSONArray("images");//图片数组
			
			String userID = UserController.getUserIDByToken(token);
			User user=userRepository.findByUserid(userID);
			
			if (user.getType().equals("admin")) {
				Commodity commodity=new Commodity();
				commodity.setCommodityName(name);
				commodity.setPrice(price);
				commodity.setStock(stock);
				commodity.setDescription(description);
				commodity.setPictureNumber(images.size());
				commodityRepository.save(commodity);
				
				Long commodityid=commodity.getCommodityid();
				int totalCount=images.size();			
				for(int i=0;i<totalCount;i++) {
					Long imageid=images.getLongValue(i);
					
					Commodity_Picture commodity_picture=new Commodity_Picture();
					commodity_picture.setCommodityid(commodityid);
					commodity_picture.setPictureorder(i);
					commodity_picture.setPictureid(imageid);
					commodity_pictureRepository.save(commodity_picture);
				}
				
				
				//在响应json中加入错误信息
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
}
