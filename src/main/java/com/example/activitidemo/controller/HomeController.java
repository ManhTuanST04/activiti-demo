package com.example.activitidemo.controller;

import com.example.activitidemo.model.DeployBPMN;
import com.example.activitidemo.service.TestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin(origins = "http://localhost:4200")
public class HomeController {

    @Autowired
    private TestService testService;

    @GetMapping("/api/get")
    public String Home () {
        var xmlRes = testService.activitiTestLoader();

        return xmlRes;
    }

    @PostMapping("/api/post")
    public String upload () {
        testService.uploadBPMN();

        return "Success";
    }

    @GetMapping("/api/get-by-key")
    public String getByKey (@RequestParam String processKey) {
        String xmlRes = testService.getProcessByKey(processKey);

        return xmlRes;
    }

    @PostMapping("api/create")
    public String createNew () {
        var res = testService.createNewBPMN();

        return res;
    }

    @PostMapping("api/deploy")
    public String deployBPMNFromXML (@RequestBody DeployBPMN requestDto) {
        var res = testService.deployBPMN(requestDto.getBpmnXml(), requestDto.getProcessKey());

        return requestDto.getProcessKey();
    }

}
