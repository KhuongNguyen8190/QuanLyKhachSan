package com.poly.DAO;

import java.util.Map;
import java.util.Map.Entry;

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
import com.poly.Bean.Customer;
import com.poly.Bean.CustomerMap;

import jakarta.servlet.http.HttpSession;

@Repository
public class CustomerDAO {
	@Autowired
	HttpSession session;
	RestTemplate rest = new RestTemplate();
	String url = "https://dothanhven-java6-default-rtdb.firebaseio.com/customer.json";

	private String getUrl(String key) {
		return url.replace(".json", "/" + key + ".json");
	}

	public CustomerMap findAll() {
		return rest.getForObject(url, CustomerMap.class);
	}

	public Customer findByKey(String key) {
		return rest.getForObject(getUrl(key), Customer.class);
	}

	public String create(Customer data) {
		// Mã hóa mật khẩu trước khi lưu vào Firebase
//		BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
//		String encodedPassword = passwordEncoder.encode(data.getPassword());
//		data.setPassword(encodedPassword);

		HttpEntity<Customer> entity = new HttpEntity<>(data);
		JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
		return resp.get("name").asText();
	}

	public Customer update(String key, Customer data) {
		HttpEntity<Customer> entity = new HttpEntity<>(data);
		rest.put(getUrl(key), entity);
		return data;
	}

	public void delete(String key) {
		rest.delete(getUrl(key));
	}

	public Customer findByName(String username) {
		CustomerMap customerMap = findAll();
		for (Customer customer : customerMap.values()) {
			if (username.equals(customer.getName())) {
				return customer;
			}
		}
		return null;
	}

	public Customer findByCustomer(CustomerMap customerMap) {

		for (Customer customer : customerMap.values()) {
			return customer;
		}
		return null;
	}

	public String findKey(Customer customer) {
		CustomerMap customerMap = findAll();
		for (Entry<String, Customer> c : customerMap.entrySet()) {
			if (c.getValue().equals(customer)) {
				System.out.println(c.getKey());
				return c.getKey();
			}
		}
		return null;
	}

	public CustomerMap findCustomerMapByValue(Customer customer) {
		CustomerMap customerMap = findAll();
		if (customerMap.containsValue(customer)) {
			System.out.println("find");
			return customerMap;
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

}
