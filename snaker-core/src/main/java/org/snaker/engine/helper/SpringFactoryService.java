package org.snaker.engine.helper;


import org.apache.log4j.Logger;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;


/**
 * 初始化注入webApplicationContext，方便获取bean
 * @author novelbio
 *
 */
public class SpringFactoryService {
	
	private final static Logger logger = Logger.getLogger(SpringFactoryService.class);

	private static ApplicationContext context;
	static protected BeanFactory factory;
	static  {
		logger.info("before context init");
		context = new ClassPathXmlApplicationContext("spring-service.xml");
		factory = (BeanFactory) context;
		logger.info("after context init");
		//TODO 这里可以动态绑定数据库
//		Mongo mongo = factory.getBean(Mongo.class);
//		mongo.close();
//		List<ServerAddress> seeds = new ArrayList<>();
//		try { seeds.add(new ServerAddress("192.168.0.104", 27017)); } catch (UnknownHostException e) { }
//		mongo.set(seeds);
	}
	
	/**
	 * 根据id拿到spring容器中的bean
	 * @param id
	 * @return
	 */
	public static Object getBean(String id) {
		return context.getBean(id);
	}

	
	/**
	 * 根据class拿到spring容器中的bean
	 * @param <T>
	 * @param id
	 * @return
	 */
	public static <T> T getBean(Class<T> requiredType) {
//		logger.info("requiredType =" + requiredType.getName());
		return context.getBean(requiredType);
	}
	
}
