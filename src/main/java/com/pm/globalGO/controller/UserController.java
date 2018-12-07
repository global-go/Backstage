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
import com.pm.globalGO.domain.Cart;
import com.pm.globalGO.domain.CartRepository;
import com.pm.globalGO.domain.Commodity;
import com.pm.globalGO.domain.CommodityRepository;
import com.pm.globalGO.domain.OrderrRepository;
import com.pm.globalGO.domain.Picture;
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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController{
	
	private static Map<Integer,String> tokenMap = new HashMap<Integer,String>();
	private static Map<Integer,Date> tokenTime = new HashMap<Integer,Date>();
	
	private static final int max_token=1000000;
	private static final long max_validity_interval=12 * 60 * 60 * 1000;
	
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private OrderrRepository orderRepository;
	@Autowired
	private Order_CommodityRepository order_CommodityRepository;
	@Autowired
	private Commodity_PictureRepository commodity_pictureRepository;
	@Autowired
	private CommodityRepository commodityRepository;
	@Autowired
	private PictureRepository pictureRepository;
	@Autowired
	private CartRepository cartRepository;
	
	static String getUserIDByToken(int token) {
		String userID = tokenMap.get(token);
		if (userID != null) {
			Date nowTime=new Date();
			long interval = nowTime.getTime() - tokenTime.get(token).getTime();
			if (interval > max_validity_interval) {
				tokenMap.remove(token);
				tokenTime.remove(token);
				userID = null;
			}
		}
		return userID;
	}
	
	static String hash(String password) {
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
	
	static int setTokenForUserID(String userID) {
		Date nowTime=new Date();
		Random rand=new Random();
		int newToken=rand.nextInt(max_token);
		while (tokenMap.get(newToken)!=null) {
			Date t = tokenTime.get(newToken);
			long interval = nowTime.getTime()-t.getTime();
			if (interval <= max_validity_interval) {
				break;
			}else {
				tokenMap.remove(newToken);
				tokenTime.remove(newToken);
				newToken=rand.nextInt(max_token);
			}
		}
		tokenMap.put(newToken, userID);
		tokenTime.put(newToken,nowTime);
		return newToken;
	}
	
	//注册
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
			User newUser=new User(userID,"User"+userID,hashPassword,pictureRepository.findByPictureid(new Long(1)).getPictureUrl(),1000.0,"user");
			userRepository.save(newUser);
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
	
	//登陆
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
		if (user!=null && !user.getType().equals("sysAdmin") && user.getPassword().equals(hashPassword)) {
				int newToken=setTokenForUserID(userID);
				ret.put("code",0);
				ret.put("errMessage","");
				ret.put("token",newToken);
				ret.put("unfinishedCount",0);
				JSONObject userInfo = new JSONObject();
				userInfo.put("id", user.getUserid());
				userInfo.put("avatar", user.getUserPicture());
				userInfo.put("nickname",user.getNickname());
				userInfo.put("type", user.getType());
				userInfo.put("balance",user.getBalance());
				ret.put("userInfo", userInfo);
		}else {
			ret.put("code",-1);
			ret.put("errMessage","用户名或密码错误");
		}
		return ret.toJSONString();
	}

	//登陆(系统管理员)
	@ResponseBody
	@PostMapping(path = "/v1/system/admin/login")
	public String sysLogin(@RequestBody String jsonstr) {
		System.out.println("sysLogin");
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		String userID = jsonObject.getString("userID");
		String password = jsonObject.getString("password");
		JSONObject ret = new JSONObject();
		User user=userRepository.findByUserid(userID);
		String hashPassword = hash(password);
		if (user!=null && user.getType().equals("sysAdmin") && user.getPassword().equals(hashPassword)) {
				int newToken=setTokenForUserID(userID);
				ret.put("code",0);
				ret.put("errMessage","");
				ret.put("token",newToken);
		}else {
			ret.put("code",-1);
			ret.put("errMessage","用户名或密码错误");
		}
		return ret.toJSONString();
	}
	
	
	//后台信息
	@ResponseBody
	@PostMapping(path="/v1/admin/info")
	public String getAllData(@RequestBody String jsonstr) {
		System.out.println("get all data");
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		String userID = getUserIDByToken(token);
		JSONObject jsonRet = new JSONObject();
		
		if(userID!=null) {	
			if(userRepository.findByUserid(userID).getType().equals("admin")) {
				JSONObject userInfo=new JSONObject();
				JSONObject orderInfo=new JSONObject();
				JSONObject commodityInfo=new JSONObject();
				
				userInfo.put("avatar",userRepository.findByUserid(userID).getUserPicture());
				jsonRet.put("userInfo",userInfo);
				
				List<Orderr> orders=orderRepository.findAll();//订单
				JSONArray orderlist=new JSONArray();
				int unfinishedCount=0;
				double income=0;
				for(int i=0;i<orders.size();i++) {
					JSONObject listitem=new JSONObject();
					Orderr order=orders.get(i);
					
					listitem.put("id",order.getOrderid());
					listitem.put("time",order.getTime());
					listitem.put("address",order.getAddress());
					listitem.put("addressee",order.getAddressee());
					listitem.put("contact",order.getContact());
					
					JSONObject userInfo1=new JSONObject();
					User user=userRepository.findByUserid(order.getUserid());
					userInfo1.put("nickname",user.getNickname());
					userInfo1.put("id", user.getUserid());
					listitem.put("userInfo",userInfo1);
					
					JSONArray commodityList = new JSONArray();
					List<Order_Commodity> oclist = order_CommodityRepository.findByOrderid(order.getOrderid());
					
					
					int orderIncome=0;
					for (int j=0;j<oclist.size();j++) {
						Order_Commodity oc = oclist.get(j);
						JSONObject commodityListItem = new JSONObject();
						commodityListItem.put("commodityID", oc.getCommodityid());
						commodityListItem.put("transactionValue", oc.getTransactionPrice());
						commodityListItem.put("number", oc.getTransactionNumber());
						commodityList.add(commodityListItem);
						orderIncome += oc.getTransactionNumber()*oc.getTransactionPrice();
					}		
					listitem.put("commodityList",commodityList);
					
					String state=order.getState();
					listitem.put("state",state);
					
					orderlist.add(listitem);
					
					if(state.equals("finished"))
						income+=orderIncome;
					else
						unfinishedCount++;
						
				}
				
				orderInfo.put("totalCount",orders.size());
				orderInfo.put("unfinishedCount",unfinishedCount);
				orderInfo.put("income",income);
				orderInfo.put("list",orderlist);
				
				
				
				List<Commodity> commodities=commodityRepository.findAll();//在售商品
				JSONArray commoditylist=new JSONArray();
				int notSoldOutCount=0;
				int maxCount=50;
				int totalCount=0;
				for(int i=0;i<commodities.size();i++) {
					Commodity commodity=commodities.get(i);
					if (commodity.getStock()<0)
						continue;
					JSONObject listitem=new JSONObject();
					
					listitem.put("id",commodity.getCommodityid());
					listitem.put("name",commodity.getCommodityName());
					listitem.put("category", commodity.getCategory());
					listitem.put("price",commodity.getPrice());
					int stock=commodity.getStock();
					listitem.put("stock",stock);
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
					commoditylist.add(listitem);
					if(stock>0) 
					     notSoldOutCount++;	
					++totalCount;		
				}
				
				commodityInfo.put("totalCount",totalCount);
				commodityInfo.put("notSoldOutCount",notSoldOutCount);
				commodityInfo.put("maxCount",maxCount);
				commodityInfo.put("list",commoditylist);
				
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
			jsonRet.put("errMessage", "token失效,请重新登陆");
		}	
		return jsonRet.toJSONString();
	}
	
	//修改用户信息
	@ResponseBody
	@PutMapping(path="/v1/user/{userid}")
	public String modifyUserInfo(
			@PathVariable(name = "userid",required = false) String userID,
			@RequestBody String jsonstr) {
		System.out.println("modify info: "+userID);
		
		JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		String userIDFromToken = getUserIDByToken(token);

		if(userIDFromToken!=null && userID.equals(userIDFromToken)) {
			jsonRet.put("code", 0);
			jsonRet.put("errMessage", "");
			JSONObject userInfo=jsonObject.getJSONObject("userInfo");
			User user=userRepository.findByUserid(userID);
			String nickname=userInfo.getString("nickname");
			String password=userInfo.getString("password");
			Long avatar=userInfo.getLong("avatar");
			
			if(nickname!=null) 
				user.setNickname(nickname);
			
			if(password!=null) 
				user.setPassword(hash(password));
				
			if(avatar!=null) {
				String avatarURL=pictureRepository.findByPictureid(avatar).getPictureUrl();
				user.setUserPicture(avatarURL);
			}
			userRepository.save(user);
			
			userInfo=new JSONObject();
			userInfo.put("id",user.getUserid());
			userInfo.put("nickname", user.getNickname());
			userInfo.put("type", user.getType());
			userInfo.put("avatar", user.getUserPicture());
			
			jsonRet.put("user", userInfo);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "错误的id");
		}
		return jsonRet.toJSONString();
	}
	
	//修改用户信息(系统管理员)
    @ResponseBody	
	@PutMapping(path="/v1/system/admin/user/{userid}")
	public String sysModifyUser(
			@PathVariable(name = "userid",required = false) String userID, 
			@RequestBody String jsonstr) {
		System.out.println("sysAdmin modify info: "+userID);
		
		JSONObject jsonRet = new JSONObject();
		
		JSONObject jsonObject = JSONObject.parseObject(jsonstr);
		int token=jsonObject.getInteger("token");
		String userIDFromToken = getUserIDByToken(token);
		User sysAdmin = userRepository.findByUserid(userIDFromToken);
		if (sysAdmin == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		if(sysAdmin.getType().equals("sysAdmin")) {

			JSONObject userInfo=jsonObject.getJSONObject("userInfo");
			User user=userRepository.findByUserid(userID);
			if (user==null) {
				jsonRet.put("code", -1);
				jsonRet.put("errMessage", "用户不存在");
				return jsonRet.toJSONString();
			}
			
			if (userInfo.containsKey("password")) {
				String password=userInfo.getString("password");
				user.setPassword(hash(password));
			}
			
			if (userInfo.containsKey("type")) {
				String type=userInfo.getString("type");
				user.setType(type);
			}
			
			if (userInfo.containsKey("balance")) {
				double balance = userInfo.getDouble("balance");
				user.setBalance(balance);
			}

			userRepository.save(user);
			
			userInfo=new JSONObject();
			userInfo.put("id",user.getUserid());
			userInfo.put("nickname", user.getNickname());
			userInfo.put("balance",user.getBalance());
			userInfo.put("type", user.getType());
			userInfo.put("avatar", user.getUserPicture());
			jsonRet.put("user", userInfo);
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "不具备权限");
		}
		return jsonRet.toJSONString();
	}
    
    //系统管理员信息
	@ResponseBody
	@GetMapping(path="/v1/system/admin/info")
	public String sysGetInfo(
			@RequestParam("token") int token) {
		System.out.println("sysAdmin get info");
		JSONObject jsonRet = new JSONObject();
		String userIDFromToken = getUserIDByToken(token);
		if (userIDFromToken == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		User sysAdmin = userRepository.findByUserid(userIDFromToken);
		if(sysAdmin.getType().equals("sysAdmin")) {
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
				userInfo.put("balance",user.getBalance());
				userInfo.put("avatar",user.getUserPicture());
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
	
	//删除用户
	@ResponseBody
	@DeleteMapping(path="/v1/system/admin/user/{id}")
	public String sysDeleteUser(
			@PathVariable("id") String userID,
			@RequestParam("token") int token
			) {
		System.out.println("delete user: "+userID);
		JSONObject jsonRet = new JSONObject();

		String userIDFromToken = getUserIDByToken(token);
		if (userIDFromToken == null) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "token失效,请重新登陆");
			return jsonRet.toJSONString();
		}
		User sysAdmin = userRepository.findByUserid(userIDFromToken);
		if(sysAdmin.getType().equals("sysAdmin")) {
			User user= userRepository.findByUserid(userID);
			if (user==null) {
				jsonRet.put("code", -1);
				jsonRet.put("errMessage", "用户不存在");
			}else {
				cartRepository.deleteByUserid(userID);
				List<Orderr> orderList = orderRepository.findByUserid(userID);
				for (int i=0;i<orderList.size();i++) {
					Long orderID = orderList.get(i).getOrderid();
					order_CommodityRepository.deleteByOrderid(orderID);
				}
				orderRepository.deleteByUserid(userID);
				userRepository.deleteByUserid(userID);
				jsonRet.put("code", 0);
				jsonRet.put("errMessage", "");
			}
		}
		else {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage", "不具备权限");
		}
		return jsonRet.toJSONString();
	}
	
}
