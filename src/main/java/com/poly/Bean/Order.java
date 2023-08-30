package com.poly.Bean;

import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {
	private RoomMap room;
	private Date timeCheckInDate;
	private Date timeCheckOutDate;
	private CustomerMap customer;
	private int numberPeople;
	private String status;
	private ServiceroomMap serviceOrder;
	private String userCreate;
	private PromotionMap promoMap;
	private OrderRoomMap orderRoom;
	private double total;
}