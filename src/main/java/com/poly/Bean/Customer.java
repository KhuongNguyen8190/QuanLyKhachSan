package com.poly.Bean;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Customer {
	@NotBlank
	private String name;
	@NotBlank
	private String phone;
	@NotBlank
	private String CCCD;
}
