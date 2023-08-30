package com.poly.DAO;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

import org.springframework.stereotype.Repository;

@Repository
public class GetDateDAO {
	public Date getDate(String date) {
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
			Date utilDate = sdf.parse(date);
			return utilDate;
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	public long checkDate(Date dateCheckIn, Date dateCheckOut) {
		long timeCheckIn = dateCheckIn.getTime();
		long timeCheckOut = dateCheckOut.getTime();
		long differenceInMillis = timeCheckOut - timeCheckIn;
		long daysDifference = differenceInMillis / (24 * 60 * 60 * 1000);
		if (daysDifference == 0) {
			return 1;
		} else {
			return daysDifference;
		}
	}

	public boolean checkDateFilter(Date checkIn, Date from, Date to) {
		if (checkDate(from, checkIn) >= 0 && checkDate(checkIn, to) >= 0) {
			return true;
		}
		return false;
	}
}
