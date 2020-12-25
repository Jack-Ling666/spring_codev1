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

public class JackDispatcherServlet extends HttpServlet {

	Properties properties = new Properties();

	// 保存beanName
	List<String> classNames = new ArrayList<String>();

	// 传说中的容器
	Map<String, Object> beanMap = new ConcurrentHashMap<String, Object>();

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		super.doGet(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("=======调用dopost======");
	}

	@Override
	public void init(ServletConfig config) throws ServletException {
		// todo :1、定位：找到配置文件
		// config.getInitParameter("contextConfigLocation")  获取web.xml文件的contextConfigLocation属性
		doLocation(config.getInitParameter("contextConfigLocation"));

		//todo：2、加载：把所有bean都加载到classNames容器中
		doLoad(properties.getProperty("scanPackage"));

		try {
			//todo：3、注册：将classNames容器里面的bean拿出来放到beanMap容器中
			doRegister();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		}

		try {
			//todo：依赖注入 将bean中带有@JackAutowired注解的属性进行赋值
			doAutowired();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}

		// 测试是否依赖注入成功
		DemoAction demoAction = (DemoAction) beanMap.get("demoAction");
		demoAction.query(null,null, "jack");
	}


	private void doLocation(String contextConfigLocation) {
		/**
		 * 1、this.getClass()  获取该类的class对象
		 * 2、this.getClass().getClassLoader()   取得改类的类加载器对象
		 * 3、this.getClass().getClassLoader().getResourceAsStream   从类加载路径取得文件的输入流
		 */
		InputStream resourceAsStream = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation.replace("classpath:", ""));

		try {
			/**
			 * todo 从字节输入流中读取键值对
			 * 参数中使用了字节输入流，通过流对象，可以关联到某文件上，
			 * 这样就能够加载文本中的数据了。文本中的数据，必须是键值
			 * 对形式，可以使用空格、等号、冒号等符号分隔。
 			 */
			properties.load(resourceAsStream);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (null != resourceAsStream) {
				try {
					resourceAsStream.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void doLoad(String scanPackage) {
		/**
		 * 2.Class.getResource(String path)
		 * path不以'/'开头时，默认是从此类所在的包下取资源；
		 * path以'/'开头时，则是从项目的ClassPath根下获取资源。在这里'/'表示ClassPath的根目录
		 */
		URL url = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/"));
		File dirs = new File(url.getFile());

		for (File file : dirs.listFiles()) {
			if (file.isDirectory()) {
				// 递归遍历出所有文件
				doLoad(scanPackage + "." + file.getName());
			} else {
				classNames.add(scanPackage + "." + file.getName().replaceAll(".class", ""));
			}
		}
	}


	private void doRegister() throws IllegalAccessException, InstantiationException {
		if (classNames.isEmpty()) {
			return;
		}
		for (String className : classNames) {
			Class<?> clazz = null;
			try {
				// 根据类名来生成类对象
				clazz = Class.forName(className);
			} catch (Exception e) {

			}
			if (clazz.isAnnotationPresent(JackController.class)) {
				// @Controller("xxx")默认没有自定义名，所以不用下面这一步
				// clazz.getAnnotation(JackController.class)
				String beanName = lowerFirstCase(clazz.getSimpleName());
				beanMap.put(beanName, clazz.newInstance());
			} else if (clazz.isAnnotationPresent(JackService.class)) {
				/**
				 * clazz.getAnnotation(JackService.class)的值就是下面xxx的值
				 * @JackService("xxx")
				 */
				String value = clazz.getAnnotation(JackService.class).value().trim();
				if ("".equals(value)) {
					// 若为空，则取类名首字母小写
					value = lowerFirstCase(clazz.getSimpleName());
				}
				Object object = clazz.newInstance();
				beanMap.put(value, object);

				// todo：这一步容易忘记遍历该类对象的接口，也将其注册到容器中去
				Class<?>[] interfaces = clazz.getInterfaces();
				for (Class inter : interfaces) {
					beanMap.put(inter.getName(), object);
				}
			} else {
				continue;
			}
		}

	}

	// 首字母小写
	private String lowerFirstCase(String simpleName) {
		if (simpleName != null) {
			char[] chars = simpleName.toCharArray();
			chars[0] += 32;
			return String.valueOf(chars);
		}
		return null;
	}


	private void doAutowired() throws IllegalAccessException {
		if (beanMap.isEmpty()) {
			return ;
		}
		for (Map.Entry<String, Object> entry : beanMap.entrySet()) {
			// 获取类对象的字段
			Field[] fields = entry.getValue().getClass().getDeclaredFields();
			for (Field field : fields) {
				// 对带有@JacjAutowired注解的字段进行注入
				if (!field.isAnnotationPresent(JackAutowired.class)) {
					continue;
				}
				else {
					String beanName = field.getAnnotation(JackAutowired.class).value();
					// 如果为空则直接获取属性类型名
					if ("".equals(beanName)) {
						beanName = field.getType().getName();
					}
					//强制访问私有属性
					field.setAccessible(true);
					// todo：field.set(Object obj, Object value) 向obj对象所指定的filed属性设置新的值value
					field.set(entry.getValue(), beanMap.get(beanName));
				}
			}
		}
	}


}
