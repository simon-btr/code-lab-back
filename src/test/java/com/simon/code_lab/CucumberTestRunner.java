package com.simon.code_lab;

import org.junit.runner.RunWith;

import io.cucumber.junit.Cucumber;
import io.cucumber.junit.CucumberOptions;

@RunWith(Cucumber.class)
@CucumberOptions(
        features = "src/test/resources/features",
        glue = {"com.simon.code_lab.steps", "com.simon.code_lab.config"},
        plugin = {"pretty"}
)
public class CucumberTestRunner {
}
