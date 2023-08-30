package com.poly.Controller;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.Errors;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.poly.Bean.Typeroom;
import com.poly.Bean.TyperoomMap;
import com.poly.DAO.TyperoomDAO;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

@Controller
public class TyperoomController {
	@Autowired
	TyperoomDAO typeroomdao;
	@Autowired
	HttpServletRequest request;;
	
	@PostMapping("/createTyperoom")
	public String addTyperoom(Model model, @RequestParam("images") List<MultipartFile> images) {
		try {
			String[] listImages = null;
			Typeroom typeroom = new Typeroom();
			String name = request.getParameter("name");
			Double price = Double.parseDouble(request.getParameter("price"));
			String description = request.getParameter("description");
			if (images.size() > 0) {
				// Lấy tên file tải lên
				List<String> nameToSave = new ArrayList<>();
				for (MultipartFile file : images) {
					byte[] fileData = file.getBytes();
					String base64EncodedImage = Base64.getEncoder().encodeToString(fileData);
					nameToSave.add(base64EncodedImage);
				}

				listImages = nameToSave.toArray(new String[0]);
			}
			typeroom.setName(name);
			typeroom.setPrice(price);
			typeroom.setDescription(description);
			typeroom.setImages(listImages);
			String isCheck=typeroomdao.create(typeroom);
			if (isCheck == (null)) {
				return "redirect:/admin/management?option=Typeroom&status=false";
			}
			return "redirect:/admin/management?option=Typeroom&status=true";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/management";
	}

	@PostMapping("/updateTyperoom/{key}")
	public String updateTyperoom(Model model, @RequestParam("imagesUpdate") List<MultipartFile> images,
			@PathVariable("key") String key) {
		try {
			String[] listImages = null;
			Typeroom typeroom = new Typeroom();
			String name = request.getParameter("name");
			Double price = Double.parseDouble(request.getParameter("price"));
			String description = request.getParameter("description");
			System.out.println("Số ảnh được chọn: "+images.size());
			if (images.size() > 0) {
				// Lấy tên file tải lên
				List<String> nameToSave = new ArrayList<>();
				for (MultipartFile file : images) {
					byte[] fileData = file.getBytes();
					String base64EncodedImage = Base64.getEncoder().encodeToString(fileData);
					nameToSave.add(base64EncodedImage);
					System.out.println("Tên ảnh: "+base64EncodedImage);
				}

				listImages = nameToSave.toArray(new String[0]);
			}else {
				listImages = typeroomdao.findByKey(key).getImages();
			}
			typeroom.setName(name);
			typeroom.setPrice(price);
			typeroom.setDescription(description);
			typeroom.setImages(listImages);
			Typeroom isCheck= typeroomdao.update(key, typeroom);
			if (isCheck == (null)) {
				return "redirect:/admin/management?option=Typeroom&status=false";
			}
			return "redirect:/admin/management?option=Typeroom&status=true";
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "redirect:/admin/management/" + key;
	}

	@PostMapping("/deleteTyperoom/{key}")
	public String deleteTyperoom(@PathVariable("key") String key) {
		typeroomdao.delete(key);
		return "redirect:/admin/management?option=Typeroom&status=true";	}
}