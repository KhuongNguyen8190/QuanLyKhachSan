package com.poly.DAO;

import java.util.ArrayList;
import java.util.List;

import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poly.Bean.Room;
import com.poly.Bean.RoomMap;
import com.poly.Bean.Serviceroom;
import com.poly.Bean.ServiceroomMap;
import com.poly.Bean.Typeroom;
import com.poly.Bean.TyperoomMap;

@Repository
public class TyperoomDAO {
	RestTemplate rest = new RestTemplate();
	String url = "https://dothanhven-java6-default-rtdb.firebaseio.com/typeroom.json";

	private String getUrl(String key) {
		return url.replace(".json", "/" + key + ".json");
	}

	public TyperoomMap findAll() {
		return rest.getForObject(url, TyperoomMap.class);
	}

	public Typeroom findByKey(String key) {
		return rest.getForObject(getUrl(key), Typeroom.class);
	}

	public String create(Typeroom data) {
		TyperoomMap roommap = findAll();
		if (roommap == null) {
			HttpEntity<Typeroom> entity = new HttpEntity<>(data);
			JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
			return resp.get("name").asText();
		} else {
			for (Typeroom service : roommap.values()) {
				if (service.getName().toLowerCase().equals(data.getName().toLowerCase())) {
					return null;
				}
			}
		}
		HttpEntity<Typeroom> entity = new HttpEntity<>(data);
		JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
		return resp.get("name").asText();
	}

	public Typeroom update(String key, Typeroom data) {
		TyperoomMap roommap = findAll();
		if (roommap == null) {
			rest.put(getUrl(key), data);
			return data;
		} else {
			for (Typeroom service : roommap.values()) {
				if (data.getName().equals(service.getName())) {
					String currentKey = findKeyByName(service.getName());
					if (currentKey.equals(key)) {
						rest.put(getUrl(key), data);
						return data;
					}
				}
			}
			return null;
		}
	}

	public void delete(String key) {
		rest.delete(getUrl(key));
	}

	public String findKeyByName(String name) {
		String jsonStr = rest.getForObject(url, String.class);
		JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
		for (String key : jsonObject.keySet()) {
			JsonObject object = jsonObject.getAsJsonObject(key);
			if (object.has("name") && name.equals(object.get("name").getAsString())) {
				return key;
			}
		}
		return null;
	}

	public TyperoomMap findByname(String name) {
		TyperoomMap roomMap = findAll();
		TyperoomMap roomMapnew = new TyperoomMap();

		if (roomMap != null) {
			for (Typeroom room : roomMap.values()) {
				if (room.getName().toLowerCase().contains(name.toLowerCase())) {
					String key = findKeyByName(room.getName());
					roomMapnew.put(key, room);
				}
			}
			return roomMapnew;
		}
		return null;
	}

	public String findNameByKey(String key) {
		Typeroom typeroom = findByKey(key);

		return typeroom.getName();
	}

	public double findPrice(String key) {
		Typeroom typeroom = findByKey(key);

		return typeroom.getPrice();
	}
}
