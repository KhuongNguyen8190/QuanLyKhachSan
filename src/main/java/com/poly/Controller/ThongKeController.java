package com.poly.Controller;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.Bean.*;
import com.poly.DAO.*;
import com.poly.Service.LoginResponse;

import jakarta.servlet.http.HttpSession;

@Controller
public class ThongKeController {
	@Autowired
	OrderDAO orderDAO;
	@Autowired
	TyperoomDAO typeRoomDAO;
	@Autowired
	GetDateDAO dateDAO;
	@Autowired
	HttpSession session;

	@RequestMapping("/admin/index")
	@Secured("ROLE_ADMIN")
	public String thongKeAll(Model model) {
		OrderMap listOrder = orderDAO.findAll();
		Double total = 0.0;
		Double totalAll = 0.0;
		Integer sumPeople = 0;
		Integer sumPeopleAll = 0;
		Map<Integer, Boolean> uniqueMonths = new HashMap<>();
		Map<Integer, Boolean> uniqueYears = new HashMap<>();
		NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);

		for (Map.Entry<String, Order> entryOrder : listOrder.entrySet()) {
			int month = entryOrder.getValue().getTimeCheckInDate().getMonth() + 1;
			int year = entryOrder.getValue().getTimeCheckInDate().getYear() + 1900;
			uniqueMonths.put(month, true);
			uniqueYears.put(year, true);
		}
		List<Object[]> listThongKe = new ArrayList<Object[]>();
		for (Integer year : uniqueYears.keySet()) {
			for (Integer month : uniqueMonths.keySet()) {
				for (Map.Entry<String, Order> entryOrder : listOrder.entrySet()) {
					if (entryOrder.getValue().getStatus().equals("1")) {
						Order order = entryOrder.getValue();
						if ((order.getTimeCheckInDate().getMonth() + 1) == month
								&& (order.getTimeCheckInDate().getYear() + 1900) == year) {
							total += order.getTotal();
							if (order.getServiceOrder() != null) {
								for (Serviceroom entryService : order.getServiceOrder().values()) {
									total += entryService.getPrice();
								}
								sumPeople += order.getNumberPeople();
							}
						}
					}
				}
				sumPeopleAll += sumPeople;
				totalAll += total;
				String time = String.valueOf(month) + "/" + String.valueOf(year);
				String formattedPrice = numberFormat.format(total) + " VNĐ";
				if (total != 0) {
					listThongKe.add(new Object[] { time, sumPeople, formattedPrice });
				}
				total = 0.0;
				sumPeople = 0;
			}
		}
		model.addAttribute("listThongKe", listThongKe);
		session.setAttribute("listThongKe", listThongKe);
		model.addAttribute("totalAll", numberFormat.format(totalAll) + " VNĐ");
		model.addAttribute("sumPeople", sumPeopleAll);
		return "admin/index";
	}

	@GetMapping("admin/thongKe/filter")
	@ResponseBody
	public ResponseEntity<Map<String, Object>> filterThongKe(Model model, @RequestParam("dateForm") String dateFrom,
			@RequestParam("dateTo") String dateTo) {
		Map<String, Object> response = new HashMap<>();
		Double total = 0.0;
		Integer sumPeople = 0;
		String time = "";

		OrderMap listOrder = orderDAO.findAll();
		Map<Integer, Boolean> uniqueMonths = new HashMap<>();
		Map<Integer, Boolean> uniqueYears = new HashMap<>();
		Map<Integer, Boolean> uniqueDays = new HashMap<>();
		Map<String, DayInfo> dayInfoMap = new HashMap<>();

		for (Map.Entry<String, Order> entryOrder : listOrder.entrySet()) {
			int month = entryOrder.getValue().getTimeCheckInDate().getMonth() + 1;
			int year = entryOrder.getValue().getTimeCheckInDate().getYear() + 1900;
			int day = entryOrder.getValue().getTimeCheckInDate().getDate();
			uniqueMonths.put(month, true);
			uniqueYears.put(year, true);
			uniqueDays.put(day, true);
		}

		List<Object[]> listThongKe = new ArrayList<Object[]>();
		try {
			if (dateFrom == "" || dateTo == "") {
				response.put("success", true);
				response.put("message", "Dữ liệu đã được tải thành công!");
				response.put("data", session.getAttribute("listThongKe"));
			} else {
				if (dateDAO.checkDate(dateDAO.getDate(dateFrom), dateDAO.getDate(dateTo)) >= 1) {
					for (Integer year : uniqueYears.keySet()) {
						for (Integer month : uniqueMonths.keySet()) {
							for (Map.Entry<String, Order> entryOrder : listOrder.entrySet()) {
								if (entryOrder.getValue().getStatus().equals("1")) {
									Order order = entryOrder.getValue();
									if ((order.getTimeCheckInDate().getMonth() + 1) == month
											&& (order.getTimeCheckInDate().getYear() + 1900) == year) {
										if (dateDAO.checkDateFilter(order.getTimeCheckInDate(),
												dateDAO.getDate(dateFrom), dateDAO.getDate(dateTo))) {
											for (Integer day : uniqueDays.keySet()) {
												if (order.getTimeCheckInDate().getDate() == day) {
													for (Room entryRoom : order.getRoom().values()) {
														Typeroom typeRoom = typeRoomDAO
																.findByKey(entryRoom.getTyperoom());
														total += (typeRoom.getPrice()
																* dateDAO.checkDate(order.getTimeCheckInDate(),
																		order.getTimeCheckOutDate()));
													}
													for (Serviceroom entryService : order.getServiceOrder().values()) {
														total += entryService.getPrice();
													}
													sumPeople += order.getNumberPeople();
													time = String.valueOf(order.getTimeCheckInDate().getDate()) + "/"
															+ String.valueOf(order.getTimeCheckInDate().getMonth() + 1)
															+ "/" + String.valueOf(
																	order.getTimeCheckInDate().getYear() + 1900);
													if (dayInfoMap.containsKey(time)) {
														DayInfo dayInfo = dayInfoMap.get(time);
														dayInfo.setTotal(dayInfo.getTotal() + total);
														dayInfo.setSumPeople(dayInfo.getSumPeople() + sumPeople);
													} else {
														DayInfo dayInfo = new DayInfo();
														dayInfo.setTotal(total);
														dayInfo.setSumPeople(sumPeople);
														dayInfoMap.put(time, dayInfo);
													}
												}
											}
										}
										total = 0.0;
										sumPeople = 0;
									}
								}
							}
						}
					}
					NumberFormat numberFormat = NumberFormat.getNumberInstance(Locale.US);
					for (Map.Entry<String, DayInfo> entry : dayInfoMap.entrySet()) {
						String day = entry.getKey();
						DayInfo dayInfo = entry.getValue();
						String formattedPrice = numberFormat.format(dayInfo.getTotal()) + " VNĐ";
						listThongKe.add(new Object[] { day, dayInfo.getSumPeople(), formattedPrice });
					}
					Collections.sort(listThongKe, new Comparator<Object[]>() {
						@Override
						public int compare(Object[] o1, Object[] o2) {
							String time1Str = (String) o1[0];
							String time2Str = (String) o2[0];

							SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
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
					response.put("success", true);
					response.put("message", "Dữ liệu đã được tải thành công!");
					response.put("data", listThongKe);
				} else {
					response.put("success", false);
					response.put("message", "Ngày tìm kiếm không hợp lệ!");
				}
			}
		} catch (Exception e) {
			response.put("success", false);
			response.put("message", "Tải lên dữ liệu thất bại!");
		}
		return ResponseEntity.ok(response);
	}

	static class DayInfo {
		private double total;
		private int sumPeople;

		public double getTotal() {
			return total;
		}

		public void setTotal(double total) {
			this.total = total;
		}

		public int getSumPeople() {
			return sumPeople;
		}

		public void setSumPeople(int sumPeople) {
			this.sumPeople = sumPeople;
		}
	}
}
