package com.bleizing.jjfitness.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

public class DateUtil {
	private static ZoneId getZoneId() {
		return ZoneId.of("Asia/Jakarta");
	}
	public static LocalDateTime getCurrentDateTime() {
		return LocalDateTime.now(getZoneId());
	}

	public static LocalDateTime parseDateFormat(String pattern, String date) {
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
		return LocalDateTime.parse(date, formatter);
	}
	
	public static LocalDate getCurrentDate() {
		return LocalDate.now(getZoneId());
	}
	
	public static String minutesToDateTime(String minutes) {
//		return "2024-12-16T" + minutes + ":00";
		return getCurrentDate() + "T" + minutes + ":00";
	}
}
