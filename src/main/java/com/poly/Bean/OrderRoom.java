 package com.poly.Bean;

import java.util.Date;

import com.google.firebase.database.annotations.NotNull;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRoom {
	@NotBlank
	private String nameCustomer;
	@NotBlank
	private String phoneCustomer;

	private String CCCDCustomer;
	private RoomMap room;
	private Date dateAt;
	private Date dateCheckIn;
	private Date dateCheckOut;
	private String status;
	private Date dateCancel;
	@NotNull
	private int numberPeople;
	boolean typeOrder;
	
}
