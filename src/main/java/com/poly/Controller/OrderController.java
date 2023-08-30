package com.poly.Controller;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.StringTokenizer;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.server.Session;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.poly.Bean.Customer;
import com.poly.Bean.CustomerMap;
import com.poly.Bean.Order;
import com.poly.Bean.OrderMap;
import com.poly.Bean.OrderRoomMap;
import com.poly.Bean.PromotionMap;
import com.poly.Bean.Room;
import com.poly.Bean.RoomMap;
import com.poly.Bean.Serviceroom;
import com.poly.Bean.ServiceroomMap;
import com.poly.Bean.Typeroom;
import com.poly.DAO.CustomerDAO;

import com.poly.DAO.OrderDAO;
import com.poly.DAO.OrderRoomDAO;

import com.poly.DAO.RoomDAO;
import com.poly.DAO.ServiceroomDAO;
import com.poly.DAO.TyperoomDAO;
import com.poly.Service.Format;
import com.poly.Service.OrderService;
import com.poly.Service.ParamService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
public class OrderController {
	@Autowired
	OrderDAO orderDao;
	@Autowired
	RoomDAO roomDAO;
	@Autowired
	HttpSession session;
	@Autowired
	ParamService paramService;
	@Autowired
	HttpServletRequest request;
	@Autowired
	CustomerDAO customerDAO; 
	@Autowired
	ServiceroomDAO serviceRoomDao;
	@Autowired
	TyperoomDAO typeroomDAO;
	@Autowired
	OrderService orderService;

	@Autowired 
	OrderRoomDAO orderRoomDAO;

	@RequestMapping("/admin/orders")
	public String HomeOrder(Model model) {
		String type = paramService.getString("type", "");
		RoomMap rm = new RoomMap();
		String date = Format.formatDate(new Date());
		OrderRoomMap roomMapOrder = orderRoomDAO.getRoombyDate(date);
		
		RoomMap roomMap = roomDAO.getAllRoom(roomMapOrder);
		
		int roomAvailable = getSizeHashMapByType(roomMap, "1");
		int roomUnAvailable = getSizeHashMapByType(roomMap, "2");
		int roomReserved = getSizeHashMapByType(roomMap, "3");
		int roomOverdue = getSizeHashMapByType(roomMap, "4");
		int roomNotClean = getSizeHashMapByType(roomMap, "5");
		int roomFix = getSizeHashMapByType(roomMap, "6");

		switch (type) {
		case "1": {
			rm = cloneHashMapByType(roomMap, type);

			break;
		}
		case "2": {
			rm = cloneHashMapByType(roomMap, type);
			break;
		}
		case "3": {
			 
			rm = roomMap;
			
			System.out.println("case 3");
			model.addAttribute("typeOrderRoom", "Đặt phòng");
			break;
		}
		case "4": {
			rm = cloneHashMapByType(roomMap, type);

			break;
		}
		case "5": {
			rm = cloneHashMapByType(roomMap, type);

			break;
		}
		case "6": {
			rm = cloneHashMapByType(roomMap, type);

			break;
		}
		default:
			System.out.println("case default");
			rm = roomMap;
		}

		model.addAttribute("roomAvailable", roomAvailable);
		model.addAttribute("roomUnAvailable", roomUnAvailable);
		model.addAttribute("roomReserved", roomReserved);
		model.addAttribute("roomOverdue", roomOverdue);
		model.addAttribute("roomNotClean", roomNotClean);
		model.addAttribute("roomFix", roomFix);
		model.addAttribute("sizeAll", roomMap.size());
		model.addAttribute("sizeRoomOrder", roomMapOrder.size());
		model.addAttribute("rooms", rm);
		System.out.println(("size "+rm.size()));
		return "admin/order";
	}

	public RoomMap cloneHashMapByType(RoomMap roomMap, String type) {
		RoomMap rm = new RoomMap();

		roomMap.entrySet().forEach(entry -> {
			if (entry.getValue().getStatus().equals(type)) {
				rm.put(entry.getKey(), entry.getValue());
			}
		});
		return rm;
	}

