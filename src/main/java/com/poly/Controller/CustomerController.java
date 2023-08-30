package com.poly.Controller;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.poly.Bean.Account;
import com.poly.Bean.AccountMap;
import com.poly.DAO.AccountDAO;
import com.poly.Service.LoginResponse;

import jakarta.servlet.http.HttpSession;

@Controller
public class CustomerController {
	@Autowired
	AccountDAO dao;
	@Autowired
	HttpSession session;

	@PostMapping("/admin/findCustomer")
	public String findCustomer(Model model, @RequestParam("keyword") String keyword) {
		AccountMap listAccount = null;
		Account account = new Account("", "", "", "", new String[] {}, false, "", "", "");
		model.addAttribute("form", account);
		try {
			if(keyword == null || keyword == "") {
				listAccount = dao.findAll();
			}else {
				listAccount = dao.findByName(keyword);
			}
			System.out.println("Size: "+listAccount.size());
			model.addAttribute("listUser", listAccount);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "admin/customer";
	}

	@RequestMapping("/admin/updateCustomer/{id}")
	@ResponseBody
	public ResponseEntity<LoginResponse> createCustomer(Model model, @RequestParam("username") String username,
			@RequestParam("fullname") String fullname, @RequestParam("cccd") String cccd,
			@RequestParam("phone") String phone, @RequestParam("address") String address,
			@RequestParam("gender") boolean gender, @PathVariable("id") String key) {
		LoginResponse response = new LoginResponse();
		response.setSuccess(false);
		String message = "";
		try {
			Account account = dao.findByKey(key);
			account.setAddress(address);
			account.setCccd(cccd);
			account.setFullname(fullname);
			account.setPhone(phone);
			account.setGender(gender);
			dao.update(key, account);
			message = "Cập nhật thông tin thành công!";
			response.setSuccess(true);
		} catch (Exception e) {
			message = "Cập nhật thông tin thất bại!";
			e.printStackTrace();
		}
		response.setMessage(message);
		return ResponseEntity.ok(response);
	}

	@RequestMapping("/admin/editCustomer/{key}")
	public String editCustomer(Model model, @PathVariable("key") String key) {
		model.addAttribute("form", dao.findByKey(key));
		AccountMap list = dao.findAll();
		model.addAttribute("listUser", list);
		return "admin/customer";
	}

	@RequestMapping("/admin/deleteCustomer/{key}")
	public String deleteCustomer(Model model, @PathVariable("key") String key) {
		dao.delete(key);
		return "redirect:/admin/customer";
	}

}
