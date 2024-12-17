package com.bleizing.jjfitness.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeUtil {
	public static final String MINUTES = "minutes";
	public static final String SECONDS = "seconds";
	
	public static long getMinutesTimeDiff(String timeCompare) {
		long timeDiff = formatDurationTo(MINUTES, getTimeBetween(DateUtil.getCurrentDateTime().toString(), timeCompare));
		return timeDiff;
	}
	
	public static int getCurrentHour() {
		return DateUtil.getCurrentDateTime().getHour();
	}
	
	public static int getCurrentMinute() {
		return DateUtil.getCurrentDateTime().getMinute();
	}
	
	public static String getCurrentTime() {
		return checkTimeLenth(getCurrentHour()) + ":" + checkTimeLenth(getCurrentMinute());
	}
	
	public static String checkTimeLenth(int time) {
		return time < 10 ? "0" + time : String.valueOf(time);
	}
	
	public static boolean isTimePassed(String timeCompare) {
		return getTimeBetween(DateUtil.minutesToDateTime(getCurrentTime()), timeCompare).isNegative();
	}
	
	public static Duration getTimeBetween(String start, String end) {
		return Duration.between(LocalDateTime.parse(start), LocalDateTime.parse(end));
	}
	
	public static long formatDurationTo(String type, Duration duration) {
		long value = 0L;
		
		switch (type) {
			case MINUTES:
				value = duration.toMinutes();
				break;
			case SECONDS:
				value = duration.toSeconds();
				
				break;
	
			default:
				break;
		}
		
		return value;
	}
}