	public int getSizeHashMapByType(RoomMap roomMap, String type) {
		RoomMap rm = new RoomMap();
		roomMap.entrySet().forEach(entry -> {
			if (entry.getValue().getStatus().equals(type)) {
				rm.put(entry.getKey(), entry.getValue());

			}
		});
		return rm.size();
	}

	// open modal
	@RequestMapping("/admin/orders/detail-room/{id}")
	public String showModal(@PathVariable("id") String id, Model model) {
		Order order = new Order();
		String status = paramService.getString("status", "");
		ServiceroomMap serviceRoomMap = serviceRoomDao.findAll();
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM:ss dd-MM-yyyy");
		Customer customer = new Customer();
		ServiceroomMap serviceroom = new ServiceroomMap();
		Room room = roomDAO.findByKey(id);

		Typeroom typeroom = typeroomDAO.findByKey(room.getTyperoom());
		String keyOrder = "";
		String totalTime = "";
		String date = "";
		String url = "";
		String action = "";

		switch (status) {
		// phòng đang trống
		case "1": {
			url = "modal-open-room";
			break;
		}
		// phòng đang ở
		case "2": {
			url = "modal-detail-room";
			order = orderDao.findByIdRoom(id);
			date = dateFormat.format(order.getTimeCheckInDate());
			keyOrder = orderDao.findKey(order);
			customer = customerDAO.findByCustomer(order.getCustomer());
			serviceroom = order.getServiceOrder();
			if (order.getServiceOrder() != null && serviceRoomMap != null) {
				serviceRoomMap = serviceRoomDao.findServiceNotOrder(serviceRoomMap, serviceroom);
			}
			totalTime = Format.checkDate(order.getTimeCheckInDate(), new Date());
			break;
		}
		// phòng đã đặt
		case "3": {
			url = "reserved-room";
			break;
		}
		// phòng quá hạn
		case "4": {
			url = "modal-detail-room";
			break;
		}
		// phòng chưa dọn
		case "5": {
			url = "modal-clean-room";
			break;
		}
		// phòng đang sửa
		case "6": {
			url = "modal-detail-room";
			break;
		}
		default:
			throw new IllegalArgumentException("Unexpected value: " + status);
		}
		model.addAttribute("customer", customer);
		model.addAttribute("serviceRooms", serviceRoomMap);
		model.addAttribute("roomOrder", room);
		model.addAttribute("time", date);
		model.addAttribute("order", order);
		model.addAttribute("keyOrder", keyOrder);
		model.addAttribute("serviceOrder", serviceroom);
		model.addAttribute("typeRoom", typeroom);
		model.addAttribute("priceRoom", Format.formatNumber(typeroom.getPrice()));
		model.addAttribute("totalTime", totalTime);
		model.addAttribute("idRoom", id);
		return "admin/modalOrders/" + url;
	}

	// create order
	@RequestMapping(path = "/admin/orders/create/{id}", method = RequestMethod.POST)
	public String createOrder(@PathVariable("id") String idRom, @ModelAttribute("order") Order o,
			@Valid @ModelAttribute("customer") Customer customer, BindingResult error,Model model) {
		if (error.hasErrors()) {
			Room room = roomDAO.findByKey(idRom);
			Typeroom typeroom = typeroomDAO.findByKey(room.getTyperoom());
			model.addAttribute("typeRoom", typeroom);
			model.addAttribute("roomOrder", room);
			model.addAttribute("priceRoom", Format.formatNumber(typeroom.getPrice()));
			
			return "admin/modalOrders/modal-open-room";
		}
		Order order = new Order();
		Date date = new Date();
		// find room need open and update status
		Room room = roomDAO.findByKey(idRom);
		room.setStatus("2");

		// create RoomMap for action create order
		RoomMap map = new RoomMap();
		map.put(idRom, room);

		// save customer
		customerDAO.create(customer);

		// find key customerMap
		String keyCustomer = customerDAO.findKey(customer);

		// create customer map
		CustomerMap customerMap = new CustomerMap();
		customerMap.put(keyCustomer, customer);

		// add value for attribute order
		order.setRoom(map);
		order.setStatus("0");
		order.setCustomer(customerMap);
		order.setTimeCheckInDate(date);
		order.setNumberPeople(o.getNumberPeople());
		order.setServiceOrder(null);
		order.setTimeCheckOutDate(null);
		order.setUserCreate((String) session.getAttribute("username"));
		
		roomDAO.update(idRom, room);
		orderDao.create(order);
		System.out.println("create");
		return "redirect:/admin/orders";
	}

