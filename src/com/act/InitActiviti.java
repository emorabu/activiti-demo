package com.act;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineConfiguration;
import org.apache.log4j.Logger;
import org.junit.Test;

/**
 * 初始化
 * @author emora
 * 建表方式二选一
 */
public class InitActiviti {
	private Logger logger=Logger.getLogger(this.getClass());
	
	@Test
	public void initTables() {
		//创建Activiti配置对象的实例
		ProcessEngineConfiguration configuration=ProcessEngineConfiguration.createStandaloneProcessEngineConfiguration();
		
		//配置数据库连接信息
		configuration.setJdbcUrl("jdbc:mysql://localhost:3306/activiti_522?useUnicode=true&characterEncoding=UTF-8");
		configuration.setJdbcDriver("com.mysql.jdbc.Driver");
		configuration.setJdbcUsername("jeecg");
		configuration.setJdbcPassword("jeecg");
		
		//设置数据库建表策略
		/**
		 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE:如果不存在就建表,存在就直接使用
		 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_FALSE:如果不存在就抛异常
		 * ProcessEngineConfiguration.DB_SCHEMA_UPDATE_CREATE_DROP:每次先删除表,再建新表
		 */
		configuration.setDatabaseSchemaUpdate(ProcessEngineConfiguration.DB_SCHEMA_UPDATE_TRUE);
		
		//使用配置对象创建ProcessEngine实例
		ProcessEngine processEngine=configuration.buildProcessEngine();
		logger.info(processEngine);
		
	}
	
	@Test
	public void initTablesByConfig() {
		ProcessEngine processEngine=ProcessEngineConfiguration.createProcessEngineConfigurationFromResource("activiti.cfg.xml")
									.buildProcessEngine();
		logger.debug(processEngine);
	}
}
