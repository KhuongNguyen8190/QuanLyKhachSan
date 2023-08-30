package com.poly.DAO;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.databind.JsonNode;
import com.poly.Bean.Customer;
import com.poly.Bean.CustomerMap;
import com.poly.Bean.Order;
import com.poly.Bean.OrderMap;
import com.poly.Bean.OrderRoom;
import com.poly.Bean.OrderRoomMap;
import com.poly.Bean.Room;
import com.poly.Bean.RoomMap;
import com.poly.Service.Format;

@Repository
public class OrderRoomDAO {
	@Autowired
	GetDateDAO dateDAO;
	@Autowired
	RoomDAO roomDAO;
	@Autowired
	OrderDAO orderDAO;

	RestTemplate rest = new RestTemplate();
	String url = "https://dothanhven-java6-default-rtdb.firebaseio.com/orderRoom.json";

	private String getUrl(String key) {
		return url.replace(".json", "/" + key + ".json");
	}

	public OrderRoomMap findAll() {
		return rest.getForObject(url, OrderRoomMap.class);
	}

	public OrderRoom findByKey(String key) {
		return rest.getForObject(getUrl(key), OrderRoom.class);
	}

	public String create(OrderRoom data) {
		HttpEntity<OrderRoom> entity = new HttpEntity<>(data);
		JsonNode resp = rest.postForObject(url, entity, JsonNode.class);
		return resp.get("name").asText();
	}

	public OrderRoom update(String key, OrderRoom data) {
		HttpEntity<OrderRoom> entity = new HttpEntity<>(data);
		rest.put(getUrl(key), entity);
		return data;
	}

	public void delete(String key) {
		rest.delete(getUrl(key));
	}

	public OrderRoom findByIdRoom(String idRoom) {
		OrderRoomMap orderRoomMap = findAll();
		for (OrderRoom orderRoom : orderRoomMap.values()) {
			if (orderRoom.getRoom().keySet().toString().equals("[" + idRoom + "]")
					&& orderRoom.getStatus().equals("1")) {
				return orderRoom;
			}
		}
		return null;
	}

	public String findKeyByIdRoom(String idRoom) {
		OrderRoomMap orderRoomMap = findAll();
		for (Entry<String, OrderRoom> orderRoom : orderRoomMap.entrySet()) {
			if (orderRoom.getValue().getRoom().keySet().toString().equals("[" + idRoom + "]")
					&& orderRoom.getValue().getStatus().equals("1")) {
				return orderRoom.getKey();
			}
		}
		return null;
	}

	public String findKey(OrderRoom orderRoom) {
		OrderRoomMap orderRoomMap = findAll();
		for (Entry<String, OrderRoom> o : orderRoomMap.entrySet()) {

			if (o.getValue().equals(orderRoom) && o.getValue().getStatus().equals(orderRoom.getStatus())) {
				return o.getKey();
			}
		}
		return null;
	}

	public String findKeyRom(OrderRoom orderRoom) {
		String keyRoom = "";
		for (Entry<String, Room> o : orderRoom.getRoom().entrySet()) {
			keyRoom = o.getKey();
		}
		return keyRoom;
	}

	public OrderRoomMap getAllRoomForCustomer(String username) {
		OrderRoomMap findAll = findAll();
		OrderRoomMap newMap = new OrderRoomMap();
		for (Map.Entry<String, OrderRoom> roomEntry : findAll.entrySet()) {
			String orderID = roomEntry.getKey();
			OrderRoom orderValue = roomEntry.getValue();
			if (username.equals(orderValue.getNameCustomer()) && !orderValue.isTypeOrder()) {
				newMap.put(orderID, roomEntry.getValue());
			}
		}
		return newMap;
	}

	// ngày đặt của người sau phải lớn hơn ngày đặt của người trước
	// ngày đặt lớn hơn ngày trả và ngày trả của người sau nhỏ hơn ngày đặt của
	// người trước
	public RoomMap getAllRoomByDateCheck(Date dateStart, Date dateEnd) {

		OrderRoomMap findAll = findAll();

		OrderRoomMap newMap = new OrderRoomMap();
		OrderRoomMap newMapEmpty = new OrderRoomMap();

		OrderRoomMap ordeRoomrMap = new OrderRoomMap();

		RoomMap roomMap = new RoomMap();
		RoomMap roomMapFindAll = roomDAO.getAllRoomEmpty();
		if (findAll == null) {
			return roomMapFindAll;
		}
		for (Map.Entry<String, OrderRoom> roomEntry : findAll.entrySet()) {

			if (roomEntry.getValue().getStatus().equals("1")) {
				newMap.put(roomEntry.getKey(), roomEntry.getValue());
			}
			ordeRoomrMap.put(roomEntry.getKey(), roomEntry.getValue());
		}
		System.out.println(ordeRoomrMap.size());
		for (Map.Entry<String, OrderRoom> roomOrder : newMap.entrySet()) {
			Date checkin = roomOrder.getValue().getDateCheckIn();
			Date checkout = roomOrder.getValue().getDateCheckOut();
			boolean dateCheck = false;
			if (dateStart.compareTo(checkin) > 0 || dateStart.compareTo(checkout) > 0 || dateEnd.compareTo(checkin) > 0
					|| dateEnd.compareTo(checkout) > 0) {
				dateCheck = true;
			}

			if ((dateStart.before(checkin) && dateStart.after(checkout))
					|| (dateEnd.after(checkin) && dateStart.before(checkout)) || (dateCheck)) {
				newMapEmpty.put(roomOrder.getKey(), roomOrder.getValue());
			}
		}
		System.out.println(newMapEmpty.size());
		for (Entry<String, Room> room : roomMapFindAll.entrySet()) {
			boolean put = true;
			for (Map.Entry<String, OrderRoom> orderRoom : newMapEmpty.entrySet()) {

				for (Entry<String, Room> romEntry : orderRoom.getValue().getRoom().entrySet()) {
					if (room.getKey().equals(romEntry.getKey())) {
						put = false;
						break;
					}
				}
			}
			if (put) {
				roomMap.put(room.getKey(), room.getValue());
			}
		}
		System.out.println(roomMap.size());
		return roomMap;
	}