	// request update order	
	@RequestMapping("/update/detail-room-serivce/{id}")
	public String updateOrder(@PathVariable("id") String id, Model model,
			@ModelAttribute("customer") Customer customer) {

		model.addAttribute("id", id);
		HttpSession session = request.getSession();
		session.setAttribute("message", "Sửa thành công phòng " + id);
		return "redirect:/admin/orders";
	}

	// add services to order
	@RequestMapping(path = "/admin/orders/addService/{key}", method = RequestMethod.POST)
	public String addService(@PathVariable("key") String key, Model model) {
		String service = paramService.getString("service", "");
		if (service.equals("")) {
			return "redirect:/admin/orders";
		}
		List<String> list = splitString(service);
		OrderMap map = orderDao.findAll();
		Order order = map.get(key);

		ServiceroomMap serviceroomMap = new ServiceroomMap();

		if (order.getServiceOrder() == null) {
			for (String keySerivce : list) {
				Serviceroom serviceroom = serviceRoomDao.findByKey(keySerivce);
				serviceroomMap.put(keySerivce, serviceroom);
			}
		} else {
			for (String keySerivce : list) {
				Serviceroom findSeriveRoom = serviceRoomDao.findServiceOrder(serviceroomMap, keySerivce);
				if (findSeriveRoom != null) {
					model.addAttribute("messageErrorService", "Không thể thêm 2 dịch vụ giống nhau!");
					return "redirect:/admin/orders";
				}
				Serviceroom serviceroom = serviceRoomDao.findByKey(keySerivce);
				serviceroomMap = order.getServiceOrder();
				serviceroomMap.put(keySerivce, serviceroom);
			}
		}

		order.setServiceOrder(serviceroomMap);
		orderDao.update(key, order);
		return "redirect:/admin/orders";
	}

	@RequestMapping("/admin/orders/pay-form/{keyOrder}")
	public String payFormOrder(@PathVariable("keyOrder") String keyOrder, Model model) {
		System.out.println(keyOrder);
		Order order = orderDao.findByKey(keyOrder);
		String idRoom = paramService.getString("idRoom", "");
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM:ss dd-MM-yyyy");
		String date = dateFormat.format(order.getTimeCheckInDate());
		keyOrder = orderDao.findKey(order);
		Customer customer = customerDAO.findByCustomer(order.getCustomer());
		ServiceroomMap servicerRoomOrder = order.getServiceOrder();
		Room room = roomDAO.findByKey(idRoom);
		String totalTime = Format.checkDate(order.getTimeCheckInDate(), new Date());
		Typeroom typeroom = typeroomDAO.findByKey(room.getTyperoom());
		model.addAttribute("customer", customer);
	
		double totalPS = 0;
		if(servicerRoomOrder != null ) {
			totalPS = serviceRoomDao.totalPriceServiceOrder(servicerRoomOrder);
		}else {
			System.out.println("null service room");
			
		}
		
		String totalPriceService = Format.formatNumber(totalPS);

		double totalMoneyR = orderService.totalMoneyRoom(order.getTimeCheckInDate(), new Date(), typeroom.getPrice());
		String totalMoneyRoom = Format.formatNumber(totalMoneyR);

		double totalMoney = totalPS + totalMoneyR;
		String totalMoneyString = Format.formatNumber(totalMoney);

		model.addAttribute("roomOrder", room);
		model.addAttribute("time", date);
		model.addAttribute("order", order);
		model.addAttribute("keyOrder", keyOrder);
		model.addAttribute("serviceOrder", servicerRoomOrder);
		model.addAttribute("typeRoom", typeroom);
		model.addAttribute("priceRoom", Format.formatNumber(typeroom.getPrice()));
		model.addAttribute("totalTime", totalTime);
		model.addAttribute("totalPriceService", totalPriceService);
		model.addAttribute("totalMoneyRoom", totalMoneyRoom);
		model.addAttribute("totalMoney", totalMoneyString);
	
		model.addAttribute("idRoom", idRoom);
		return "admin/modalOrders/Modal-pay";
	}

