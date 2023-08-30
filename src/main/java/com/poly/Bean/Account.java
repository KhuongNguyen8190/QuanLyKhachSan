package com.poly.Bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Account {
	String username;
	String password;
	String fullname;
	String cccd;
	String[] role;
	boolean gender;
	String address;
	String phone;
	String image;
	
}
