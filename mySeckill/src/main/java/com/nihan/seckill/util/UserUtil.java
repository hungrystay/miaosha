package com.nihan.seckill.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.nihan.seckill.domain.MiaoshaUser;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class UserUtil {
	
	private static void createUser(int count) throws Exception{
		List<MiaoshaUser> users = new ArrayList<MiaoshaUser>(count);
		//生成用户
		for(int i=0;i<count;i++) {
			MiaoshaUser user = new MiaoshaUser();
			user.setId(13000000000L+i);
			user.setLoginCount(1);
			user.setNickname("user"+i);
			user.setRegisterDate(new Date());
			user.setSalt("1a2b3c");
			user.setPassword(MD5Util.inputPassToDbPass("123456", user.getSalt()));
			users.add(user);
		}
		System.out.println("create user");

		//登录，生成token
		String urlString = "http://localhost:8082/login/do_login";
		File file = new File("/Users/nihan/imooc/highConcurrencyStudy/stress_test/token.txt");
		if(file.exists()) {
			file.delete();
		}
		RandomAccessFile raf = new RandomAccessFile(file, "rw");
		file.createNewFile();
		raf.seek(0);
		for(int i=0;i<users.size();i++) {
			MiaoshaUser user = users.get(i);
			URL url = new URL(urlString);

			//建立http连接，并且向连接发送POST请求 out
			HttpURLConnection connection = (HttpURLConnection)url.openConnection();
			connection.setRequestMethod("POST");
			connection.setDoOutput(true);
			OutputStream out = connection.getOutputStream();
			String params = "mobile="+user.getId()+"&password="+MD5Util.inputPassToFormPass("123456");
			out.write(params.getBytes());
			out.flush();

			//得到返回值，并且准备将返回值写入到缓存buff中，并且拼接成一个字符串response
//			InputStream inputStream = connection.getInputStream();
//			ByteArrayOutputStream bout = new ByteArrayOutputStream();
//			byte buff[] = new byte[1024];
//			int len = 0;
//			while((len = inputStream.read(buff)) >= 0) {
//				bout.write(buff, 0 ,len);
//			}
//			inputStream.close();
//			bout.close();
//			String response = new String(bout.toByteArray());
//			System.out.println("response ============ ");
//			System.out.println(response);

			//从连接中得到返回值
			InputStream inputStream = connection.getInputStream();
			BufferedReader in = new BufferedReader(new InputStreamReader(inputStream));
			StringBuilder response = new StringBuilder();
			String thisLine = null;
			while((thisLine = in.readLine()) != null) {
				response.append(thisLine);
			}
			//将response解析成json，然后取出相应的域
			JSONObject jo = JSON.parseObject(response.toString());
			String token = jo.getString("data");
			System.out.println("create token : " + user.getId());

			//将token信息写入到token.txt文件中
			String row = user.getId()+","+token;
			raf.seek(raf.length());
			raf.write(row.getBytes());
			raf.write("\r\n".getBytes());
			System.out.println("write to file : " + user.getId());
		}
		raf.close();
		System.out.println("over");
	}
	
	public static void main(String[] args)throws Exception {
		createUser(5000);
	}
}
