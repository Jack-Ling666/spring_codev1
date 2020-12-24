package com.jack.spring.servlet;

import com.jack.demo.action.DemoAction;
import com.jack.spring.annotation.JackAutowired;
import com.jack.spring.annotation.JackController;
import com.jack.spring.annotation.JackService;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

public class JackDispatcherServletBak extends HttpServlet {
	
	private Properties properties = new Properties();

	private List<String> classNames = new ArrayList<String>();

	private Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();
	
	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("dopost调用");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// 定位
		doLocation(config.getInitParameter("contextConfigLocation"));

		// 加载 Properties.getProperty()来读取配置文件里面的属性值
		doLoad(properties.getProperty("scanPackage"));

		// 注册
		try {
			doRegister();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		//依赖注入
		doAutowired();

		DemoAction demoAction = (DemoAction)beanMap.get("demoAction");
		demoAction.query(null, null, "linhongjie");


		//        DemoAction action = (DemoAction)beanMap.get("demoAction");
//        action.query(null,null,"Tom");
	}

	private void doAutowired() {
		if (beanMap.isEmpty()) {
			return ;
		}
		for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				if (!field.isAnnotationPresent(JackAutowired.class)) {
					continue ;
				}
				String beanName = field.getAnnotation(JackAutowired.class).value().trim();
				if ("".equals(beanName)) {
					beanName = field.getType().getName();
				}
				field.setAccessible(true);
				try {
					field.set(entry.getValue(), beanMap.get(beanName));
				} catch (IllegalAccessException e) {
					e.printStackTrace();
				}
			}
		}

	}

	private void doRegister() throws IllegalAccessException, InstantiationException {
		if (classNames.isEmpty()) {
			return ;
		}
		for (String className : classNames) {
			Class<?> clazz = null;
			try {
				clazz = Class.forName(className);
			} catch (ClassNotFoundException e) {
				e.printStackTrace();
			}
			if (clazz.isAnnotationPresent(JackController.class)) {
				className = lowerFirseCase(clazz.getSimpleName());
				beanMap.put(className, clazz.newInstance());
			} else if (clazz.isAnnotationPresent(JackService.class)) {
				JackService jackService = clazz.getAnnotation(JackService.class);
				String beanName = jackService.value();
				if ("".equals(beanName.trim())) {
					beanName = lowerFirseCase(clazz.getSimpleName());
				}
				Object instance = clazz.newInstance();
				beanMap.put(beanName, instance);
				Class<?>[] interfaces = clazz.getInterfaces();
				for (Class inter : interfaces) {
					beanMap.put(inter.getName(), instance);
				}
			} else {
				continue;
			}
		}

	}

	private String lowerFirseCase(String className) {
		if (className != null) {
			char[] chars = className.toCharArray();
			chars[0] += 32;
			return String.valueOf(chars);
		}
		return null;
	}

	private void doLoad(String scanPackage) {
		URL url = this.getClass().getClassLoader().getResource("/" + scanPackage.replaceAll("\\.", "/"));
		File classDir = new File(url.getFile());
		for (File file : classDir.listFiles()) {
			if (file.isDirectory()) {
				doLoad(scanPackage + "." + file.getName());
			}
			else {
				classNames.add(scanPackage + "." + file.getName().replaceAll(".class",""));
			}
		}
	}

	private void doLocation(String location) {
		// todo 加载资源
		InputStream is = this.getClass().getClassLoader().getResourceAsStream(location.replace("classpath:",""));
		try {
			properties.load(is);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != is) {
				try {
					is.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}


}