	@RequestMapping("/admin/orders/pay/{keyOrder}")
	public String payOrder(@PathVariable("keyOrder") String keyOrder, Model model) {
		Order order = orderDao.findByKey(keyOrder);
		String idRoom = paramService.getString("idRoom", "");
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM:ss dd-MM-yyyy");
		String date = dateFormat.format(order.getTimeCheckInDate());
		keyOrder = orderDao.findKey(order);
		Customer customer = customerDAO.findByCustomer(order.getCustomer());
		ServiceroomMap servicerRoomOrder = order.getServiceOrder();
		Room room = roomDAO.findByKey(idRoom);
		String totalTime = Format.checkDate(order.getTimeCheckInDate(), new Date());
		Typeroom typeroom = typeroomDAO.findByKey(room.getTyperoom());
		model.addAttribute("customer", customer);

		double totalPS = 0;
		double totalMoneyR = orderService.totalMoneyRoom(order.getTimeCheckInDate(), new Date(), typeroom.getPrice());
		if(servicerRoomOrder != null) {
			 totalPS = serviceRoomDao.totalPriceServiceOrder(servicerRoomOrder);
		}
		String totalPriceService = Format.formatNumber(totalPS);
		String totalMoneyRoom = Format.formatNumber(totalMoneyR);
		double totalMoney = totalPS + totalMoneyR;
		String totalMoneyString = Format.formatNumber(totalMoney);
		

		Date dateCheckout = new Date();
		order.setTimeCheckOutDate(dateCheckout);
		order.setStatus("1");
		order.setTotal(totalMoney);
		room.setStatus("5");
		System.out.println("pay");
		roomDAO.update(idRoom, room);
		orderDao.update(keyOrder, order);

		model.addAttribute("paySuccess", "Thanh toán thành công");
		model.addAttribute("roomOrder", room);
		model.addAttribute("time", date);
		model.addAttribute("order", order);
		model.addAttribute("keyOrder", keyOrder);
		model.addAttribute("serviceOrder", servicerRoomOrder);
		model.addAttribute("typeRoom", typeroom);
		model.addAttribute("priceRoom", Format.formatNumber(typeroom.getPrice()));
		model.addAttribute("totalTime", totalTime);
		model.addAttribute("totalPriceService", totalPriceService);
		model.addAttribute("totalMoneyRoom", totalMoneyRoom);
		model.addAttribute("totalMoney", totalMoneyString);
	
		model.addAttribute("idRoom", idRoom);
		return "admin/modalOrders/Modal-pay";
	}
	@RequestMapping("/admin/orders/clean-room/{keyRoom}")
	public String cleanRoom(@PathVariable("keyRoom") String keyRoom) {
		Room room = roomDAO.findByKey(keyRoom);
		room.setStatus("1");
		roomDAO.update(keyRoom, room);
		return "redirect:/admin/orders";
	}
	// subtring list service
	public static List<String> splitString(String string) {
		List<String> substrings = new ArrayList<>();
		StringTokenizer tokenizer = new StringTokenizer(string, ",");
		while (tokenizer.hasMoreTokens()) {
			substrings.add(tokenizer.nextToken());
		}
		return substrings;
	}
}
