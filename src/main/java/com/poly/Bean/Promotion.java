package com.poly.Bean;

import java.util.Date;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Promotion {
	private String name;
	private double value;
	private Date dateAt;
	private Date dateStart;
	private Date dateEnd;
	private boolean status;
}
