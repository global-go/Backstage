package com.pm.globalGO.controller;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.pm.globalGO.domain.Picture;
import com.pm.globalGO.domain.PictureRepository;
import com.pm.globalGO.domain.UserRepository;


@RestController
public class PictureController{
	
	@Autowired
	private PictureRepository pictureRepository;
	
	static String buildRandString(int len) {
		Random rand=new Random();
		StringBuilder ret = new StringBuilder("");
		for(int i=0;i<len;i++) {
			int randint=rand.nextInt(36);
			if (randint<26) {
				ret.append((char)(randint+'a'));
			}else {
				randint-=26;
				ret.append((char)(randint+'0'));
			}
		}
		return ret.toString();
	}
	
	@ResponseBody
	@PostMapping(path = "/v1/images")
	public String uploadPic(@RequestParam("image") MultipartFile file) throws FileNotFoundException {
		System.out.println("uploadPic");
		JSONObject jsonRet = new JSONObject();
		if (file.isEmpty()) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage","没有接受到文件");
		}else {
			
			String fileName = file.getOriginalFilename();
			String suffix = fileName.substring(fileName.lastIndexOf("."));
			ApplicationHome home = new ApplicationHome(getClass());
			File jarFile=home.getSource();
			String path =jarFile.getParentFile().getParent()+"/upload";
			String imagename;
			File filePath;
			while (true) {
				imagename = buildRandString(10);
				filePath = new File(path + "/" + imagename +suffix); 
				if (filePath.exists())
					continue;
				else
					break;
			}
			
			if (!filePath.getParentFile().exists()) {
				filePath.getParentFile().mkdirs();
			}
			try {
				String url="/upload/"+imagename+suffix;
				file.transferTo(filePath);
				Picture pic = new Picture();
				//pic.setPictureid(imageID);
				pic.setPictureUrl(url);
				pictureRepository.save(pic);
				jsonRet.put("code", 0);
				jsonRet.put("errMessage","");
				System.out.println("url="+url);
				JSONObject image = new JSONObject();
				image.put("id", pic.getPictureid());
				image.put("url",url);
				jsonRet.put("image", image);
			}catch (Exception e) {
				jsonRet.put("code", -1);
				jsonRet.put("errMessage","文件保存失败");
			}
		}		
		return jsonRet.toJSONString();
	}
}
