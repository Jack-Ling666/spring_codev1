package com.jack.demo.service.impl;


import com.jack.demo.service.IDemoService;
import com.jack.spring.annotation.JackService;

@JackService
public class DemoService implements IDemoService {

	public String get(String name) {
		return "My name is " + name;
	}

}
