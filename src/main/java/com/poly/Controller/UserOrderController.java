package com.poly.Controller;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.Bean.*;
import com.poly.DAO.*;
import com.poly.Service.LoginResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

@Controller
public class UserOrderController {
	@Autowired
	HttpSession session;
	@Autowired
	HttpServletRequest request;
	@Autowired
	RoomDAO roomDAO;
	@Autowired
	AccountDAO accDAO;
	@Autowired
	GetDateDAO dateDAO;
	@Autowired
	TyperoomDAO typeDAO;
	@Autowired
	OrderRoomDAO orderRoomDAO;

	public static boolean isNumber(String input) {
		try {
			Integer.parseInt(input);
			return true;
		} catch (NumberFormatException e) {
			return false;
		}
	}

	@RequestMapping("/user/checkDateOrder")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> checkRoomNone(Model model, @RequestParam("dateCheckIn") String checkIn,
			@RequestParam("dateCheckOut") String checkOut) {
		Map<String, Object> response = new HashMap<>();
		OrderRoomMap orderRoomMap = orderRoomDAO.findAll();
		List<Object[]> listRoomNone = new ArrayList<Object[]>();
		// Lưu key không trùng
		Set<String> uniqueKeys = new HashSet<>();
		Set<String> uniqueKeyNones = new HashSet<>();
		if (dateDAO.checkDate(dateDAO.getDate(checkIn), dateDAO.getDate(checkOut)) > 0) {
			if (dateDAO.checkDate(new Date(), dateDAO.getDate(checkIn)) > 0) {
				for (Map.Entry<String, OrderRoom> key : orderRoomMap.entrySet()) {
					if ((dateDAO.checkDate(key.getValue().getDateCheckOut(), dateDAO.getDate(checkIn)) > 0
							&& dateDAO.checkDate(key.getValue().getDateCheckOut(), dateDAO.getDate(checkOut)) > 0)
							|| dateDAO.checkDate(dateDAO.getDate(checkOut),key.getValue().getDateCheckIn()) > 0) {
						RoomMap roomNone = key.getValue().getRoom();
						for (Map.Entry<String, Room> room : roomNone.entrySet()) {
							String roomKey = room.getKey();
							// Kiểm tra các phòng đủ điều kiện
							if (!uniqueKeys.contains(roomKey)) {
								uniqueKeys.add(roomKey);
								listRoomNone.add(new Object[] { roomKey, room.getValue().getName() });
							}
						}
					}else {
						RoomMap roomNone = key.getValue().getRoom();
						for (Map.Entry<String, Room> room : roomNone.entrySet()) {
							String roomKey = room.getKey();
							// Kiểm tra các phòng không đủ điều kiện
							if (!uniqueKeyNones.contains(roomKey)) {
								uniqueKeyNones.add(roomKey);
							}
						}
					}
				}
				for (Map.Entry<String, Room> room : roomDAO.findAll().entrySet()) {
					String roomKey = room.getKey();
					// Kiểm tra phòng không đủ đk và add các phòng rỗng
					if (!uniqueKeyNones.contains(roomKey)
							&& room.getValue().getTyperoom().equals((String) session.getAttribute("keyRoom"))) {
						uniqueKeys.add(roomKey);
						listRoomNone.add(new Object[] { roomKey, room.getValue().getName() });
					}
				}
				response.put("success", true);
				response.put("data", listRoomNone);
			}else {
				response.put("message", "Ngày nhận phòng không hợp lệ!");
				response.put("success", false);
			}
			
		} else {
			response.put("message", "Thời gian nhận trả phòng không hợp lệ!");
			response.put("success", false);
		}
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/user/orderRoom")
	@ResponseBody
	public ResponseEntity<LoginResponse> userOrder(Model model, @RequestParam("dateCheckIn") String checkIn,
			@RequestParam("dateCheckOut") String checkOut, @RequestParam("idRoom") String room,
			@RequestParam("numberPeople") String numberPeople) {
		LoginResponse response = new LoginResponse();
		response.setSuccess(false);
		String message = "";
		if (session.getAttribute("username") == null) {
			message = "Vui lòng đăng nhập để đặt phòng!";
		} else {
			Account account = accDAO.findByUsername((String) session.getAttribute("username"));
			if (account.getPhone() == "" || account.getAddress() == "" || account.getCccd() == ""
					|| account.getFullname() == "") {
				message = "Thông tin quý khách chưa được cập nhật!";
			} else {
				Room roomOrder = roomDAO.findByKey(room);
				if (dateDAO.checkDate(dateDAO.getDate(checkIn), dateDAO.getDate(checkOut)) > 0) {
					if (dateDAO.checkDate(new Date(), dateDAO.getDate(checkIn)) > 0) {
						if (numberPeople == null || numberPeople.isEmpty()) {
							message = "Vui lòng nhập số lượng người ở!";
						} else {
							if (isNumber(numberPeople)) {
								try {
									// ROOM
									RoomMap roomMap = new RoomMap();
									roomMap.put(room, roomOrder);
									// CUSTOMER ORDER
									OrderRoom order = new OrderRoom();
									order.setDateCheckIn(dateDAO.getDate(checkIn));
									order.setDateCheckOut(dateDAO.getDate(checkOut));
									order.setDateAt(new Date());
									order.setTypeOrder(false);
									order.setDateCancel(null);
									order.setNameCustomer(account.getUsername());
									order.setPhoneCustomer(account.getPhone());
									order.setCCCDCustomer(account.getCccd());
									order.setNumberPeople(Integer.parseInt(numberPeople));
									order.setDateCancel(new Date());
									order.setStatus("1");
									order.setRoom(roomMap);
									orderRoomDAO.create(order);
									// SET STATUS ROOM
									message = "Bạn đã đặt phòng " + roomOrder.getName() + " thành công!";
									response.setSuccess(true);
								} catch (Exception e) {
									message = "Đặt phòng thất bại!";
									e.printStackTrace();
								}
							} else {
								message = "Số lượng người ở không hợp lệ!";
							}
						}
					} else {
						message = "Ngày nhận phòng không hợp lệ!";
					}

				} else {
					message = "Thời gian nhận trả phòng không hợp lệ!";
				}
			}
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/user/cancelRoom/{roomId}")
	@ResponseBody
	public ResponseEntity<LoginResponse> cancelRoom(@PathVariable String roomId) {
		LoginResponse response = new LoginResponse();
		response.setSuccess(false);
		String message = "";
		try {
			OrderRoom orderRoom = orderRoomDAO.findByKey(roomId);
			String keyRoom = "";
			// GET KEY ROOM
			for (Map.Entry<String, Room> room : orderRoom.getRoom().entrySet()) {
				keyRoom = room.getKey();
			}
			// UPDATE LẠI TRẠNG THÁI PHÒNG
			Room room = roomDAO.findByKey(keyRoom);
			room.setStatus("1");
			roomDAO.update(keyRoom, room);
			// UPDATE TRẠNG THÁI ORDER
			RoomMap roomMap = new RoomMap();
			roomMap.put(keyRoom, room);
			if (orderRoom.getStatus().equals("1")) {
				orderRoom.setStatus("0");
				orderRoom.setRoom(roomMap);
				orderRoom.setDateCancel(new Date());
				orderRoomDAO.update(roomId, orderRoom);
				message = "Hủy phòng " + room.getName() + " thành công!";
				response.setSuccess(true);
			}
		} catch (Exception e) {
			message = "Hủy phòng thất bại!";
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}
}
