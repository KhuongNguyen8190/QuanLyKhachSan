package com.poly.Bean;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DateCheck {
	@NotBlank
	private String dateStart;
	@NotBlank
	private String dateEnd;
}
