package com.scrat.framework.config.subscribe;


import com.scrat.framework.config.common.Check;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * 描述：注解处理器
 * 作者 ：kangzz
 * 日期 ：2016-10-21 17:39:57
 */
public class AsuraSubAnnotationProcessor {

	private AsuraSubAnnotationProcessor() {
		loadMBeans(true);
	}

	public static AsuraSubAnnotationProcessor getInstance() {
		return new AsuraSubAnnotationProcessor();
	}

	/**
	 * 描述：根据注解加载MBean
	 * 作者 ：kangzz
	 * 日期 ：2016-10-21 17:40:12
	 */
	protected void loadMBeans(boolean isServer) {
		File file;
		JarFile jarFile;
		Enumeration<JarEntry> s;
		JarEntry entry;
		Class<?> clazz;
		String className = "";
		AsuraSub asuraSub;
		String classPath = System.getProperty("java.class.path");
		for (String path : Arrays.asList(classPath.split(System.getProperty("path.separator")))) {
			file = new File(path);
			if (file.isFile()) {
				if ((file.getName().startsWith("com-asura-") || file.getName().startsWith("com.asura-") || file
						.getName().startsWith("sms-biz"))) {
					try {
						jarFile = new JarFile(file);
						s = jarFile.entries();
						while (s.hasMoreElements()) {
							entry = s.nextElement();
							className = entry.getName();
							if (!entry.isDirectory() && className.endsWith(".class")) {
								clazz = Class.forName(className.substring(0, className.length() - 6).replace("/", "."),
										false, Thread.currentThread().getContextClassLoader());
								asuraSub = clazz.getAnnotation(AsuraSub.class);
								if (asuraSub != null) {
									Object[] enumObjs = clazz.getEnumConstants();
									if(!Check.NuNObject(enumObjs)){
										for (Object o : enumObjs) {
											Method getType = o.getClass().getMethod("getType");
											Object typeObj = getType.invoke(o);
											Method getCode = o.getClass().getMethod("getCode");
											Object codeObj = getCode.invoke(o);
											if (typeObj != null && codeObj != null) {
												ConfigSubscriber.getInstance().registConfig(typeObj.toString(), codeObj.toString());
												System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ")
														.format(new Date())
														+ "Registed Config : type="
														+ typeObj.toString()
														+ " code=" + codeObj.toString());
											}
										}
									}
									/*for(Method dMethod : clazz.getDeclaredMethods()){
										if(dMethod.isAnnotationPresent(AsuraSubField.class)){
											AsuraSubField asuraSubField = dMethod.getAnnotation(AsuraSubField.class);
											ConfigSubscriber.getInstance().registConfig(asuraSubField.key());
										}
									}*/
								}
							}
						}
					} catch (IOException e) {
						e.printStackTrace();
						System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date())
								+ " 注册系统参数异常 " + file.getName() + " is not a jar file!" + "" + e.getMessage());
						System.exit(1);
					} catch (ClassNotFoundException e) {
						e.printStackTrace();
						System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date())
								+ " 注册系统参数异常" + className + "" + e.getMessage());
						System.exit(1);
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date())
								+ " 注册系统参数异常" + e.getMessage());
						System.exit(1);
					}
				}
			} else {
				if (file.getPath().contains("classes")) {
					readDir(file.getPath(), "", isServer);
				}
			}
		}
	}

	/**
	 * 描述：处理未打包在jar中的class文件
	 * 作者 ：kangzz
	 * 日期 ：2016-10-21 17:40:29
	 */
	private void readDir(String path, String packageName, boolean isServer) {
		String className = "";
		Class<?> clazz;
		AsuraSub asuraSub;
		File[] files = new File(path).listFiles();
		if (null == files)
			return;
		for (File f : Arrays.asList(files)) {
			if (f.isDirectory()) {
				if ("".equals(packageName)) {
					readDir(f.getPath(), f.getName(), isServer);
				} else {
					readDir(f.getPath(), packageName + "." + f.getName(), isServer);
				}
			} else {
				if (f.getName().endsWith(".class")) {
					try {
						className = f.getName();
						clazz = Class.forName(packageName + "."
								+ className.substring(0, f.getName().length() - 6).replace("/", "."), false, Thread
								.currentThread().getContextClassLoader());
						asuraSub = clazz.getAnnotation(AsuraSub.class);
						if (asuraSub != null) {
							Object[] enumObjs = clazz.getEnumConstants();
							if(!Check.NuNObject(enumObjs)){
								for (Object o : enumObjs) {
									Method getType = o.getClass().getMethod("getType");
									Object typeObj = getType.invoke(o);
									Method getCode = o.getClass().getMethod("getCode");
									Object codeObj = getCode.invoke(o);
									if (typeObj != null && codeObj != null) {
										ConfigSubscriber.getInstance().registConfig(typeObj.toString(), codeObj.toString());
										System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ")
												.format(new Date())
												+ "Registed Config : type="
												+ typeObj.toString()
												+ " code=" + codeObj.toString());
									}
								}
							}
						/*	for(Method dMethod : clazz.getDeclaredMethods()){
								if(dMethod.isAnnotationPresent(AsuraSubField.class)){
									AsuraSubField asuraSubField = dMethod.getAnnotation(AsuraSubField.class);
									ConfigSubscriber.getInstance().registConfig(asuraSubField.key());
								}
							}*/
						}
					} catch (Exception e) {
						e.printStackTrace();
						System.out.println(new SimpleDateFormat("[yyyy-MM-dd HH:mm:ss] ").format(new Date())
								+ " 注册系统参数异常" + e.getMessage());
						System.exit(1);
					}
				}
			}
		}
	}
}
