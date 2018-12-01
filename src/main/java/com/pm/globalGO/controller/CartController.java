package com.pm.globalGO.controller;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pm.globalGO.domain.Cart;
import com.pm.globalGO.domain.CartRepository;
import com.pm.globalGO.domain.User;
import com.pm.globalGO.domain.UserRepository;

@RestController
public class CartController{

	@Autowired
	private CartRepository cartRepository;
	
	//修改购物车
	@ResponseBody
	@PutMapping(path="/v1/user/{userid}/cart")
	public String modifyCart(
			@PathVariable(name = "userid") String userID,
			@RequestBody String jsonstr) {
		System.out.println("modify cart");
		
		JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		Long commodityID=jsonObject.getLong("commodityID");
		int number=jsonObject.getInteger("number");
		String userIDFromToken = UserController.getUserIDByToken(token);
		if (userIDFromToken == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
		}
		if(userIDFromToken!=null && userID.equals(userIDFromToken)) {
			Cart cart=cartRepository.findByUseridAndCommodityid(userID,commodityID);
			if (number==0) {
				if (cart!=null) {
					cartRepository.delete(cart);
				}
			}else {
				if (cart==null) {
					cart = new Cart();
					cart.setCommodityid(commodityID);
					cart.setUserid(userID);
				}
				cart.setCartNumber(number);
				//System.out.println(cart.getUserid());
				//System.out.println(cart.getCommodityid());
				//System.out.println(cart.getCartNumber());
				cartRepository.save(cart);
			}
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "错误的id");
		}
		return jsonRet.toJSONString();
	}
	
	//获取购物车
	@ResponseBody
	@GetMapping(path="/v1/user/{userid}/cart")
	public String getCart(
			@PathVariable(name = "userid") String userID,
			@RequestParam("token") int token) {
		
		System.out.println("getCart: "+userID);
		JSONObject jsonRet=new JSONObject();
		String userIDFromToken = UserController.getUserIDByToken(token);
		if (userIDFromToken == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
		}
		JSONArray cartList = new JSONArray();
		if(userIDFromToken!=null && userID.equals(userIDFromToken)) {
			List<Cart> carts = cartRepository.findByUserid(userID);
			for (int i=0;i<carts.size();i++) {
				Cart cart=carts.get(i);
				JSONObject cartListItem= new JSONObject();
				cartListItem.put("commodityID", cart.getCommodityid());
				cartListItem.put("number", cart.getCartNumber());
				cartList.add(cartListItem);
			}
			jsonRet.put("cart", cartList);
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
		}else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "错误的id");
		}
		return jsonRet.toJSONString();
	}
}
