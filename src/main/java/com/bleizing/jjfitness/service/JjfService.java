package com.bleizing.jjfitness.service;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import com.bleizing.jjfitness.dto.request.WarJjfRequest;
import com.bleizing.jjfitness.util.DateUtil;
import com.bleizing.jjfitness.util.TimeUtil;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class JjfService {

	private final String BASE_URL = "https://jjmember.ampabadev.com";

	private RestTemplate restTemplate = new RestTemplate();
	
	private boolean retry = true;
	private boolean apiRetry = false;
	
	private String username;
	private String password;
	private String cookie;
	private String idBooking;
	
	private boolean doProcess = true;
	
	private long interval;
	private long intervalTime = 2 * 1000;
	private long intervalTimeMax = 2 * 60 * 1000;
	
	private long minutesLessThan = 1;
	
	private int countBook = 0;
	private int countBookMax = 3;
	
	private int countRetry = 0;
	
	private Map<String, String> classMap;
	
	public void warJjf(WarJjfRequest request) {
		log.info("Start warJjf {}", request);
		initClassMap();
		
		username = request.getUsername();
		password = request.getPassword();
		
		Thread thread = new Thread() {
			public void run() {
				try {
					do {
						++countRetry;
						log.info("countRetry = " + countRetry);
						
						long timeDiff = TimeUtil.getMinutesTimeDiff(getClassMap(request.getWoName()));
						if (timeDiff <= minutesLessThan) {
							interval = intervalTime;
							doProcess = true;
						} else {
							interval = timeDiff * 60 * 1000;
							if (interval > intervalTimeMax) {
								interval = intervalTimeMax;
							}
							doProcess = false;
						}
						
						log.info("Retry in = " + interval);
						
						if (doProcess) {
							checkLogin(cookie);
							menu(request.getWoName());
							book();
//							bookCancel();
						}
						
						Thread.sleep(interval);
					} while (retry);
					
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	private void checkLogin(String response) {
		log.info("Start checkLogin");
		
		boolean needLogin = false;
		
		if (cookie == null) {
			needLogin = true;
		}
		
		if (response != null && response.contains("Masukkan Nama Pengguna dan Kata Sandi Anda")) {
			needLogin = true;
			cookie = null;
			apiRetry = true;
		}
		
		if (needLogin) {
			login();
		}
	}
	
	private void login() {
		log.info("Start login");
		
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
		MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
		
		map.add("namapengguna", username);
		map.add("katasandi", password);
		
		HttpEntity<MultiValueMap<String, String>> req = new HttpEntity<>(map, headers);
		
		ResponseEntity<String> response = hitApi("/secure_login.php", HttpMethod.POST, req);
		String resp = response.getHeaders().toString();
		
		cookie = "PHPSESSID=";
		if (resp.contains("PHPSESSID")) {
			String[] setCookie = resp.split("PHPSESSID=");
			String[] phpSessId = setCookie[1].split(";");
			cookie = cookie + phpSessId[0];
			apiRetry = false;
		}
	}
	
	private void menu(String woName) {
		log.info("Start menu");
		
		String resp = "";
		ResponseEntity<String> response;
		
		response = hitApi("/menu.php?open=booking-view", HttpMethod.GET, setCookieHeader());
		
		if (response != null) {
			resp = response.getBody();
			
			String[] tbodys = resp.split("<tbody>");
			String tr = "";
			
			boolean needBooking = true;
			boolean filterBooked = false;
			
			if (tbodys.length > 2) {
				filterBooked = true;
			}
			
			if (filterBooked) {
				if (tbodys[1].contains(woName.toUpperCase())) {
					log.info("Already booked");
					needBooking = false;
					retry = false;
				}
			}
			
			if (needBooking) {
				log.info("Need to booking");
				
				resp = tbodys[tbodys.length - 1];
				String[] trs = resp.split("<tr>");
				for (int i = 0; i < trs.length; i++) {
					trs[i] = trs[i].split("</tr>")[0];
					if (trs[i].contains(woName.toUpperCase())) {
						tr = trs[i];
						break;
					}
				}
				
				if (!tr.equals("")) {
					log.info("Class is open");
					
					if (!tr.contains("<td>0</td>")) {
						log.info("Class has quota");
						
						String tanggal = tr.split("<td>")[2];
						tanggal = tanggal.split("</td>")[0];
						tanggal = tanggal.charAt(0) == ' ' ? tanggal.replaceFirst("\\s+","") : tanggal;
						
						if (!TimeUtil.isTimePassed(DateUtil.parseDateFormat("dd-MM-yyyy HH:mm", tanggal).toString())) {
							log.info("Class time incoming");
							
							if (tr.contains("open=booking-detail&id=")) {
								String[] ahref = tr.split("open=booking-detail&id=");
								String[] id = ahref[1].split("\"");
								idBooking = id[0];
							}
						} else {
							log.info("Class time passed out");
							retry = false;
						}
					} else {
						log.info("Class has no quota");
						retry = false;
					}
				} else {
					log.info("Class not open");
					retry = true;
				}
			} else {
				log.info("No need to booking");
				retry = false;
			}
		}
	}
	
	private void book() {
		log.info("Start book = " + idBooking);

		if (idBooking != null) {
			ResponseEntity<String> response = hitApi("/booking-add.php?id=" + idBooking, HttpMethod.GET, setCookieHeader());
			countBook++;
			if (response.getStatusCode().equals(HttpStatus.FOUND) ||response.getStatusCode().equals(HttpStatus.OK)) {
				log.info("Book success");
				retry = false;
			} else {
				log.info("Book failed");
				retry = true;
			}
			
			if (countBook > countBookMax) {
				retry = false;
			}
		}
	}
	
	private void bookCancel() {
		log.info("Start book cancel = " + idBooking);

		if (idBooking != null) {
			ResponseEntity<String> response = hitApi("/booking-cancel.php?id=" + idBooking, HttpMethod.GET, setCookieHeader());
			if (response.getStatusCode().equals(HttpStatus.FOUND)) {
				log.info("book cancel success");
				retry = false;
			}
		}
	}
	
	private void logout() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("cookie", cookie);
		HttpEntity<Void> requestEntity = new HttpEntity<>(headers);

		ResponseEntity<String> response = hitApi("/keluar.php", HttpMethod.GET, requestEntity);
		
		if (response.getStatusCode().equals(HttpStatus.FOUND)) {
			cookie = null;
		}
	}
	
	private HttpEntity<Void> setCookieHeader() {
		HttpHeaders headers = new HttpHeaders();
		headers.set("cookie", cookie);
		return new HttpEntity<>(headers);
	}
	
	private ResponseEntity<String> hitApi(String url, HttpMethod httpMethod, HttpEntity<?> request) {
		ResponseEntity<String> response = null;
		try {
			response = restTemplate.exchange(BASE_URL + url, httpMethod, request, String.class);
			if (!url.contains("login")) {
				checkLogin(response.getBody());
			}
			apiRetry = false;
		} catch (Exception e) {
			apiRetry = true;
		}
		
		if (apiRetry) {
			response = hitApi(url, httpMethod, request);
		}
		
		return response;
	}
	
	private void initClassMap() {
		classMap = new HashMap<>();
		classMap.put("muaythai", "10:45");
		classMap.put("spinning", "21:00");
	}
	
	private String getClassMap(String woName) {
		log.info("getClassMap for = " + woName);
		String value = "08:00";
		
		if (woName != null && !woName.equals("")) {
			if (classMap != null && classMap.size() > 0) {
				if (classMap.containsKey(woName)) {
					value = classMap.get(woName);
					log.info("Using time from classmap");
				} else {
					log.info("Classmap not contains wo name. Using default time");
				}
			} else {
				log.info("Classmap undefined. Using default time");
			}
		} else {
			log.info("WO name is null. Using default time");
		}
		
		value = DateUtil.minutesToDateTime(value);
		
		return value;
	}
}
