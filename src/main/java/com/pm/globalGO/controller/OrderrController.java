package com.pm.globalGO.controller;
import com.pm.globalGO.domain.Cart;
import com.pm.globalGO.domain.Commodity;
import com.pm.globalGO.domain.CommodityRepository;
import com.pm.globalGO.domain.Commodity_Picture;
import com.pm.globalGO.domain.Commodity_PictureRepository;
import com.pm.globalGO.domain.Order_Commodity;
import com.pm.globalGO.domain.Order_CommodityRepository;
import com.pm.globalGO.domain.Orderr;
import com.pm.globalGO.domain.OrderrRepository;
import com.pm.globalGO.domain.PictureRepository;
import com.pm.globalGO.domain.User;
import com.pm.globalGO.domain.UserRepository;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONReader;
import com.pm.globalGO.controller.ResultEnum;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

import javax.print.attribute.standard.DateTimeAtCompleted;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;



@RestController
public class OrderrController{
	
	@Autowired
	private OrderrRepository orderrRepository;
	
	@Autowired
	private UserRepository userRepository;
	
	@Autowired
	private Order_CommodityRepository order_commodityRepository;
	
	@Autowired
	private CommodityRepository commodityRepository;
	
	@Autowired
	private Commodity_PictureRepository commodity_pictureRepository;
	
	@Autowired
	private PictureRepository pictureRepository;
	
	//创建订单
	@ResponseBody
	@PostMapping(path = "/v1/user/{userid}/order")
	public String addOrder(
			@PathVariable(name = "userid") String userID,
			@RequestBody String jsonstr) {
		System.out.println("add order");
		JSONObject jsonRet = new JSONObject();
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token = jsonObject.getInteger("token");
		
		String userIDFromToken= UserController.getUserIDByToken(token);
		if (userIDFromToken==null) {
			jsonRet.put("code",-1);
			jsonRet.put("errMessage","token已失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		if (userID.equals(userIDFromToken)) {
			String address = jsonObject.getString("address");
			String addressee = jsonObject.getString("addressee");
			String contact = jsonObject.getString("contact");
			JSONArray orderArray = jsonObject.getJSONArray("order");
			
			Orderr neworder = new Orderr();
			neworder.setUserid(userID);
			neworder.setAddress(address);
			neworder.setAddressee(addressee);
			neworder.setContact(contact);
			neworder.setState("pending");
			System.out.println(userID+" "+address+" "+addressee+" ");
			neworder.setTime(new Timestamp(System.currentTimeMillis()));
			orderrRepository.save(neworder);
			Long orderid=neworder.getOrderid();
			for (int i=0;i<orderArray.size();i++) {
				JSONObject orderArrayItem = orderArray.getJSONObject(i);
				Long commodityID=orderArrayItem.getLong("commodityID");
				int number=orderArrayItem.getInteger("number");
				Order_Commodity newoc=new Order_Commodity();
				newoc.setOrderid(orderid);
				newoc.setCommodityid(commodityID);
				newoc.setTransactionNumber(number);
				newoc.setTransactionPrice(commodityRepository.findByCommodityid(commodityID).getPrice());
				order_commodityRepository.save(newoc);
			}
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
		}else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "错误的id");
			return jsonRet.toJSONString();
		}
		return jsonRet.toJSONString();
	}
	
	
	//获取用户订单信息
	@ResponseBody
	@GetMapping(path="/v1/user/{userid}/order")
	public String getOrder(
			@PathVariable(name = "userid") String userID,
			@RequestParam("token") int token) {
		System.out.println("getOrder: "+userID);
		JSONObject jsonRet=new JSONObject();
		String userIDFromToken = UserController.getUserIDByToken(token);
		if (userIDFromToken == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
			return jsonRet.toJSONString();
		}

		if(userIDFromToken!=null && userID.equals(userIDFromToken)) {
			JSONArray orderArray = new JSONArray();
			List<Orderr> orderList = orderrRepository.findByUserid(userID);
			for (int i=0;i<orderList.size();i++) {
				JSONObject orderArrayItem = new JSONObject();
				Orderr order=orderList.get(i);
				orderArrayItem.put("orderID", order.getOrderid());
				orderArrayItem.put("time",order.getTime().toString());
				orderArrayItem.put("address",order.getAddress());
				orderArrayItem.put("addressee", order.getAddressee());
				orderArrayItem.put("contact", order.getContact());
				orderArrayItem.put("state", order.getState());
				JSONArray commodityArray = new JSONArray();
				List<Order_Commodity> ocList = order_commodityRepository.findByOrderid(order.getOrderid());
				for (int j=0;j<ocList.size();j++) {
					Order_Commodity order_commodity = ocList.get(j);
					Commodity commodity = commodityRepository.findByCommodityid(order_commodity.getCommodityid());
					JSONObject commodityArrayItem = new JSONObject();
					commodityArrayItem.put("commodityID", commodity.getCommodityid());
					commodityArrayItem.put("name", commodity.getCommodityName());
					commodityArrayItem.put("price", commodity.getPrice());
					List<Commodity_Picture> commodity_pictures = commodity_pictureRepository.findByCommodityid(commodity.getCommodityid());
					JSONArray images = new JSONArray();
					for (int k=0;k<images.size();k++) {
						Commodity_Picture commodity_picture = commodity_pictures.get(k);
						JSONObject imagesItem = new JSONObject();
						imagesItem.put("id", commodity_picture.getCommodityid());
						imagesItem.put("order",commodity_picture.getPictureorder());
						imagesItem.put("url", pictureRepository.findByPictureid(commodity_picture.getPictureid()).getPictureUrl());
						images.add(imagesItem);
					}
					commodityArrayItem.put("images",images);
					commodityArrayItem.put("transactionValue",order_commodity.getTransactionPrice());
					commodityArrayItem.put("number", order_commodity.getTransactionNumber());
					commodityArray.add(commodityArrayItem);
				}
				orderArrayItem.put("commodities",commodityArray);
				orderArray.add(orderArrayItem);
			}
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			jsonRet.put("orders",orderArray);
		}else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "错误的id");
		}
		return jsonRet.toJSONString();
	}
	
	//修改订单状态
	@ResponseBody
	@PutMapping(path = "/v1/admin/order/{orderid}")
	public String modifyOrderState(
			@PathVariable(name = "orderid") Long orderid,
			@RequestBody String jsonstr) {
		System.out.println("modify order state");
		JSONObject jsonRet=new JSONObject();
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token = jsonObject.getInteger("token");
		String state = jsonObject.getString("state");

		String userID = UserController.getUserIDByToken(token);
		if (userID == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		
		User user=userRepository.findById(userID).get();

		if (user.getType().equals("admin")) {
			Orderr order = orderrRepository.findByOrderid(orderid);
			if (order!=null) {
				order.setState(state);
				orderrRepository.save(order);
				jsonRet.put("code",0);
				jsonRet.put("errMessage","");
			}else {
				jsonRet.put("code",-1);
				jsonRet.put("errMessage","不存在的订单");
			}
		}
		else {
			jsonRet.put("code",-1);
			jsonRet.put("errMessage","不具备权限");
		}
		
		return jsonRet.toJSONString();
	}
}

