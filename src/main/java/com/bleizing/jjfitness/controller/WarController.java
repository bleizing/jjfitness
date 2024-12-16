package com.bleizing.jjfitness.controller;

import java.text.ParseException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bleizing.jjfitness.JjfitnessApplication;
import com.bleizing.jjfitness.dto.request.WarJjfRequest;
import com.bleizing.jjfitness.service.JjfService;

@RestController
@RequestMapping("/jjf")
public class WarController {
	@Autowired
	private JjfService jjfService;
	
	@PostMapping("/war")
	public void war(WarJjfRequest request) {
		jjfService.warJjf(request);
	}
	
	public static void main(String[] args) throws ParseException {
		SpringApplication.run(JjfitnessApplication.class, args);

		System.out.println("Hello World");
		
		JjfService jjfService = new JjfService();
		
		jjfService.warJjf(WarJjfRequest.builder()
				.username("dimasz_97@gmail.com")
				.password("1693484551")
				.woName("spinning")
				.build());
	}
}
