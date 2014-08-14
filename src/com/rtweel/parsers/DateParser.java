package com.rtweel.parsers;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DateParser {

	public static String parse(String inputDate) {
		Date fullDate = new Date(); // Sun Jul 27 06:17:10 GMT+03:00 2014

		SimpleDateFormat dateFormat = new SimpleDateFormat(
				"EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);// getDefault());
		try {
			fullDate = dateFormat.parse(inputDate);
		} catch (ParseException e) {
			e.printStackTrace();
			return inputDate;
		}

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fullDate);

		String day, month;
		if (calendar.get(Calendar.DAY_OF_MONTH) < 10) {
			day = "0" + calendar.get(Calendar.DAY_OF_MONTH);
		} else {
			day = String.valueOf(calendar.get(Calendar.DAY_OF_MONTH));
		}

		if (calendar.get(Calendar.MONTH) < 10) {
			month = "0" + (calendar.get(Calendar.MONTH) + 1);
		} else {
			month = String.valueOf(calendar.get(Calendar.MONTH));
		}

		String date = day + "." + month + "." + calendar.get(Calendar.YEAR);

		String hours, minutes, seconds;
		if (calendar.get(Calendar.HOUR_OF_DAY) < 10) {
			hours = "0" + calendar.get(Calendar.HOUR_OF_DAY);
		} else {
			hours = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
		}

		if (calendar.get(Calendar.MINUTE) < 10) {
			minutes = "0" + calendar.get(Calendar.MINUTE);
		} else {
			minutes = String.valueOf(calendar.get(Calendar.MINUTE));
		}

		if (calendar.get(Calendar.SECOND) < 10) {
			seconds = "0" + calendar.get(Calendar.SECOND);
		} else {
			seconds = String.valueOf(calendar.get(Calendar.SECOND));
		}

		String time = hours + ":" + minutes + ":" + seconds;

		String finalDate = date + " " + time;
		
		return finalDate;
	}

}
