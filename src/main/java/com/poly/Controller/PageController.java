package com.poly.Controller;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.poly.Bean.*;
import com.poly.DAO.*;

import jakarta.servlet.http.HttpSession;

@Controller
public class PageController {
	@Autowired
	TyperoomDAO typeroomdao;
	@Autowired
	ServiceroomDAO serviceroomDAO;
	@Autowired
	RoomDAO roomdao;
	@Autowired
	AccountDAO accountDAO;
	@Autowired
	HttpSession session;
	@Autowired
	GetDateDAO dateDAO;
	@Autowired
	OrderRoomDAO orderRoomDAO;
	@Autowired
	GetDateDAO getDateDAO;

	// CUSTOMER
	@GetMapping("/")
	public String home(Model model) {
		model.addAttribute("typerooms", typeroomdao.findAll());
		if ((String) session.getAttribute("username") != null) {
			Account account = accountDAO.findByUsername((String) session.getAttribute("username"));
			if (account != null) {
				for (String role : account.getRole()) {
					if (role.equals("ADMIN")) {
						session.setAttribute("admin", true);
						break;
					}
				}
			}
		}
		return "user/index";
	}

	@GetMapping("/infomation-room/{key}")
	public String infomationRoom(Model model, @PathVariable("key") String key) {
		model.addAttribute("inforoom", typeroomdao.findByKey(key));
		model.addAttribute("listroom", roomdao.findByTypeRoom(key));
		model.addAttribute("sizeRoom", roomdao.findByTypeRoom(key).size());
		session.setAttribute("keyRoom", key);
		return "user/infomation-room";
	}

