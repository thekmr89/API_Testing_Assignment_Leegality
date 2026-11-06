package com.automation.listeners;

import io.qameta.allure.Allure;
import org.testng.ITestListener;
import org.testng.ITestResult;

public class FailureListener implements ITestListener {
    @Override
    public void onTestFailure(ITestResult result) {
        Allure.addAttachment("Failure context", "text/plain",
                "Test: " + result.getName() + System.lineSeparator()
                        + "Description: " + result.getMethod().getDescription() + System.lineSeparator()
                        + "Failure: " + (result.getThrowable() == null ? "unknown" : result.getThrowable()),
                ".txt");
    }
}
