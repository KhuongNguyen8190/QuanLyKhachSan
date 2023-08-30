package com.poly.Bean;


import java.util.Date;

import org.springframework.web.multipart.MultipartFile;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Serviceroom {
	String name;
	double price;
	Date datecreated;
	boolean status;
	String usercreated;
	String description;
	String image;
}
