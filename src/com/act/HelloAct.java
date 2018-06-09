package com.act;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.zip.ZipInputStream;

import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.Test;

public class HelloAct {
	private Logger logger=Logger.getLogger(this.getClass());
	public static ProcessEngine processEngine=ProcessEngines.getDefaultProcessEngine();//默认加载 classpath:activiti.cfg.xml
	
	/**
	 * 部署流程定义
	 * 会保存在 act_re_deployment, act_re_procdef,act_ge_bytearray, act_ge_property  表中
	 * act_re_procdef表记录了bpmn文件的基本信息,VERSION_为版本信息,通过DEPLOYMENT_ID_关联act_re_deployment表
	 * act_re_deployment表记录了部署信息
	 * act_ge_bytearray 存储bpmn和png的二进制文件,每部署一次就针对bpmn和png文件各增加一条记录
	 * act_ge_property 主键生成策略
	 */
	@Test
	public void deploy() {
		Deployment deployment=processEngine.getRepositoryService()//获取流程定义,部署相关的Service
							.createDeployment()//创建部署对象
							.name("hello1")//部署名称
							.addClasspathResource("Hello.bpmn")//从classpath的资源中加载，一次只能加载一个文件
							.addClasspathResource("Hello.png")//从classpath的资源中加载，一次只能加载一个文件
							.deploy();
		logger.info("部署的id: "+deployment.getId());
		logger.info("部署的名称: "+deployment.getName());
	}
	
	@Test
	public void deployByZip() {
		InputStream InputStream=this.getClass().getResourceAsStream("/Hello2.zip");
		ZipInputStream zipInputStream=new ZipInputStream(InputStream);
		
		Deployment deployment=processEngine.getRepositoryService()//获取流程定义,部署相关的Service
				.createDeployment()//创建部署对象
				.name("hello2")//部署名称
				.addZipInputStream(zipInputStream)
				.deploy();
		logger.info("部署的id: "+deployment.getId());
		logger.info("部署的名称: "+deployment.getName());
	}
	
	/**
	 * 启动流程实例
	 * 会保存在 act_ru_execution 和 act_ru_task 表中,
	 * act_ru_execution的ID_即为流程实例id,PROC_DEF_ID即为流程实例的定义id，也即act_re_procdef的ID_,
	 * act_ru_task记录流程任务的执行信息
	 */
	@Test
	public void startProcess() {
		ProcessInstance processInstance=processEngine.getRuntimeService() //正在执行的流程实例及对象相关的Service
										.startProcessInstanceByKey("hello");//使用流程定义的key启动流程实例,key对应bpmn文件的process id,即act_re_procdef表的KEY_,只会启动key对应的最新的流程
		logger.info("流程实例id: "+processInstance.getId());
		logger.info("流程定义id: "+processInstance.getProcessDefinitionId());
	}
	
	/**
	 * 任务查询
	 */
	@Test
	public void queryTask() {
		//查询正在执行的任务中指派人为"张三"的任务
		List<Task> tasks=processEngine.getTaskService() //正在执行的任务相关的Service
							.createTaskQuery() //创建任务查询对象
							.taskAssignee("张三")
							.list();
		for (Task task : tasks) {
			logger.info("任务id: "+task.getId());
			logger.info("任务名称: "+task.getName());
			logger.info("任务创建时间: "+task.getCreateTime());
			logger.info("任务办理人: "+task.getAssignee());
			logger.info("流程实例id: "+task.getProcessInstanceId());
			logger.info("执行对象id: "+task.getExecutionId());
			logger.info("流程定义id: "+task.getProcessDefinitionId());
		}
	}
	
	/**
	 * 完成任务
	 */
	@Test
	public void completeTask() {
		String taskId="25002";
		processEngine.getTaskService()
					.complete(taskId);
		logger.info("任务"+taskId+"已完成!");
	}
	
	/**
	 * 查询流程定义
	 */
	@Test
	public void queryProcessDefinition() {
		List<ProcessDefinition> processDefinitions=processEngine.getRepositoryService()
													.createProcessDefinitionQuery()
													//查询条件
								//					.deploymentId("")//使用部署id查询
								//					.processDefinitionId("")//使用流程定义id查询
								//					.processDefinitionKey("")//流程定义的key
													//排序
								//					.orderByDeploymentId()
								//					.orderByProcessDefinitionId()
								//					.orderByProcessDefinitionKey()
													//结果集
													.list();//List<ProcessDefinition>
								//					.singleResult();//ProcessDefinition
								//					.listPage(firstResult, maxResults)//分页查询
		for (ProcessDefinition processDefinition : processDefinitions) {
			logger.info("流程定义id: "+processDefinition.getId());//流程定义的key:版本:随机值
			logger.info("流程定义名称: "+processDefinition.getName());//bpmn文件的name值
			logger.info("流程定义的key: "+processDefinition.getKey());//bpmn文件的id值
			logger.info("流程定义的版本: "+processDefinition.getVersion());//流程定义的key值相同时,version+=1
			logger.info("资源文件bpmn文件名: "+processDefinition.getResourceName());
			logger.info("资源文件png文件名: "+processDefinition.getDiagramResourceName());
			logger.info("部署对象id: "+processDefinition.getDeploymentId());
			logger.info("\n");
		}			
	}
	
	/**
	 * 删除流程定义
	 */
	@Test
	public void deleteProcessDefinition() {
		String deploymentId="2501";
		processEngine.getRepositoryService()
//					.deleteDeployment(deploymentId);//使用部署id删除,非级联删除,只能删除未启动的流程
					.deleteDeployment(deploymentId, true);//级联删除,无流程是否启动都可以删除
		logger.info("部署id"+deploymentId+"删除成功!");
	}
	
	/**
	 * 查看流程图
	 */
	@Test
	public void viewPic() {
		String deploymentId="35001";
		RepositoryService repositoryService=processEngine.getRepositoryService();
		List<String> resourceNames=repositoryService.getDeploymentResourceNames(deploymentId);
		String resourceName = null;
		for (String name : resourceNames) {
			if(name.endsWith(".png")) {
				resourceName=name;
				break;
			}
		}
		InputStream in=repositoryService.getResourceAsStream(deploymentId, resourceName);
		File file=new File("f:/temp/4",resourceName);
		try {
			FileUtils.copyInputStreamToFile(in, file);
		} catch (IOException e) {
			logger.error(e.getMessage());
		}
		logger.info("图片导出成功!");
	}
}
