package com.example.activitidemo.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.activiti.api.process.runtime.ProcessRuntime;
import org.activiti.bpmn.converter.BpmnXMLConverter;
import org.activiti.bpmn.model.*;
import org.activiti.bpmn.model.Process;
import org.activiti.bpmn.model.SequenceFlow;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.io.InputStreamSource;
import org.activiti.engine.repository.Deployment;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class TestService {
    private final ProcessEngine processEngine;

    private final RepositoryService repositoryService;

    public String activitiTestLoader() {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("processes/diagram.bpmn");
        BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(inputStream), false, false, "UTF-8");

        List<ProcessDefinition> processDefinitionQuery =
                processEngine
                        .getRepositoryService()
                        .createProcessDefinitionQuery()
                        .processDefinitionKey("nameProcess")
                        .orderByDeploymentId()
                        .desc()
                        .list();

        var process = bpmnModel.getProcesses().get(0);

        // Lấy tất cả các FlowElement trong Process
//        var flowElements = bpmnModel.getMainProcess().getFlowElements();
//
//        // Lấy tất cả các User Task
//        var userTasks = flowElements.stream()
//                .filter(element -> element instanceof ManualTask)
//                .map(ManualTask.class::cast)
//                .collect(Collectors.toList());

        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bpmnBytes = converter.convertToXML(bpmnModel);
        return new String(bpmnBytes);
    }

    public void uploadBPMN() {
        try {
            InputStream inputStream = getClass().getClassLoader().getResourceAsStream("processes/qr-code.bpmn");
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(inputStream), false, false, "UTF-8");

            RepositoryService repositoryService = processEngine.getRepositoryService();
            Deployment deployment = repositoryService.createDeployment()
                    .addBpmnModel("qr-code.bpmn", bpmnModel)
                    .deploy();

//            repositoryService.createDeployment()
//                    .addClasspathResource("processes/diagram01.bpmn")
//                    .deploy();

            log.info("Import bmpn Success!");
        } catch (Exception ex) {
            log.error(ex.toString());
        }
    }

    public String getProcessByKey(String processKey) {
//        RepositoryService repositoryService = processEngine.getRepositoryService();

        List<ProcessDefinition> processDefinitionQuery = null;
        try {
            processDefinitionQuery =
                    repositoryService.createProcessDefinitionQuery()
                            .processDefinitionKey(processKey)
                            .orderByDeploymentId()
                            .desc()
                            .list();

            var processDefinition = processDefinitionQuery.get(0).getId();
            var bpmnModel = repositoryService.getBpmnModel(processDefinition);

            List<Process> processes = bpmnModel.getProcesses();

            var flowElements = processes.get(0).getFlowElements();

            // Lấy tất cả các FlowElement trong Process
//            var flowElements = bpmnModel.getMainProcess().getFlowElements();

            // Lấy tất cả các User Task
            var userTasks = flowElements.stream()
                    .filter(element -> element instanceof ManualTask)
                    .map(ManualTask.class::cast)
                    .collect(Collectors.toList());

            TaskService taskService = processEngine.getTaskService();
            ManualTask task0 = userTasks.get(0);
            task0.setName("CRM");

//            taskService.complete(userTasks.get(0).getId());

            ManualTask task1 = userTasks.get(1);
            task1.setName("MO");

//            var process = bpmnModel.getMainProcess();

//            process.removeFlowElement(userTasks.get(2).getId());

            BpmnXMLConverter converter = new BpmnXMLConverter();
            byte[] bpmnBytes = converter.convertToXML(bpmnModel);

            return new String(bpmnBytes);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public String deployBPMN(String bpmnXml, String newKey) {
//        RepositoryService repositoryService = processEngine.getRepositoryService();

        InputStream inputStream = null;
        try {
            inputStream = new ByteArrayInputStream(bpmnXml.getBytes());
            BpmnModel bpmnModel = new BpmnXMLConverter().convertToBpmnModel(new InputStreamSource(inputStream), false, false, "UTF-8");

            var process = bpmnModel.getProcesses().get(0);
            process.setExecutable(true);
            // Cập nhật processDefinitionKey
            if (!"".equals(newKey)) {
                process.setId(newKey);
            }

            Deployment deployment = repositoryService.createDeployment()
                    .addBpmnModel(newKey + ".bpmn", bpmnModel)
                    .deploy();

        } catch (Exception e) {
            log.error(e.toString());
            throw new RuntimeException(e);
        }

        return "Deploy success";
    }

    public String createNewBPMN() {
        BpmnModel model = new BpmnModel();
        Process process = new Process();
        process.setId("myProcess");
        process.setName("My Process");
        model.addProcess(process);

        // Khai báo các Task
        StartEvent startEvent = new StartEvent();
        startEvent.setId("startEvent1");
        process.addFlowElement(startEvent);

        UserTask task1 = new UserTask();
        task1.setId("task1");
        task1.setName("Enter information");
        process.addFlowElement(task1);

        ServiceTask task2 = new ServiceTask();
        task2.setId("task2");
        task2.setName("Call API");
        process.addFlowElement(task2);

        EndEvent endEvent = new EndEvent();
        endEvent.setId("endEvent1");
        process.addFlowElement(endEvent);

        // Thiết lập các liên kết giữa các Task
        process.addFlowElement(new SequenceFlow(startEvent.getId(), task1.getId()));
        process.addFlowElement(new SequenceFlow(task1.getId(), task2.getId()));
        process.addFlowElement(new SequenceFlow(task2.getId(), endEvent.getId()));

        BpmnXMLConverter converter = new BpmnXMLConverter();
        byte[] bpmnBytes = converter.convertToXML(model);
        String bpmnXml = new String(bpmnBytes);

//        RepositoryService repositoryService = processEngine.getRepositoryService();
//        Deployment deployment = repositoryService.createDeployment()
//                .addBpmnModel("create_processModel.bpmn", model)
//                .deploy();

        return bpmnXml;
    }
}