	@GetMapping("/order-history")
	public String orderHistory(Model model) {
		OrderRoomMap dataMap = orderRoomDAO.getAllRoomForCustomer((String) session.getAttribute("username"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy");
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
		List<Object[]> listOrder = new ArrayList<>();
		List<Object[]> listRoomDaO = new ArrayList<>();
		for (Map.Entry<String, OrderRoom> entry : dataMap.entrySet()) {
			OrderRoom orderRoom = entry.getValue();
			String timeCheckInDateStr = dateFormat.format(orderRoom.getDateCheckIn());
			String timeCheckOutDateStr = dateFormat.format(orderRoom.getDateCheckOut());
			String timeOrderRoomStr = dateFormat.format(orderRoom.getDateAt());
			String timeCancelStr = "";
			if (orderRoom.getDateCancel() != null) {
				timeCancelStr = dateFormat.format(orderRoom.getDateCancel());
			}
			String nameRoom = "";
			String keyTypeRoom = "";
			String statusRoom = "";
			String statusOrder = orderRoom.getStatus();
			for (Room room : orderRoom.getRoom().values()) {
				nameRoom = room.getName();
				keyTypeRoom = room.getTyperoom();
				statusRoom = room.getStatus();
			}
			String nameTypeRoom = typeroomdao.findByKey(keyTypeRoom).getName();
			Long price = (long) (getDateDAO.checkDate(orderRoom.getDateCheckIn(), orderRoom.getDateCheckOut())
					* typeroomdao.findByKey(keyTypeRoom).getPrice());
			try {
				if (dateDAO.checkDate(dateFormat.parse(timeCheckOutDateStr), new Date()) >= 1) {
					listRoomDaO.add(new Object[] { entry.getKey(), timeOrderRoomStr, timeCheckInDateStr,
							timeCheckOutDateStr, nameTypeRoom, nameRoom, numberFormat.format(price) + " VNĐ",
							statusRoom, statusOrder, timeCancelStr });
				} else {
					listOrder.add(new Object[] { entry.getKey(), timeOrderRoomStr, timeCheckInDateStr,
							timeCheckOutDateStr, nameTypeRoom, nameRoom, numberFormat.format(price) + " VNĐ",
							statusRoom, statusOrder, timeCancelStr });
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		Collections.sort(listOrder, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				String time1Str = (String) o1[2];
				String time2Str = (String) o2[2];
				try {
					Date time1 = dateFormat.parse(time1Str);
					Date time2 = dateFormat.parse(time2Str);
					return time1.compareTo(time2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
		Collections.sort(listRoomDaO, new Comparator<Object[]>() {
			@Override
			public int compare(Object[] o1, Object[] o2) {
				String time1Str = (String) o1[2];
				String time2Str = (String) o2[2];
				try {
					Date time1 = dateFormat.parse(time1Str);
					Date time2 = dateFormat.parse(time2Str);
					return time1.compareTo(time2);
				} catch (Exception e) {
					e.printStackTrace();
				}
				return 0;
			}
		});
		model.addAttribute("listRoom", listOrder);
		model.addAttribute("listRoomDaO", listRoomDaO);
		return "user/order-history";
	}

	@GetMapping("/admin/customer")
	public String managerCustomr(Model model) {
		Account account = new Account("", "", "", "", new String[] {}, false, "", "", "");
		model.addAttribute("form", account);
		model.addAttribute("listUser", accountDAO.findAll());
		return "admin/customer";
	}

	@GetMapping("/admin/management")
	public String typeRoom2(Model model, @RequestParam("option") Optional<String> getoption,
			@RequestParam("keyword") Optional<String> getkeyword, @ModelAttribute("typefind") Typeroom typeroom,
			@ModelAttribute("servfind") Serviceroom serviceroom, @ModelAttribute("roomfind") Room room) {
		String option = getoption.orElse("");
		String keyword = getkeyword.orElse("");
		if (option == "" && keyword == "") {
			RoomMap roommap = roomdao.findAll();
			HashMap<String, Room> listRoomMap = new HashMap<>();
			if (roommap != null) {
				for (Map.Entry<String, Room> roomCheck : roommap.entrySet()) {
					Typeroom typeRoom = typeroomdao.findByKey(roomCheck.getValue().getTyperoom());
					Room roomSave = roomdao.findByKey(roomCheck.getKey());
					if (typeRoom != null) {
						roomSave.setTyperoom(typeRoom.getName());
					}
					listRoomMap.put(roomCheck.getKey(), roomSave);
				}
			}
			model.addAttribute("listroom", listRoomMap);
			TyperoomMap type = typeroomdao.findAll();
			ServiceroomMap serv = serviceroomDAO.findAll();
			model.addAttribute("listserv", serv);
			model.addAttribute("listtype", type);
			model.addAttribute("typefind", new Typeroom());
		} else if (option == "" && keyword != "") {
			RoomMap roommap = roomdao.findByname(keyword);
			HashMap<String, Room> listRoomMap = new HashMap<>();
			System.out.println("haha" + roommap);
			if (roommap == null) {
				listRoomMap = roomdao.findAll();
			} else {
				for (Map.Entry<String, Room> roomCheck : roommap.entrySet()) {
					Typeroom typeRoom = typeroomdao.findByKey(roomCheck.getValue().getTyperoom());
					Room roomSave = roomdao.findByKey(roomCheck.getKey());
					if (typeRoom != null) {
						roomSave.setTyperoom(typeRoom.getName());
					}
					listRoomMap.put(roomCheck.getKey(), roomSave);
				}
			}
			model.addAttribute("listroom", listRoomMap);
		} else if (option.equals("Room")) {
			RoomMap roommap = null;
			HashMap<String, Room> listRoomMap = new HashMap<>();
			if (keyword.equals("")) {
				roommap = roomdao.findAll();
			} else {

				roommap = roomdao.findByname(keyword);
			}
			if (roommap != null) {
				for (Map.Entry<String, Room> roomCheck : roommap.entrySet()) {
					Typeroom typeRoom = typeroomdao.findByKey(roomCheck.getValue().getTyperoom());
					Room roomSave = roomdao.findByKey(roomCheck.getKey());
					if (typeRoom != null) {
						roomSave.setTyperoom(typeRoom.getName());
					}
					listRoomMap.put(roomCheck.getKey(), roomSave);
				}
			}
			model.addAttribute("listroom", listRoomMap);
		} else if (option.equals("Typeroom")) {
			TyperoomMap typeroomMap = null;
			if (keyword.equals("")) {
				typeroomMap = typeroomdao.findAll();
			} else {
				typeroomMap = typeroomdao.findByname(keyword);
			}
			model.addAttribute("listtype", typeroomMap);
		} else if (option.equals("Service")) {
			ServiceroomMap roommap = null;
			if (keyword.equals("")) {
				roommap = serviceroomDAO.findAll();
			} else {
				roommap = serviceroomDAO.findByname(keyword);
			}
			model.addAttribute("listserv", roommap);
		}
		return "admin/management";
	}

	@GetMapping("/admin/management/{key}")
	public String typeRoom(Model model, @ModelAttribute("typefind") Typeroom typeroom,
			@ModelAttribute("servfind") Serviceroom serviceroom, @ModelAttribute("roomfind") Room room,
			@PathVariable("key") String kw) {
		TyperoomMap type = typeroomdao.findAll();
		ServiceroomMap serv = serviceroomDAO.findAll();
		RoomMap roommap = roomdao.findAll();
		HashMap<String, Room> listRoomMap = new HashMap<>();
		if (roommap != null) {
			for (Map.Entry<String, Room> roomCheck : roommap.entrySet()) {
				Typeroom typeRoom = typeroomdao.findByKey(roomCheck.getValue().getTyperoom());
				Room roomSave = roomdao.findByKey(roomCheck.getKey());
				if (typeRoom != null) {
					roomSave.setTyperoom(typeRoom.getName());
				}
				listRoomMap.put(roomCheck.getKey(), roomSave);
			}
		}
		Typeroom typefind = typeroomdao.findByKey(kw);
		Room roomfind = roomdao.findByKey(kw);
		Serviceroom servfind = serviceroomDAO.findByKey(kw);
		model.addAttribute("listtype", type);
		model.addAttribute("listroom", listRoomMap);
		model.addAttribute("listserv", serv);
		if (roomfind == null && typefind == null && servfind != null) {
			model.addAttribute("servfind", servfind);
		} else if (roomfind != null && typefind == null && servfind == null) {
			model.addAttribute("roomfind", roomfind);
		} else {
			model.addAttribute("typefind", typefind);
		}
		return "admin/management";
	}

	// History check in
	@RequestMapping("/admin/orders/history-checkin")
	public String historyCheckin(Model model) {
		OrderRoomMap dataMap = orderRoomDAO.getAllRoomForCustomer((String) session.getAttribute("username"));
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		List<Object> listOrder = new ArrayList<>();
		for (Map.Entry<String, OrderRoom> entry : dataMap.entrySet()) {
			OrderRoom orderRoom = entry.getValue();
			String timeCheckInDateStr = dateFormat.format(orderRoom.getDateCheckIn());
			String timeCheckOutDateStr = dateFormat.format(orderRoom.getDateCheckOut());
			String timeOrderRoomStr = dateFormat.format(orderRoom.getDateAt());
			String timeCancelStr = "";
			String nameCustomer = "";
			if (accountDAO.findByUsername(orderRoom.getNameCustomer()) != null) {
				nameCustomer = accountDAO.findByUsername(orderRoom.getNameCustomer()).getFullname();
			} else {
				nameCustomer = orderRoom.getNameCustomer();
			}
			String nameRoom = "";
			String keyTypeRoom = "";
			String statusRoom = "";
			String statusOrder = orderRoom.getStatus();
			for (Room room : orderRoom.getRoom().values()) {
				nameRoom = room.getName();
				keyTypeRoom = room.getTyperoom();
				statusRoom = room.getStatus();
			}
			String nameTypeRoom = typeroomdao.findByKey(keyTypeRoom).getName();
			Long price = (long) (getDateDAO.checkDate(orderRoom.getDateCheckIn(), orderRoom.getDateCheckOut())
					* typeroomdao.findByKey(keyTypeRoom).getPrice());
			listOrder.add(new Object[] { entry.getKey(), timeOrderRoomStr, timeCheckInDateStr, timeCheckOutDateStr,
					nameTypeRoom, nameRoom, price, statusRoom, statusOrder, timeCancelStr, nameCustomer });
		}
		model.addAttribute("historyCheckin", listOrder);
		return "admin/history-checkin";
	}

	@GetMapping("/admin/room")
	public String managerRoom() {
		return "admin/room";
	}
}