	public OrderRoomMap getAllRoomByDateCheckNotEmpty(Date dateStart, Date dateEnd) {
		OrderRoomMap findAll = findAll();

		OrderRoomMap newMap = new OrderRoomMap();
		OrderRoomMap newMapEmpty = new OrderRoomMap();

		OrderRoomMap ordeRoomrMap = new OrderRoomMap();

		RoomMap roomMap = new RoomMap();
		RoomMap roomMapFindAll = roomDAO.getAllRoomEmpty();

		for (Map.Entry<String, OrderRoom> roomEntry : findAll.entrySet()) {

			if (roomEntry.getValue().getStatus().equals("1")) {
				newMap.put(roomEntry.getKey(), roomEntry.getValue());
			}
			ordeRoomrMap.put(roomEntry.getKey(), roomEntry.getValue());
		}

		for (Map.Entry<String, OrderRoom> roomOrder : newMap.entrySet()) {
			Date checkin = roomOrder.getValue().getDateCheckIn();
			Date checkout = roomOrder.getValue().getDateCheckOut();
			boolean dateCheck = false;
			if (dateStart.compareTo(checkin) > 0 || dateStart.compareTo(checkout) > 0 || dateEnd.compareTo(checkin) > 0
					|| dateEnd.compareTo(checkout) > 0) {
				dateCheck = true;
			}
			System.out.println(dateCheck);
			if ((dateStart.before(checkin) && dateStart.after(checkout))
					|| (dateEnd.after(checkin) && dateStart.before(checkout)) || (dateCheck)) {
				newMapEmpty.put(roomOrder.getKey(), roomOrder.getValue());
			}
		}
		System.out.println("size " + newMapEmpty.size());
//		for (Entry<String, Room> room : roomMapFindAll.entrySet()) {
//			boolean put = true;
//			for (Map.Entry<String, OrderRoom> orderRoom : newMapEmpty.entrySet()) {
//				for (Entry<String, Room> romEntry : orderRoom.getValue().getRoom().entrySet()) {
//					if (room.getKey().equals(romEntry.getKey())) {
//						System.out.println("zoo");
//						roomMap.put(room.getKey(), room.getValue());
//						
//					}
//				}
//			}
//	}
		return newMapEmpty;

	}

	// order room today
	public RoomMap getRoomOrderToday() {
		RoomMap roomMap = roomDAO.findAll();
		RoomMap roomMapNew = new RoomMap();
		OrderRoomMap orderRoomMap = findAll();

		OrderRoomMap orderRoomMapNew = new OrderRoomMap();
		if (orderRoomMap != null) {

			for (Entry<String, OrderRoom> orderRoom : orderRoomMap.entrySet()) {
				if (orderRoom.getValue().getStatus().equals("1")) {
					orderRoomMapNew.put(orderRoom.getKey(), orderRoom.getValue());
				}
			}
		}
		for (Entry<String, Room> room : roomMap.entrySet()) {

			for (Map.Entry<String, OrderRoom> orderRoom : orderRoomMapNew.entrySet()) {

				for (Entry<String, Room> romEntry : orderRoom.getValue().getRoom().entrySet()) {
					String dateString = Format.formatDate(new Date());

					Date date = Format.getTypeDate(dateString);
					if (room.getKey().equals(romEntry.getKey()) && orderRoom.getValue().getDateCheckIn().equals(date)) {
						roomMapNew.put(room.getKey(), room.getValue());
						break;
					}
				}
			}
		}
		return roomMapNew;
	}

	public OrderRoomMap getRoombyDate(String date) {

		OrderRoomMap orderRoomMap = findAll();
		OrderRoomMap orderRoomMapNew = new OrderRoomMap();
		for (Entry<String, OrderRoom> orderRoom : orderRoomMap.entrySet()) {
			String dateCheck = Format.formatDate(orderRoom.getValue().getDateAt());
			if (orderRoom.getValue().getStatus().equals("1") && (date.equals(dateCheck))) {
				orderRoomMapNew.put(orderRoom.getKey(), orderRoom.getValue());
			}
		}

		return orderRoomMapNew;
	}

}
