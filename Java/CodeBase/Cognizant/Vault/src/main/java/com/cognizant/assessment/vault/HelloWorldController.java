package com.cognizant.assessment.vault;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
@RestController
public class HelloWorldController
{
    @Autowired
    private Environment environment;
    @RequestMapping("/")
    public String hello()
    {
        return "Hello javaTpoint" + environment.getProperty("test.dbuser");
    }
}