package com.jack.demo.action;

import com.jack.spring.annotation.JackAutowired;
import com.jack.spring.annotation.JackController;
import com.jack.spring.annotation.JackRequestMapping;
import com.jack.demo.service.IDemoService;

@JackController
public class MyAction {

		@JackAutowired
		IDemoService demoService;
	
		@JackRequestMapping("/index.html")
		public void query(){

		}
	
}
