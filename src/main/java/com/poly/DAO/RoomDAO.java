package com.poly.DAO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.poly.Bean.Customer;
import com.poly.Bean.CustomerMap;
import com.poly.Bean.OrderRoom;
import com.poly.Bean.OrderRoomMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.poly.Bean.Account;
import com.poly.Bean.AccountMap;
import com.poly.Bean.Room;
import com.poly.Bean.RoomMap;
import com.poly.Bean.Serviceroom;
import com.poly.Bean.ServiceroomMap;
import com.poly.Bean.Typeroom;

@Repository
public class RoomDAO {
	RestTemplate rest = new RestTemplate();
	String url = "https://dothanhven-java6-default-rtdb.firebaseio.com/rooms.json";

	private String getUrl(String key) {
		return url.replace(".json", "/" + key + ".json");
	}

	public RoomMap findAll() {
		return rest.getForObject(url, RoomMap.class);
	}

	public Room findByKey(String key) {
		return rest.getForObject(getUrl(key), Room.class);
	}

	public String create(Room data) {
		RoomMap roommap = findAll();
		if (roommap == null) {
			HttpEntity<Room> entity = new HttpEntity<>(data);
			JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
			return resp.get("name").asText();
		} else {
			for (Room service : roommap.values()) {
				if (service.getName().toLowerCase().equals(data.getName().toLowerCase())) {
					return null;
				}
			}
		}

		HttpEntity<Room> entity = new HttpEntity<>(data);
		JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
		return resp.get("name").asText();
	}

	public Room update(String key, Room data) {
		RoomMap roommap = findAll();
		if (roommap == null) {
			rest.put(getUrl(key), data);
			return data;
		} else {
			for (Room service : roommap.values()) {
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

	public RoomMap findByKeyRoom(String keyRoom) {
		RoomMap roomMap = findAll();
		if (roomMap.containsKey(keyRoom)) {
			return roomMap;
		}
		return null;
	}

	public String getKeyRoomOpen() {
		RoomMap roomMap = findAll();
		return null;
	}

	public RoomMap getRoomEmpty() {
		RoomMap roomMapNew = new RoomMap();
		RoomMap roomMap = findAll();
		for (Entry<String, Room> room : roomMap.entrySet()) {
			if (room.getValue().getStatus().equals("1")) {
				roomMapNew.put(room.getKey(), room.getValue());
			}
		}
		return roomMapNew;
	}

	public RoomMap getRoomEmptyByType(String typeRoom) {
		RoomMap roomMapNew = new RoomMap();
		RoomMap roomMap = findAll();
		for (Entry<String, Room> room : roomMap.entrySet()) {
			if (room.getValue().getStatus().equals("1") && room.getValue().getTyperoom().equals(typeRoom)) {
				roomMapNew.put(room.getKey(), room.getValue());
			}
		}
		return roomMapNew;
	}

	public HashMap<String, Room> findByTypeRoom(String typeRoom) {
		HashMap<String, Room> listRooms = new HashMap<>();
		String jsonStr = rest.getForObject(url, String.class);
		JsonObject jsonObject = JsonParser.parseString(jsonStr).getAsJsonObject();
		for (String key : jsonObject.keySet()) {
			JsonObject object = jsonObject.getAsJsonObject(key);
			if (object.get("typeroom").getAsString().equals(typeRoom)
					&& object.get("status").getAsString().equals("1")) {
				Room room = findByKey(key);
				listRooms.put(key, room);
			}
		}
		return listRooms;
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

	public RoomMap findByname(String name) {
		RoomMap roomMap = findAll();
		RoomMap roomMapnew = new RoomMap();
		if (roomMap != null) {
			for (Room room : roomMap.values()) {
				if (room.getName().toLowerCase().contains(name.toLowerCase())) {
					String key = findKeyByName(room.getName());
					roomMapnew.put(key, room);
				}
			}
			return roomMapnew;
		}
		return null;
	}
	
	public RoomMap getAllRoomEmpty() {
        RoomMap roomMapNew = new RoomMap();
        RoomMap roomMap = findAll();
        for (Entry<String, Room> room : roomMap.entrySet()) {
            if(!room.getValue().getStatus().equals("2") && !room.getValue().getStatus().equals("6")) {
                roomMapNew.put(room.getKey(), room.getValue());
            }
        }
        return roomMapNew;
    }
	public RoomMap getAllRoom(OrderRoomMap orderRoomMap) {
        System.out.println(orderRoomMap.size());
        RoomMap roomMapNew = new RoomMap();
        RoomMap roomMap = findAll();
        
        for (Entry<String, Room> room : roomMap.entrySet()) {
            boolean put = true;
            for (Entry<String, OrderRoom> orderRoom : orderRoomMap.entrySet()) {
            
                for (Entry<String, Room> roomOrder : orderRoom.getValue().getRoom().entrySet()) {
                    if (room.getKey().equals(roomOrder.getKey())) {
                        System.out.println(roomOrder.getKey());
                        put = false;
                        break;
                    }
                }
                
            }
            if(put) {
                roomMapNew.put(room.getKey(), room.getValue());
            }
        }
        return roomMapNew;
    }

}
