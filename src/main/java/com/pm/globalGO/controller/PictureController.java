package com.pm.globalGO.controller;
import java.io.File;
import java.io.FileNotFoundException;

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
	
	@ResponseBody
	@PostMapping(path = "v1/images")
	public String uploadPic(@RequestParam("image") MultipartFile file) throws FileNotFoundException {
		System.out.println("uploadPic");
		JSONObject jsonRet = new JSONObject();
		if (file.isEmpty()) {
			jsonRet.put("code", -1);
			jsonRet.put("errMessage","没有接受到文件");
		}else {
			long imageID=pictureRepository.count()+1;
			String fileName = file.getOriginalFilename();
			String suffix = fileName.substring(fileName.lastIndexOf("."));
			ApplicationHome home = new ApplicationHome(getClass());
			File jarFile=home.getSource();
			String path =jarFile.getParent()+"/upload";
			File filePath = new File(path + "/" + imageID +suffix); 
			if (!filePath.getParentFile().exists()) {
				filePath.getParentFile().mkdirs();
			}
			try {
				String url=filePath.getPath();
				file.transferTo(filePath);
				Picture pic = new Picture();
				pic.setPictureIndex(imageID);
				pic.setPictureURL(url);
				pictureRepository.save(pic);
				jsonRet.put("code", 0);
				jsonRet.put("errMessage","");
				System.out.println("url="+url);
				JSONObject image = new JSONObject();
				image.put("id", imageID);
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
