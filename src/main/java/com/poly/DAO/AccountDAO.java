package com.poly.DAO;

import java.util.Map;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poly.Bean.Account;
import com.poly.Bean.AccountMap;
import com.poly.Bean.Serviceroom;
import com.poly.Bean.ServiceroomMap;

import jakarta.servlet.http.HttpSession;
import net.minidev.json.JSONObject;

@Repository
public class AccountDAO {
	@Autowired
	HttpSession session;
	RestTemplate rest = new RestTemplate();
	String url = "https://dothanhven-java6-default-rtdb.firebaseio.com/accounts.json";

	private String getUrl(String key) {
		return url.replace(".json", "/" + key + ".json");
	}

	public AccountMap findAll() {
		return rest.getForObject(url, AccountMap.class);
	}

	public Account findByKey(String key) {
		return rest.getForObject(getUrl(key), Account.class);
	}

	public String create(Account data) {
		// Mã hóa mật khẩu trước khi lưu vào Firebase
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(data.getPassword());
		data.setPassword(encodedPassword);

		HttpEntity<Account> entity = new HttpEntity<>(data);
		JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
		return resp.get("name").asText();
	}

	public Account update(String key, Account data) {
		// Mã hóa mật khẩu trước khi cập nhật vào Firebase
		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
		String encodedPassword = passwordEncoder.encode(data.getPassword());
		data.setPassword(encodedPassword);

		HttpEntity<Account> entity = new HttpEntity<>(data);
		rest.put(getUrl(key), entity);
		return data;
	}

	public void delete(String key) {
		rest.delete(getUrl(key));
	}

	public Account findByUsername(String username) {
		AccountMap accountMap = findAll();
		if (accountMap != null) {
			for (Account account : accountMap.values()) {
				if (username.equals(account.getUsername())) {
					return account;
				}
			}
		}
		return null;
	}

	public String findKeyByUsername(String targetUsername) {
		String jsonStr = rest.getForObject(url, String.class);
		JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
		for (String key : jsonObject.keySet()) {
			JsonObject object = jsonObject.getAsJsonObject(key);
			if (object.has("username") && targetUsername.equals(object.get("username").getAsString())) {
				return key;
			}
		}
		return null;
	}
	
	public AccountMap findByName(String name) {
		AccountMap accountMap = findAll();
		AccountMap accountMapNew = new AccountMap();
		for (Map.Entry<String, Account> key : accountMap.entrySet()) {
			if(key.getValue().getFullname().toLowerCase().contains(name.toLowerCase())) {
				accountMapNew.put(key.getKey(), key.getValue());	
			}
		}
		return accountMapNew;
	}

}
