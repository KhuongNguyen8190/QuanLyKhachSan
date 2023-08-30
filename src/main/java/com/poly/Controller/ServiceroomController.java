package com.poly.Controller;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import com.poly.Bean.Serviceroom;
import com.poly.DAO.ServiceroomDAO;
import com.poly.Service.ParamService;

@Controller
public class ServiceroomController {
	@Autowired
	ServiceroomDAO serviceroomdao;
	@Autowired
	ParamService paramService;

	@PostMapping("/createServiceroom")
	public String addServiceroom(Model model, @ModelAttribute Serviceroom Serviceroom) {
		Date date = new Date();
		Serviceroom.setDatecreated(date);
		Serviceroom.setStatus(true);
		String isCheck = serviceroomdao.create(Serviceroom);
		if (isCheck == (null)) {
			return "redirect:/admin/management?option=Service&status=false";
		}
		return "redirect:/admin/management?option=Service&status=true";
	}

	@PostMapping("/updateServiceroom/{key}")
	public String updateServiceroom(Model model, @ModelAttribute Serviceroom serviceroom,
			@PathVariable("key") String key) {
		Serviceroom serv = serviceroomdao.findByKey(key);
		serviceroom.setDatecreated(serv.getDatecreated());
		serviceroom.setUsercreated(serv.getUsercreated());
		Serviceroom isCheck= serviceroomdao.update(key, serviceroom);
		if (isCheck == (null)) {
			return "redirect:/admin/management?option=Service&status=false";
		}
		return "redirect:/admin/management?option=Service&status=true";
	}

	@PostMapping("/deleteServiceroom/{key}")
	public String deleteServiceroom(@PathVariable("key") String key) {
		serviceroomdao.delete(key);
		return "redirect:/admin/management?option=Service&status=true";
	}
}
