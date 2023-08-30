package com.poly.Service;

import java.util.Date;

import org.springframework.stereotype.Service;

import com.poly.Bean.Order;

@Service
public class OrderService {
	public double totalMoneyRoom(Date dateCheckIn, Date dateCheckOut, double priceRoom) {
		double totalMoneyRoom = 1;
		long timeCheckIn = dateCheckIn.getTime();
		long timeCheckOut = dateCheckOut.getTime();
		long differenceInMillis = timeCheckOut - timeCheckIn;
		long daysDifference = differenceInMillis / (1000 * 60 * 60);

		long diffSeconds = differenceInMillis / 1000 % 60;
		long diffMinutes = differenceInMillis / (60 * 1000) % 60;
		long diffHours = differenceInMillis / (60 * 60 * 1000) % 24;
		long diffDays = differenceInMillis / (24 * 60 * 60 * 1000);

		if (diffDays > 0 && diffHours < 6) {
			totalMoneyRoom = priceRoom * diffDays;
		} else if (diffDays > 0 && diffHours > 6) {
			totalMoneyRoom = priceRoom * diffDays + priceRoom;
		} else if (diffHours == 0 && diffMinutes < 10) {
			totalMoneyRoom = 0;
		} else {
			totalMoneyRoom = priceRoom;
		}
		return totalMoneyRoom;
	}

	public void test() {
		System.out.println("test");
	}
}
