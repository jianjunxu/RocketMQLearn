package com.jayden.jxlx.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

public class Log4jConfigListener implements ServletContextListener {

	public void contextInitialized(ServletContextEvent sce) {
		try {
			LogbackWebConfigurer.initLogging(sce.getServletContext());
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		LogbackWebConfigurer.shutdownLogging(sce.getServletContext());
	}

}
