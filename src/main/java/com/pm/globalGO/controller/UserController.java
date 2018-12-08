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
	private PictureRepository pictureRepository;
	
	@Autowired
	private OrderrRepository orderrRepository;
	
	//通过token识别用户
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
	
	//对密码进行哈希操作
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
	
	//为登陆的用户设置token
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
				
				//统计未完成订单数
				int unfinishedCount=0;
				List<Orderr> orderList = orderrRepository.findByUserid(userID);
				for (int i=0;i<orderList.size();i++) {
					if (!orderList.get(i).getState().equals("finished")) {
						++unfinishedCount;
					}
				}
				
				ret.put("unfinishedCount",unfinishedCount);
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
}
