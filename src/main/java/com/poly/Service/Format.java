package com.poly.Service;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.springframework.stereotype.Service;

@Service
public class Format {
	public static String formatNumber(double number) {
		Locale locale = new Locale("vi", "VN");
		NumberFormat numberFormat = NumberFormat.getNumberInstance(locale);

		return numberFormat.format(number);
	}
	public static String formatTime(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("HH:MM dd-MM-yyyy");
		return dateFormat.format(date);
	}
	public static String formatDate(Date date) {
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		return dateFormat.format(date);
	}
	public static String checkDate(Date dateCheckIn, Date dateCheckOut) {
		long timeCheckIn = dateCheckIn.getTime();
		long timeCheckOut = dateCheckOut.getTime();
		long differenceInMillis = timeCheckOut - timeCheckIn;
		long daysDifference = differenceInMillis / (1000 * 60 * 60);
		long diffSeconds = differenceInMillis / 1000 % 60;
		long diffMinutes = differenceInMillis / (60 * 1000) % 60;
		long diffHours = differenceInMillis / (60 * 60 * 1000) % 24;
		long diffDays = differenceInMillis / (24 * 60 * 60 * 1000);
		
		String dayformat = diffDays + " Ngày " + diffHours + " giờ " + diffMinutes +" phút";
		
		return dayformat;
	}

	public static Date getTypeDate(String sDate)  {
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
		Date dateTime = null;
		try {
			dateTime = dateFormat.parse(sDate);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return dateTime;
	}
}
