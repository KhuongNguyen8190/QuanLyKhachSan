package com.poly.Controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.poly.Bean.Room;
import com.poly.Bean.RoomMap;
import com.poly.DAO.RoomDAO;

@Controller
public class RoomController {
	@Autowired
	RoomDAO Roomdao;

	@PostMapping("/createRoom")
	public String addRoom(Model model, @ModelAttribute Room Room) {
		Room.setStatus("1");
		String isCheck = Roomdao.create(Room);
		if (isCheck == (null)) {
			return "redirect:/admin/management?option=Room&status=false";
		}
		return "redirect:/admin/management??option=Room&status=true";
	}

	@PostMapping("/updateRoom/{key}")
	public String updateRoom(Model model, @ModelAttribute Room Room, @PathVariable("key") String key) {
		Room isCheck = Roomdao.update(key, Room);
		if (isCheck == (null)) {
			return "redirect:/admin/management??option=Room&status=false";
		}
		return "redirect:/admin/management??option=Room&status=true";
	}

	@PostMapping("/deleteRoom/{key}")
	public String deleteRoom(@PathVariable("key") String key) {
		Roomdao.delete(key);
		return "redirect:/admin/management??option=Room&status=true";
	}
}
