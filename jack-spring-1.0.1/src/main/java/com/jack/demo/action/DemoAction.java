package com.jack.demo.action;

import com.jack.demo.service.IDemoService;
import com.jack.spring.annotation.JackAutowired;
import com.jack.spring.annotation.JackController;
import com.jack.spring.annotation.JackRequestMapping;
import com.jack.spring.annotation.JackRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@JackController
@JackRequestMapping("/demo")
public class DemoAction {
	
	@JackAutowired
	private IDemoService demoService;
	
	@JackRequestMapping("/query.json")
	public void query(HttpServletRequest req,HttpServletResponse resp,
		   @JackRequestParam("name") String name){
		String result = demoService.get(name);
		System.out.println(result);
	}
	
	@JackRequestMapping("/edit.json")
	public void edit(HttpServletRequest req,HttpServletResponse resp,Integer id){

	}
	
}
