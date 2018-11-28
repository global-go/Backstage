package com.pm.globalGO.controller;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import javax.websocket.server.PathParam;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.pm.globalGO.domain.Commodity_Picture;
import com.pm.globalGO.domain.Commodity_PicturePK;
import com.pm.globalGO.domain.Commodity_PictureRepository;
import com.pm.globalGO.domain.Order_Commodity;
import com.pm.globalGO.domain.Order_CommodityPK;
import com.pm.globalGO.domain.Order_CommodityRepository;
import com.pm.globalGO.domain.Commodity;
import com.pm.globalGO.domain.CommodityRepository;
import com.pm.globalGO.domain.OrderrRepository;
import com.pm.globalGO.domain.Orderr;
import com.pm.globalGO.domain.PictureRepository;
import com.pm.globalGO.domain.User;
import com.pm.globalGO.domain.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController{
	
	private static Map<Integer,String> tokenMap = new HashMap<Integer,String>();
	private static Map<Integer,Date> tokenTime = new HashMap<Integer,Date>();
	private static int sysAdminToken;
	
	private static final int max_token=1000000;
	private static final long max_validity_interval=12 * 60 * 60 * 1000;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OrderrRepository orderRepository;
	@Autowired
	private Order_CommodityRepository order_CommodityRepository;
	@Autowired
	private Commodity_Picture commodity_picture;
	@Autowired
	private CommodityRepository commodityRepository;
	@Autowired
	private PictureRepository pictureRepository;
	
	
	void saveUser(User user) {
		userRepository.save(user);
	}
	
	
	String getUserIDByToken(int token) {
		return tokenMap.get(token);
	}
	
	String hash(String password) {
		StringBuilder ret = new StringBuilder("");
		for (int k=1;k<=20;k++) {
			int t=0;
			for (int i=0;i<password.length();i++) {
				t = t * k + password.charAt(i);
			}	
			if (t<0)
				t=-t;
			ret.append((char)((int)'a'+t%26));
		}
		return ret.toString();
	}
	
	int setTokenForUserID(String userID) {
		Date nowTime=new Date();
		Random rand=new Random();
		int newToken=rand.nextInt(max_token);
		while (tokenMap.get(newToken)!=null && newToken!=sysAdminToken) {
			if (newToken == sysAdminToken) {
				newToken = rand.nextInt(max_token);
				continue;
			}
			Date t = tokenTime.get(newToken);
			long interval = nowTime.getTime()-t.getTime();
			if (interval > max_validity_interval) {
				break;
			}else {
				newToken=rand.nextInt(max_token);
			}
		}
		tokenMap.put(newToken, userID);
		tokenTime.put(newToken,nowTime);
		return newToken;
	}
	
	int setTokenForSysAdmin() {
		Date nowTime=new Date();
		Random rand=new Random();
		int newToken=rand.nextInt(max_token);
		while (tokenMap.get(newToken)!=null) {
			Date t = tokenTime.get(newToken);
			long interval = nowTime.getTime()-t.getTime();
			if (interval > max_validity_interval) {
				break;
			}else {
				newToken=rand.nextInt(max_token);
			}
		}
		sysAdminToken=newToken;
		tokenTime.put(newToken,nowTime);
		return newToken;
	}
	

	@ResponseBody
	@PostMapping(path = "/v1/register")
	public String register(@RequestBody String jsonstr) {
		System.out.println("register");
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		String userID = jsonObject.getString("userID");
		String password = jsonObject.getString("password");	
		JSONObject ret = new JSONObject();
		User user=userRepository.findByUserid(userID);
		if (user==null) {
			String hashPassword = hash(password);
			User newUser=new User(userID,"User"+userID,hashPassword,"",0,"user");
			saveUser(newUser);
			int newToken=setTokenForUserID(userID);
			ret.put("code", 0);
			ret.put("errMessage","");
			ret.put("token",newToken);
		}else {
			ret.put("code", -1);
			ret.put("errMessage","Existing userID");
			ret.put("token",0);
		}
		return ret.toJSONString();
	}
	
	@ResponseBody
	@PostMapping(path = "/v1/login")
	public String login(@RequestBody String jsonstr) {
		System.out.println("login");
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		String userID = jsonObject.getString("userID");
		String password = jsonObject.getString("password");
		JSONObject ret = new JSONObject();
		User user=userRepository.findByUserid(userID);
		String hashPassword = hash(password);
		if (user!=null && user.getPassword().equals(hashPassword)) {
				int newToken=setTokenForUserID(userID);
				ret.put("code",0);
				ret.put("errMessage","");
				ret.put("token",newToken);
				ret.put("unfinishedCount",0);
				JSONObject userInfo = new JSONObject();
				userInfo.put("id", user.getUserid());
				userInfo.put("avatar", user.getUserpicture());
				userInfo.put("nickname",user.getNickname());
				userInfo.put("type", user.getType());
				userInfo.put("balance",user.getBalance());
				ret.put("userInfo", userInfo);
		}else {
			ret.put("code",-1);
			ret.put("errMessage","failde");
		}
		return ret.toJSONString();
	}
	
	@ResponseBody
	@PostMapping(path="/admin/info")
	public String getAllData(@RequestBody String jsonstr) {
		System.out.println("get all data");
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		String userID=tokenMap.get(token);
		
		JSONObject jsonRet = new JSONObject();
		
		if(userID!=null) {	
			if(userRepository.findByUserid(userID).getType().equals("admin")) {
				JSONObject userInfo=new JSONObject();
				JSONObject orderInfo=new JSONObject();
				JSONObject commodityInfo=new JSONObject();
				
				userInfo.put("avatar",userRepository.findByUserid(userID).getUserpicture());
				jsonRet.put("userInfo",userInfo);
				
				List<Orderr> orders=orderReoisitory.findAll();//订单
				JSONArray list=new JSONArray();
				int totalCount=orders.size();
				int unfinishedCount=0;
				double income=0;
				for(int i=0;i<totalCount;i++) {
					JSONObject listitem=new JSONObject();
					Orderr order=orders.get(i);
					
					listitem.put("id",order.getOrderID());
					listitem.put("time",order.getTime());
					listitem.put("address",order.getAddress());
					listitem.put("addressee",order.getAddressee());
					listitem.put("contact",order.getContact());
					
					userInfo=new JSONObject();
					userInfo.put("nickname",userRepository.findByUserid(order.getUserID()).getUserNickname());
					listitem.put("userInfo",userInfo);
					
					listitem.put("commodityID",order.getCommodityID());
					
					Order_Commodity order_Commodity=order_CommodityRepository.findById(new Order_CommodityPK(order.getOrderID(),order.getCommodityID()));
					double transactionPrice=order_Commodity.getTransactionPrice();
					int number=order_Commodity.getTransactionNumber();
					listitem.put("transactionValue",transactionPrice);
					listitem.put("number",number);
					
					String state=order.getState();
					listitem.put("state",state);
					
					list.add(listitem);
					
					if(state.equals("finished"))
						income+=(transactionPrice*number);
					else
						unfinishedCount++;
						
				}
				
				orderInfo.put("totalCount",totalCount);
				orderInfo.put("unfinishedCount",unfinishedCount);
				orderInfo.put("income",income);
				orderInfo.put("list",list);
				
				
				
				List<Commodity> commodities=commodityReoisitory.findAll();//在售商品
				list=new JSONArray();
				totalCount=commodities.size();
				int notSoldOutCount=0;
				int maxCount=50;//随便定的，再议
				for(int i=0;i<totalCount;i++) {
					JSONObject listitem=new JSONObject();
					Commodity commodity=commodities.get(i);
					
					listitem.put("name",commodity.getCommodityName());
					listitem.put("price",commodity.getPrice());
					int stock=commodity.getStock();
					listitem.put("stock",stock);
					listitem.put("description",commodity.getDescription());
					
					JSONArray images=new JSONArray();
					List<Commodity_Picture> pictures=commodity_pictureRepository.findByCommodityID(commodity.getCommodityID());
					for(int j=0;j<pictures.size();j++) {
						JSONObject image=new JSONObject();
					    image.put("id",pictures.get(i).getPictureOrder());
					    image.put("url",pictureRepository.findByPictureIndex(pictures.get(i).getPictureIndex()).getPictureURL());
					    images.add(image);
					    
					}
					
					listitem.put("images",images);
					list.add(listitem);
									
					totalCount++;
					if(stock>0) {
					     notSoldOutCount++;
						 }
						
									
				}
				
				commodityInfo.put("totalCount",totalCount);
				commodityInfo.put("notSoldOutCount",unfinishedCount);
				commodityInfo.put("maxCount",income);
				commodityInfo.put("list",list);
				
				jsonRet.put("Order", orderInfo);
				jsonRet.put("Commodity",commodityInfo);
			  
			}
			else {
				jsonRet.put("code", -1);
				jsonRet.put("errMessage", "不具备admin权限");
			}
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "账号不存在");
		}	
		return jsonRet.toJSONString();
	}
	
	@ResponseBody
	@PutMapping(path="/user/{id}")
	public String modifyUserInfo(@PathParam("id") String userID, @RequestBody String jsonstr) {
		System.out.println("modify info: "+userID);
		
		JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		if(userID.equals(tokenMap.get(token))) {
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			JSONObject userInfo=jsonObject.getJSONObject("userInfo");
			User user=userRepository.findByUserid(userID);
			String nickname=userInfo.getString("nickname");
			String password=userInfo.getString("password");
			Long avatar=userInfo.getLong("avatar");
			String avatarURL="";
			if(nickname!=null) 
				user.setNickname(nickname);
			
			if(password!=null) 
				user.setPassword(password);
				
			if(avatar!=null) {
				avatarURL=pictureRepository.findBy(avatar).getPictureURL();
				user.setUserpicture(avatarURL);
			}
			
			saveUser(user);
			
			userInfo=new JSONObject();
			userInfo.put("id",user.getUserid());
			userInfo.put("nickname", user.getNickname());
			userInfo.put("type", user.getType());
			userInfo.put("avatar", avatarURL);
			
			jsonRet.put("user", userInfo);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token不一致");
		}
		return jsonRet.toJSONString();
	}
	
    @ResponseBody	
	@PutMapping(path="/admin/user/{id}")
	public String sysModifyUser(@PathParam("id") String userID, @RequestBody String jsonstr) {
		System.out.println("sysAdmin modify info: "+userID);
		
		JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		if(token==sysAdminToken) {
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			JSONObject userInfo=jsonObject.getJSONObject("userInfo");
			User user=userRepository.findByUserid(userID);
			String nickname=userInfo.getString("nickname");
			String password=userInfo.getString("password");
			String type=userInfo.getString("type");
			Long avatar=userInfo.getLong("avatar");
			String avatarURL="";
			if(nickname!=null) 
				user.setNickname(nickname);
				
			if(password!=null) 
				user.setPassword(password);
				
			if(type!=null) 
				user.setType(type);
				
			if(avatar!=null) {
				avatarURL=pictureRepository.findBy(avatar).getPictureURL();
				user.setUserpicture(avatarURL);
			}
		    
			saveUser(user);
			
			userInfo=new JSONObject();
			userInfo.put("id",user.getUserid());
			userInfo.put("nickname", user.getNickname());
			userInfo.put("type", user.getType());
			userInfo.put("avatar", avatarURL);
			
			jsonRet.put("user", userInfo);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "不具备权限");
		}
		return jsonRet.toJSONString();
	}
    
	@ResponseBody
	@GetMapping(path="/system/admin/info")
	public String sysGetInfo(@RequestBody String jsonstr) {
		System.out.println("sysAdmin get info");
		
        JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		if(token==sysAdminToken) {
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			List<User> allUsers=userRepository.findAll();
			User user=null;
			JSONObject userInfo=null;
			JSONArray users=new JSONArray();
			for(int i=0;i<allUsers.size();i++) {
				user=allUsers.get(i);
				userInfo=new JSONObject();
				userInfo.put("id",user.getUserid());
				userInfo.put("nickname", user.getNickname());
				userInfo.put("type", user.getType());
				userInfo.put("avatar",user.getUserpicture());
				users.add(userInfo);
			}   
			jsonRet.put("users", users);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "不具备权限");
		}
		return jsonRet.toJSONString();
	}
	
	@ResponseBody
	@DeleteMapping(path="/system/admin/user/{id}")
	public String sysDeleteUser(@PathParam("id") String userID,@RequestBody String jsonstr) {
		System.out.println("delete user: "+userID);
		
        JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		if(token==sysAdminToken) {
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			userRepository.deleteById(userID);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "不具备权限");
		}
		return jsonRet.toJSONString();
	}
	
}
