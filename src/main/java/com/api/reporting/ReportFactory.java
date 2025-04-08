package com.api.reporting;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;


import com.api.payloads.Results;
import com.dbUtils.DatabaseManager;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.response.chat.ChatPostMessageResponse;
import io.restassured.http.ContentType;
import org.apache.commons.mail.DefaultAuthenticator;
import org.apache.commons.mail.EmailAttachment;
import org.apache.commons.mail.EmailException;
import org.apache.commons.mail.HtmlEmail;


import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.Status;
import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
import com.google.common.io.Files;
import com.jayway.jsonpath.JsonPath;

import io.cucumber.java.Scenario;
import io.cucumber.plugin.event.PickleStepTestStep;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;
import com.api.logging.*;
import com.api.utilities.ExcelUtil;
import org.testng.Assert;

public class ReportFactory {
    public static boolean reporting = true;
    private static ExtentHtmlReporter reporter;
    private static ExtentReports report;
    private static ThreadLocal<ExtentTest> tests = new ThreadLocal<ExtentTest>();
    private static ExtentTest node;
    private static String reportFolder;
    private static String reportFolderName;
    private static String reportFilePath;
    private static List<String> snapshotsList = new ArrayList<String>();
    private static List<String> messageList = new ArrayList<String>();
    private static SimpleDateFormat sdt = new SimpleDateFormat("MMddyyy-hhmmss");
    //private static int testStatus=0;

    private static String WebHookURL;
    private static String ChannelID;
    private static String Environment;
    private static String EnvironmentURL;
    public static String ReportName;
    public static String applicationName;
    private static String tagNumber;
    private static String ReportLink;
    public static Integer totalTests;
    public static LinkedList<Integer> totalTestsCount = new LinkedList<>();
    public static Integer totalPassTests;
    public static LinkedList<Integer> totalPassTestsCount = new LinkedList<>();
    public static List<String> PassTests;
    public static Integer totalFailTests;
    public static LinkedList<Integer> totalFailTestsCount = new LinkedList<>();
    public static long endTime;

    public static long startTime;
    public static double totalExecutionTimeInSeconds;
    private static HashMap<String, Integer> FailTests;

    private static ThreadLocal<String> testName = new ThreadLocal<String>();
    private static ThreadLocal<Integer> testStatus = new ThreadLocal<Integer>();
    private static ThreadLocal<Integer> testPassStatus = new ThreadLocal<Integer>();
    //private static String testResult="PASS";
    public static ThreadLocal<String> testResult = new ThreadLocal<>();

    //	private static List<PickleStepTestStep> TestCaseSteps;
//	private static Scenario CurrentScenario;
//	public static int TotalTestSteps;	
//	public static int StepNumber;
    public static ExceptionHandler handler;
    //	public static String errorDetails;
    public static HashMap<String, HashMap<String, String>> TestCases;

    private static ThreadLocal<List<PickleStepTestStep>> TestCaseSteps = new ThreadLocal<>();
    private static ThreadLocal<Scenario> CurrentScenario = new ThreadLocal<>();
    public static ThreadLocal<Integer> TotalTestSteps = new ThreadLocal<>();
    public static ThreadLocal<Integer> StepNumber = new ThreadLocal<>();
    public static ThreadLocal<String> errorDetails = new ThreadLocal<>();
    public static ExtentTest tcTestNode;

    public static ExtentHtmlReporter tcReporter;
    public static ExtentReports tcReport;
    public static ExtentTest tcTest;
    public static String pod;
    public static long suiteStartTime;
    public static long suiteEndTime;
    public static long totalSuiteExecutionTimeInSeconds;


    public static void StartReport(String reportname) {
        suiteStartTime = System.currentTimeMillis();
        reportFolderName = sdt.format(new Date()).toString();
        reportFolder = System.getProperty("user.dir") + "/Reports/" + reportFolderName;
        File folder = new File(reportFolder);
        folder.mkdir();
        reporter = new ExtentHtmlReporter(reportFolder + "/" + reportname + ".html");
        report = new ExtentReports();
        report.attachReporter(reporter);
        reportFilePath = reportFolder + "/" + reportname + ".html";
        totalTests = 0;
        totalPassTests = 0;
        totalFailTests = 0;
        PassTests = new ArrayList<String>();
        FailTests = new HashMap<String, Integer>();

        //Test Case test Node
        tcTestNode = report.createTest("TEST CASES");

        //Exception Handling
        handler = new ExceptionHandler();
        Thread.setDefaultUncaughtExceptionHandler(handler);

        TestCases = new HashMap<String, HashMap<String, String>>();

        //Test Case Document
        tcReporter = new ExtentHtmlReporter("target" + "/" + "TestCases" + ".html");
        tcReport = new ExtentReports();
        tcReport.attachReporter(tcReporter);
        tcTest = tcReport.createTest("TEST CASES");
    }

    public static void setCucumberReports() {
        System.setProperty("cucumber.options", "--features src/test/resources/IPO_Features --glue stepdefs");
    }

    public static void StartTest(String testname) {
        startTime = System.currentTimeMillis();
        ExtentTest test = report.createTest(testname);
        tests.set(test);
        testStatus.set(0);
        testPassStatus.set(0);
        LogFactory.LogInfo("=========Starting Test " + testname + "==================");
        //LogFactory.LogInfo("Starting "+testname);

        testName.set(testname);
        totalTests++;
        UpdateTestStatusCount("Total");
    }

    public static void AssignCategories(Collection<String> categories) {
        for (String category : categories) {
            tests.get().assignCategory(category);
        }
    }

    public static void testInfo(String message) {
        tests.get().info(message);
        LogFactory.LogInfo(message);
    }

    public static void PassTest(String message) {
        //tests.get().pass("<b>"+message+"</b>\n"+CaptureScreenshot());
        tests.get().pass("<b>" + message + "</b>");
        testPassStatus.set(testPassStatus.get() + 1);
        LogFactory.LogInfo(message);
    }

    //	public static void FailTest(String message) {
//		//testStatus+=1;
//		testStatus.set(testStatus.get()+1);
//		testResult="FAIL";
//		messageList.add(message);
//		//tests.get().fail("<b><font color=red>"+message+"<font></b>\n"+CaptureScreenshot());
//		tests.get().fail("<b><font color=red>"+message+"<font></b>\n");
//		LogFactory.LogWarning(message);
//
//	}
    public static void FailTest(String message) {
        //testStatus+=1;
        testStatus.set(testStatus.get() + 1);
        testResult.remove();
        testResult.set("FAIL");
        messageList.add(message);
        //tests.get().fail("<b><font color=red>"+message+"<font></b>\n"+CaptureScreenshot());
        tests.get().fail("<b><font color=red>" + message + "<font></b>\n");
        LogFactory.LogWarning(message);

    }

    public static ExtentTest getTest() {
        return tests.get();
    }

    /*public static void endTest() {

        if(CurrentScenario.isFailed()||(testStatus.get()>0)) {
            TestCases.get(CurrentScenario.getName()).put("Status", "FAIL");
        }

        //setting test case and status in test case node
        if(TestCases.get(CurrentScenario.getName()).get("Status").equalsIgnoreCase("Pass")) {
            String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
            tcTestNode.log(Status.PASS, TestCases.get(CurrentScenario.getName()).get("TC")+str);
        }else {
            String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
            tcTestNode.log(Status.FAIL, TestCases.get(CurrentScenario.getName()).get("TC")+str);
        }

        //set test case in test case document
        tcTest.log(Status.INFO, TestCases.get(CurrentScenario.getName()).get("TC"));

        if((testStatus.get()==0)&&(testPassStatus.get()==0)) {
            //ReportFactory.testInfo("Overall Status: NO TESTS EXECUTED");
            tests.get().skip("Overall Status: NO TESTS EXECUTED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
        }else if(testStatus.get()!=0) {
            //ReportFactory.testInfo("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
            tests.get().fail("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            throw new RuntimeException();
        }else if(StepNumber<TotalTestSteps) {
            //ReportFactory.testInfo("Overall Status: FAILED (All test steps not executed)");
            //ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
            ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
            ReportFactory.FailTest("Overall Status: FAILED (All test steps not executed)");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            throw new RuntimeException();
        }else if(CurrentScenario.isFailed()){
            //ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
            ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
            ReportFactory.FailTest("Overall Status: FAILED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            throw new RuntimeException();
        }else {
            ReportFactory.testInfo("Overall Status: PASSED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalPassTests++;
            PassTests.add(testName.get());
        }


    }*/
    public static void endTest() {

        if (CurrentScenario.get().isFailed() || (testStatus.get() > 0)) {
            TestCases.get(CurrentScenario.get().getName()).put("Status", "FAIL");
        }

        //setting test case and status in test case node
        if (TestCases.get(CurrentScenario.get().getName()).get("Status").equalsIgnoreCase("Pass")) {
            String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
            tcTestNode.log(Status.PASS, TestCases.get(CurrentScenario.get().getName()).get("TC") + str);
        } else {
            String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
            tcTestNode.log(Status.FAIL, TestCases.get(CurrentScenario.get().getName()).get("TC") + str);
        }

        //set test case in test case document
        tcTest.log(Status.INFO, TestCases.get(CurrentScenario.get().getName()).get("TC"));

        if ((testStatus.get() == 0) && (testPassStatus.get() == 0)) {
            //ReportFactory.testInfo("Overall Status: NO TESTS EXECUTED");
            tests.get().skip("Overall Status: NO TESTS EXECUTED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
        } else if (testStatus.get() != 0) {
            //ReportFactory.testInfo("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
            tests.get().fail("Overall Status: FAILED (" + testStatus.get() + " failure(s))");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            UpdateTestStatusCount("Fail");
            throw new RuntimeException();
        } else if (StepNumber.get() < TotalTestSteps.get()) {
            //ReportFactory.testInfo("Overall Status: FAILED (All test steps not executed)");
            //ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
            ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
            ReportFactory.FailTest("Overall Status: FAILED (All test steps not executed)");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            UpdateTestStatusCount("Fail");
            throw new RuntimeException();
        } else if (CurrentScenario.get().isFailed()) {
            //ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
            ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
            ReportFactory.FailTest("Overall Status: FAILED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalFailTests++;
            FailTests.put(testName.get(), testStatus.get());
            UpdateTestStatusCount("Fail");
            throw new RuntimeException();
        } else {
            ReportFactory.testInfo("Overall Status: PASSED");
            tests.remove();
            LogFactory.LogInfo("=========Ending Test==================");
            totalPassTests++;
            PassTests.add(testName.get());
            UpdateTestStatusCount("Pass");
        }
    }

    //	public static void SetTestCaseErrorDetials(String error) {
//		errorDetails=error;
//	}
    public static void SetTestCaseErrorDetials(String error) {
        errorDetails.remove();
        errorDetails.set(error);
    }

    public static void Setup_SlackIntegration() {
        String testName = "";
        if (System.getProperty("testName") != null) {
            testName = System.getProperty("testName");
        }
        String channelId = "";
        if (System.getProperty("channelID") != null) {
            channelId = System.getProperty("channelID");
        }
        if ((System.getProperty("Env") != null) && (System.getProperty("EnvURL") != null)) {
            ReportFactory.SetSlackDetails(channelId, testName + " Test Execution Summary", System.getProperty("EnvURL"), System.getProperty("Env"));
        } else if (System.getProperty("Env") != null) {
            ReportFactory.SetSlackDetails(channelId, testName + " Test Execution Summary", System.getProperty("Env"), System.getProperty("Env"));
        } else if (System.getProperty("EnvURL") != null) {
            ReportFactory.SetSlackDetails(channelId, testName + " Test Execution Summary", System.getProperty("EnvURL"), System.getProperty("EnvURL"));
        } else {
            ReportFactory.SetSlackDetails(channelId, testName + " Test Execution Summary", "BaseURI", "Default");
        }
    }

    /*public static void SetSlackDetails(String webHookURL, String reportName, String environmentURL) {
        WebHookURL=webHookURL;
        ReportName=reportName;
        Environment=environmentURL;
        ReportLink="";
    }*/
    public static void SetSlackDetails(String channelID, String reportName, String environmentURL, String environment) {
        ChannelID = channelID;
        ReportName = reportName;
        Environment = environment;
        EnvironmentURL = environmentURL;
        ReportLink = "";
        System.out.println(Environment);
        System.out.println(EnvironmentURL);
    }

    public static void PublishReportOnSlack() {
        endTime = System.currentTimeMillis();
        suiteEndTime = System.currentTimeMillis();
        totalSuiteExecutionTimeInSeconds = calculateSuiteExecutionTime();
        System.out.println("totalSuiteExecutionTimeInSeconds : " + totalSuiteExecutionTimeInSeconds);
        //applicationName = "Span Calculator";
        tagNumber = "Tag Number";
        applicationName = System.getProperty("ApplicationName");
        for (String channel : ChannelID.split(",")) {
            System.out.println(channel);
            List<String> blocks = new ArrayList<String>();
            int cnt = 0;
            SlackUtil slack = new SlackUtil(channel);
            String message = "";
            String messageText = "";
            String messageTextPart = "";
            if (totalFailTestsCount.size() == 0) {
                messageText = "@arom-qa-team, *Application Name : " + applicationName + "*, *TimeTaken: " + (int) (totalSuiteExecutionTimeInSeconds / 60) + "mins*, *" + ReportName + "*,>*Environment : <" + Environment + ">*,>*Total Tests : " + totalTestsCount.size() + "*,>*Passed : " + totalPassTestsCount.size() + "*,>*Failed : " + totalFailTestsCount.size() + "*,>*Failed Tests :* _NA_,>*Test Report :*  _See Next Bot Message_";
                message = "@arom-qa-team"
                        + "\n *Application Name : " + applicationName + "*"
                        + "\n *" + ReportName + "*"
                        + "\n>*Environment : <" + Environment + ">*"
                        + "\n>*Total Tests : " + totalTestsCount.size() + "*"
                        + "\n>*Passed : " + totalPassTestsCount.size() + "*"
                        + "\n>*Failed : " + totalFailTestsCount.size() + "*"
                        + "\n>*TimeTaken : " + (int) (totalSuiteExecutionTimeInSeconds / 60) + "mins*"
                        + "\n>*Failed Tests :* _NA_"
                        + "\n>*Test Report :*  _See Next Bot Message_";
                blocks.add(cnt++, message);
                message = "";
                messageTextPart = "";
            } else {
                messageText = "@arom-qa-team, *" + applicationName + "*,*TimeTaken: " + (int) (totalSuiteExecutionTimeInSeconds / 60) + "mins*,*" + ReportName + "*,>*Environment : <" + Environment + ">*,>*Total Tests : " + totalTestsCount.size() + "*,>*Passed : " + totalPassTestsCount.size() + "*,>*Failed : " + totalFailTestsCount.size() + "*,>*Failed Tests :* _View Thread_,>*Test Report :*  _See Next Bot Message_";
                message = "@arom-qa-team"
                        + "\n *" + applicationName + "*"
                        + "\n *" + ReportName + "*"
                        + "\n>*Environment : <" + Environment + ">*"
                        + "\n>*Total Tests : " + totalTestsCount.size() + "*"
                        + "\n>*Passed : " + totalPassTestsCount.size() + "*"
                        + "\n>*Failed : " + totalFailTestsCount.size() + "*"
                        + "\n>*TimeTaken : " + (int) (totalSuiteExecutionTimeInSeconds / 60) + "mins*"
                        + "\n>*Failed Tests :* _View Thread_"
                        + "\n>*Test Report :*  _See Next Bot Message_";
                blocks.add(cnt++, message);
                message = "*Failed Tests*";
                messageTextPart = ";*Failed Tests*";
            }
            for (String key : FailTests.keySet()) {
                String tmp = message + "\n>_" + key + " - " + FailTests.get(key) + " failure(s)_";
                if (tmp.length() > 3000) {
                    blocks.add(cnt++, message);
                    message = "";
                    messageText += messageTextPart;
                    messageTextPart = ";";
                } else {
                    message = message + "\n>_" + key + " - " + FailTests.get(key) + " failure(s)_";
                    messageTextPart = messageTextPart + ",>_" + key + " - " + FailTests.get(key) + " failure(s)_";
                }
            }
            if (!message.equalsIgnoreCase("")) {
                blocks.add(cnt, message);
                messageText += messageTextPart;
            }
            //save blocks to excel
            String tempfileName = sdt.format(new Date()) + ".xlsx";
            ExcelUtil.CreateWorkbook("src/test/resources/" + tempfileName);
            ExcelUtil blocksXl = new ExcelUtil("src/test/resources/" + tempfileName);
            blocksXl.getWorbook().createSheet("Blocks");
            blocksXl.setAvtiveSheet("Blocks");
            int i = 0;
            for (String mssg : blocks) {
                //slack.postFormattedMessage(mssg);
                //slack.postFormattedMessageWithThread(mssg);
                blocksXl.setParam(0, i, mssg);
                i++;
            }
            blocksXl.setParam(1, 0, ChannelID);
            blocksXl.CopyWorkbook("src/test/resources/Test_Status.xlsx");
            blocksXl.closeWorkbook();

            //save test status text to file
            File file1 = new File("src/test/resources/Test_Status_Text.txt");
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("src/test/resources/Test_Status_Text.txt");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                myWriter.write(messageText);
                myWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void PublishDataToGrafana() {

        String isGitRun = "false";

        if(System.getProperty("isGitRun") != null) {
            isGitRun = System.getProperty("isGitRun");
        }

        if (isGitRun.equalsIgnoreCase("true")) {

            String sql = "INSERT INTO test_case_results_summary (teamname, testfeaturename, testcaseName, status, execution_time) " +
                    "VALUES (?, ?, ?, ?, NOW()) ON DUPLICATE KEY UPDATE status = VALUES(status), execution_time = VALUES(execution_time);";

            for (String key : FailTests.keySet()) {

                try {
                    PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql);
                    pstmt.setString(1, applicationName.toUpperCase().contains("MPM") ? "AROM-Margin" : "AROM-NonMargin");
                    pstmt.setString(2, ReportName);
                    pstmt.setString(3, key);
                    pstmt.setInt(4, 0);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }

            for (String key : PassTests) {
                try {
                    PreparedStatement pstmt = DatabaseManager.getInstance().getConnection().prepareStatement(sql);
                    pstmt.setString(1, applicationName.toUpperCase().contains("MPM") ? "AROM-Margin" : "AROM-NonMargin");
                    pstmt.setString(2, ReportName);
                    pstmt.setString(3, key);
                    pstmt.setInt(4, 1);
                    pstmt.executeUpdate();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static void PublishReportOnSlack2() {
        if (System.getProperty("testStatus") == null) {
            PublishReportOnSlack3();
        } else {
            PublishReportOnSlack4();
            publishTestResultsToDashboard();
        }
    }

    public static void PublishReportOnSlack3() {
        //<${{ needs.presigned-url.outputs.s3_url }}|Entenpackage com.api.reporting;
        //
        //import java.io.File;
        //import java.io.FileInputStream;
        //import java.io.FileNotFoundException;
        //import java.io.FileOutputStream;
        //import java.io.FileWriter;
        //import java.io.IOException;
        //import java.lang.annotation.Annotation;
        //import java.lang.reflect.Field;
        //import java.lang.reflect.Method;
        //import java.net.MalformedURLException;
        //import java.net.URL;
        //import java.text.SimpleDateFormat;
        //import java.time.LocalDateTime;
        //import java.time.format.DateTimeFormatter;
        //import java.util.*;
        //import java.util.stream.Collectors;
        //import java.util.zip.ZipEntry;
        //import java.util.zip.ZipOutputStream;
        //
        //
        //import com.api.payloads.Results;
        //import com.slack.api.Slack;
        //import com.slack.api.methods.SlackApiException;
        //import com.slack.api.methods.response.chat.ChatPostMessageResponse;
        //import io.restassured.http.ContentType;
        //import org.apache.commons.mail.DefaultAuthenticator;
        //import org.apache.commons.mail.EmailAttachment;
        //import org.apache.commons.mail.EmailException;
        //import org.apache.commons.mail.HtmlEmail;
        //
        //
        //import com.aventstack.extentreports.ExtentReports;
        //import com.aventstack.extentreports.ExtentTest;
        //import com.aventstack.extentreports.Status;
        //import com.aventstack.extentreports.reporter.ExtentHtmlReporter;
        //import com.google.common.io.Files;
        //import com.jayway.jsonpath.JsonPath;
        //
        //import io.cucumber.java.Scenario;
        //import io.cucumber.plugin.event.PickleStepTestStep;
        //import io.restassured.RestAssured;
        //import io.restassured.response.Response;
        //import io.restassured.specification.RequestSpecification;
        //import com.api.logging.*;
        //import com.api.utilities.ExcelUtil;
        //import org.testng.Assert;
        //
        //public class ReportFactory {
        //	public static boolean reporting = true;
        //	private static ExtentHtmlReporter reporter;
        //	private static ExtentReports report;
        //	private static ThreadLocal<ExtentTest> tests  =new ThreadLocal<ExtentTest>();
        //	private static ExtentTest node;
        //	private static String reportFolder;
        //	private static String reportFolderName;
        //	private static String reportFilePath;
        //	private static List<String> snapshotsList = new ArrayList<String>();
        //	private static List<String> messageList = new ArrayList<String>();
        //	private static SimpleDateFormat sdt = new SimpleDateFormat("MMddyyy-hhmmss");
        //	//private static int testStatus=0;
        //
        //	private static String WebHookURL;
        //	private static String ChannelID;
        //	private static String Environment;
        //	private static String EnvironmentURL;
        //	public static String ReportName;
        //	public static String applicationName;
        //	private static String tagNumber;
        //	private static String ReportLink;
        //	public static Integer totalTests;
        //	public static LinkedList<Integer> totalTestsCount = new LinkedList<>();
        //	public static Integer totalPassTests;
        //	public static LinkedList<Integer> totalPassTestsCount = new LinkedList<>();
        //	public static List<String> PassTests;
        //	public static Integer totalFailTests;
        //	public static LinkedList<Integer> totalFailTestsCount = new LinkedList<>();
        //	public static long endTime;
        //
        //	public static long startTime;
        //	public static double totalExecutionTimeInSeconds;
        //	private static HashMap<String,Integer> FailTests;
        //
        //	private static ThreadLocal<String> testName = new ThreadLocal<String>();
        //	private static ThreadLocal<Integer> testStatus = new ThreadLocal<Integer>();
        //	private static ThreadLocal<Integer> testPassStatus = new ThreadLocal<Integer>();
        //	//private static String testResult="PASS";
        //	public static ThreadLocal<String> testResult = new ThreadLocal<>();
        //
        ////	private static List<PickleStepTestStep> TestCaseSteps;
        ////	private static Scenario CurrentScenario;
        ////	public static int TotalTestSteps;
        ////	public static int StepNumber;
        //	public static ExceptionHandler handler;
        ////	public static String errorDetails;
        //	public static HashMap<String,HashMap<String, String>> TestCases;
        //
        //	private static ThreadLocal<List<PickleStepTestStep>> TestCaseSteps=new ThreadLocal<>();
        //	private static ThreadLocal<Scenario> CurrentScenario = new ThreadLocal<>();
        //	public static ThreadLocal<Integer> TotalTestSteps= new ThreadLocal<>();
        //	public static ThreadLocal<Integer> StepNumber= new ThreadLocal<>();
        //	public static ThreadLocal<String> errorDetails= new ThreadLocal<>();
        //	public static ExtentTest tcTestNode;
        //
        //	public static ExtentHtmlReporter tcReporter;
        //	public static ExtentReports tcReport;
        //	public static ExtentTest tcTest;
        //	public static String pod;
        //
        //
        //	public static void StartReport(String reportname) {
        //		reportFolderName = sdt.format(new Date()).toString();
        //		reportFolder = System.getProperty("user.dir")+"/Reports/"+reportFolderName;
        //		File folder = new File(reportFolder);
        //		folder.mkdir();
        //		reporter = new ExtentHtmlReporter(reportFolder+"/"+reportname+".html");
        //		report = new ExtentReports();
        //		report.attachReporter(reporter);
        //		reportFilePath=reportFolder+"/"+reportname+".html";
        //		totalTests=0;
        //		totalPassTests=0;
        //		totalFailTests=0;
        //		PassTests = new ArrayList<String>();
        //		FailTests = new HashMap<String,Integer>();
        //
        //		//Test Case test Node
        //		tcTestNode = report.createTest("TEST CASES");
        //
        //		//Exception Handling
        //		handler = new ExceptionHandler();
        //		Thread.setDefaultUncaughtExceptionHandler(handler);
        //
        //		TestCases = new HashMap<String,HashMap<String,String>>();
        //
        //		//Test Case Document
        //		tcReporter = new ExtentHtmlReporter("target"+"/"+"TestCases"+".html");
        //		tcReport = new ExtentReports();
        //		tcReport.attachReporter(tcReporter);
        //		tcTest = tcReport.createTest("TEST CASES");
        //	}
        //	public static void setCucumberReports() {
        //		System.setProperty("cucumber.options", "--features src/test/resources/IPO_Features --glue stepdefs");
        //	}
        //	public static void StartTest(String testname) {
        //		startTime = System.currentTimeMillis();
        //		ExtentTest test = report.createTest(testname);
        //		tests.set(test);
        //		testStatus.set(0);
        //		testPassStatus.set(0);
        //		LogFactory.LogInfo("=========Starting Test "+testname+"==================");
        //		//LogFactory.LogInfo("Starting "+testname);
        //
        //		testName.set(testname);
        //		totalTests++;
        //		UpdateTestStatusCount("Total");
        //	}
        //
        //	public static void AssignCategories(Collection<String> categories) {
        //		for(String category: categories) {
        //			tests.get().assignCategory(category);
        //		}
        //	}
        //
        //	public static void testInfo(String message) {
        //		tests.get().info(message);
        //		LogFactory.LogInfo(message);
        //	}
        //
        //	public static void PassTest(String message) {
        //		//tests.get().pass("<b>"+message+"</b>\n"+CaptureScreenshot());
        //		tests.get().pass("<b>"+message+"</b>");
        //		testPassStatus.set(testPassStatus.get()+1);
        //		LogFactory.LogInfo(message);
        //	}
        ////	public static void FailTest(String message) {
        ////		//testStatus+=1;
        ////		testStatus.set(testStatus.get()+1);
        ////		testResult="FAIL";
        ////		messageList.add(message);
        ////		//tests.get().fail("<b><font color=red>"+message+"<font></b>\n"+CaptureScreenshot());
        ////		tests.get().fail("<b><font color=red>"+message+"<font></b>\n");
        ////		LogFactory.LogWarning(message);
        ////
        ////	}
        //	public static void FailTest(String message) {
        //		//testStatus+=1;
        //		testStatus.set(testStatus.get()+1);
        //		testResult.remove();
        //		testResult.set("FAIL");
        //		messageList.add(message);
        //		//tests.get().fail("<b><font color=red>"+message+"<font></b>\n"+CaptureScreenshot());
        //		tests.get().fail("<b><font color=red>"+message+"<font></b>\n");
        //		LogFactory.LogWarning(message);
        //
        //	}
        //	public static ExtentTest getTest() {
        //		return tests.get();
        //	}
        //	/*public static void endTest() {
        //
        //		if(CurrentScenario.isFailed()||(testStatus.get()>0)) {
        //			TestCases.get(CurrentScenario.getName()).put("Status", "FAIL");
        //		}
        //
        //		//setting test case and status in test case node
        //		if(TestCases.get(CurrentScenario.getName()).get("Status").equalsIgnoreCase("Pass")) {
        //			String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
        //			tcTestNode.log(Status.PASS, TestCases.get(CurrentScenario.getName()).get("TC")+str);
        //		}else {
        //			String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
        //			tcTestNode.log(Status.FAIL, TestCases.get(CurrentScenario.getName()).get("TC")+str);
        //		}
        //
        //		//set test case in test case document
        //		tcTest.log(Status.INFO, TestCases.get(CurrentScenario.getName()).get("TC"));
        //
        //		if((testStatus.get()==0)&&(testPassStatus.get()==0)) {
        //			//ReportFactory.testInfo("Overall Status: NO TESTS EXECUTED");
        //			tests.get().skip("Overall Status: NO TESTS EXECUTED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //		}else if(testStatus.get()!=0) {
        //			//ReportFactory.testInfo("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
        //			tests.get().fail("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			throw new RuntimeException();
        //		}else if(StepNumber<TotalTestSteps) {
        //			//ReportFactory.testInfo("Overall Status: FAILED (All test steps not executed)");
        //			//ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
        //			ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
        //			ReportFactory.FailTest("Overall Status: FAILED (All test steps not executed)");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			throw new RuntimeException();
        //		}else if(CurrentScenario.isFailed()){
        //			//ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
        //			ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
        //			ReportFactory.FailTest("Overall Status: FAILED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			throw new RuntimeException();
        //		}else {
        //			ReportFactory.testInfo("Overall Status: PASSED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalPassTests++;
        //			PassTests.add(testName.get());
        //		}
        //
        //
        //	}*/
        //	public static void endTest() {
        //
        //		if(CurrentScenario.get().isFailed()||(testStatus.get()>0)) {
        //			TestCases.get(CurrentScenario.get().getName()).put("Status", "FAIL");
        //		}
        //
        //		//setting test case and status in test case node
        //		if(TestCases.get(CurrentScenario.get().getName()).get("Status").equalsIgnoreCase("Pass")) {
        //			String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
        //			tcTestNode.log(Status.PASS, TestCases.get(CurrentScenario.get().getName()).get("TC")+str);
        //		}else {
        //			String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
        //			tcTestNode.log(Status.FAIL, TestCases.get(CurrentScenario.get().getName()).get("TC")+str);
        //		}
        //
        //		//set test case in test case document
        //		tcTest.log(Status.INFO, TestCases.get(CurrentScenario.get().getName()).get("TC"));
        //
        //		if((testStatus.get()==0)&&(testPassStatus.get()==0)) {
        //			//ReportFactory.testInfo("Overall Status: NO TESTS EXECUTED");
        //			tests.get().skip("Overall Status: NO TESTS EXECUTED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //		}else if(testStatus.get()!=0) {
        //			//ReportFactory.testInfo("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
        //			tests.get().fail("Overall Status: FAILED ("+testStatus.get()+" failure(s))");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			UpdateTestStatusCount("Fail");
        //			throw new RuntimeException();
        //		}else if(StepNumber.get()<TotalTestSteps.get()) {
        //			//ReportFactory.testInfo("Overall Status: FAILED (All test steps not executed)");
        //			//ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
        //			ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
        //			ReportFactory.FailTest("Overall Status: FAILED (All test steps not executed)");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			UpdateTestStatusCount("Fail");
        //			throw new RuntimeException();
        //		}else if(CurrentScenario.get().isFailed()){
        //			//ReportFactory.testInfo("<pre><b>"+errorDetails+"</b></pre>");
        //			ReportFactory.testInfo("<pre><b>An exception occured while executing step</b></pre>");
        //			ReportFactory.FailTest("Overall Status: FAILED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalFailTests++;
        //			FailTests.put(testName.get(), testStatus.get());
        //			UpdateTestStatusCount("Fail");
        //			throw new RuntimeException();
        //		}else {
        //			ReportFactory.testInfo("Overall Status: PASSED");
        //			tests.remove();
        //			LogFactory.LogInfo("=========Ending Test==================");
        //			totalPassTests++;
        //			PassTests.add(testName.get());
        //			UpdateTestStatusCount("Pass");
        //		}
        //	}
        //
        ////	public static void SetTestCaseErrorDetials(String error) {
        ////		errorDetails=error;
        ////	}
        //	public static void SetTestCaseErrorDetials(String error) {
        //		errorDetails.remove();
        //		errorDetails.set(error);
        //	}
        //
        //	public static void Setup_SlackIntegration() {
        //		String testName = "";
        //		if(System.getProperty("testName")!=null) {
        //			testName=System.getProperty("testName");
        //		}
        //		String channelId="";
        //		if(System.getProperty("channelID")!=null) {
        //			channelId=System.getProperty("channelID");
        //		}
        //		if((System.getProperty("Env")!=null) && (System.getProperty("EnvURL")!=null)) {
        //			ReportFactory.SetSlackDetails(channelId, testName+" Test Execution Summary",System.getProperty("EnvURL"), System.getProperty("Env"));
        //		}else if(System.getProperty("Env")!=null) {
        //			ReportFactory.SetSlackDetails(channelId, testName+" Test Execution Summary",System.getProperty("Env"),System.getProperty("Env"));
        //		}else if(System.getProperty("EnvURL")!=null) {
        //			ReportFactory.SetSlackDetails(channelId, testName+" Test Execution Summary", System.getProperty("EnvURL"),System.getProperty("EnvURL"));
        //		}else {
        //			ReportFactory.SetSlackDetails(channelId, testName+" Test Execution Summary", "BaseURI", "Default");
        //		}
        //	}
        //
        //	/*public static void SetSlackDetails(String webHookURL, String reportName, String environmentURL) {
        //		WebHookURL=webHookURL;
        //		ReportName=reportName;
        //		Environment=environmentURL;
        //		ReportLink="";
        //	}*/
        //	public static void SetSlackDetails(String channelID, String reportName, String environmentURL,String environment) {
        //		ChannelID=channelID;
        //		ReportName=reportName;
        //		Environment=environment;
        //		EnvironmentURL=environmentURL;
        //		ReportLink="";
        //		System.out.println(Environment);
        //		System.out.println(EnvironmentURL);
        //	}
        //	public static void PublishReportOnSlack() {
        //		endTime = System.currentTimeMillis();
        //		//applicationName = "Span Calculator";
        //		tagNumber = "Tag Number";
        //		applicationName = System.getProperty("ApplicationName");
        //		for(String channel: ChannelID.split(",")) {
        //			System.out.println(channel);
        //			List<String> blocks = new ArrayList<String>();
        //			int cnt=0;
        //			SlackUtil slack = new SlackUtil(channel);
        //			String message="";
        //			String messageText="";
        //			String messageTextPart="";
        //			if(totalFailTestsCount.size()==0) {
        //				messageText = "<!here>, *Application Name : "+ applicationName+"*,*"+ReportName+"*,>*Environment : <"+Environment+">*,>*Total Tests : "+totalTestsCount.size()+"*,>*Passed : "+totalPassTestsCount.size()+"*,>*Failed : "+totalFailTestsCount.size()+"*,>*Failed Tests :* _NA_,>*Test Report :*  _See Next Bot Message_";
        //				message = "<!here>"
        //						+ "\n *Application Name : "+ applicationName+"*"
        //						+ "\n *"+ReportName+"*"
        //						+ "\n>*Environment : <"+Environment+">*"
        //						+ "\n>*Total Tests : "+totalTestsCount.size()+"*"
        //						+ "\n>*Passed : "+totalPassTestsCount.size()+"*"
        //						+ "\n>*Failed : "+totalFailTestsCount.size()+"*"
        //						+ "\n>*Failed Tests :* _NA_"
        //						+ "\n>*Test Report :*  _See Next Bot Message_";
        //				blocks.add(cnt++, message);
        //				message="";
        //				messageTextPart="";
        //			}else {
        //				messageText = "<!here>, *"+ applicationName+"*,*"+ReportName+"*,>*Environment : <"+Environment+">*,>*Total Tests : "+totalTestsCount.size()+"*,>*Passed : "+totalPassTestsCount.size()+"*,>*Failed : "+totalFailTestsCount.size()+"*,>*Failed Tests :* _View Thread_,>*Test Report :*  _See Next Bot Message_";
        //				message = "<!here>"
        //						+"\n *"+applicationName+"*"
        //						+ "\n *"+ReportName+"*"
        //						+ "\n>*Environment : <"+Environment+">*"
        //						+ "\n>*Total Tests : "+totalTestsCount.size()+"*"
        //						+ "\n>*Passed : "+totalPassTestsCount.size()+"*"
        //						+ "\n>*Failed : "+totalFailTestsCount.size()+"*"
        //						+ "\n>*Failed Tests :* _View Thread_"
        //						+ "\n>*Test Report :*  _See Next Bot Message_";
        //				blocks.add(cnt++, message);
        //				message="*Failed Tests*";
        //				messageTextPart=";*Failed Tests*";
        //			}
        //			for(String key:FailTests.keySet()) {
        //				String tmp=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        //				if(tmp.length()>3000) {
        //					blocks.add(cnt++, message);
        //					message="";
        //					messageText+=messageTextPart;
        //					messageTextPart=";";
        //				}else {
        //					message=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        //					messageTextPart=messageTextPart+",>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        //				}
        //			}
        //			if(!message.equalsIgnoreCase("")) {
        //				blocks.add(cnt, message);
        //				messageText+=messageTextPart;
        //			}
        //			//save blocks to excel
        //			String tempfileName = sdt.format(new Date())+".xlsx";
        //			ExcelUtil.CreateWorkbook("src/test/resources/"+tempfileName);
        //			ExcelUtil blocksXl = new ExcelUtil("src/test/resources/"+tempfileName);
        //			blocksXl.getWorbook().createSheet("Blocks");
        //			blocksXl.setAvtiveSheet("Blocks");
        //			int i = 0;
        //			for(String mssg:blocks) {
        //				//slack.postFormattedMessage(mssg);
        //				//slack.postFormattedMessageWithThread(mssg);
        //				blocksXl.setParam(0, i, mssg);
        //				i++;
        //			}
        //			blocksXl.setParam(1, 0, ChannelID);
        //			blocksXl.CopyWorkbook("src/test/resources/Test_Status.xlsx");
        //			blocksXl.closeWorkbook();
        //
        //			//save test status text to file
        //			File file1 = new File("src/test/resources/Test_Status_Text.txt");
        //			FileWriter myWriter = null;
        //			try {
        //				myWriter = new FileWriter("src/test/resources/Test_Status_Text.txt");
        //			} catch (IOException e1) {
        //				// TODO Auto-generated catch block
        //				e1.printStackTrace();
        //			}
        //			try {
        //				myWriter.write(messageText);
        //				myWriter.close();
        //			} catch (IOException e) {
        //				// TODO Auto-generated catch block
        //				e.printStackTrace();
        //			}
        //		}
        //	}
        //	public static void PublishReportOnSlack2(){
        //		if(System.getProperty("testStatus")==null) {
        //			PublishReportOnSlack3();
        //		}else {
        //			PublishReportOnSlack4();
        //			publishTestResultsToDashboard();
        //		}
        //	}
        //
        //	public static void PublishReportOnSlack3(){
        //		//<${{ needs.presigned-url.outputs.s3_url }}|Extent Report Link>
        //		String reportLink = System.getProperty("ReportLink");
        //		ExcelUtil blocksXl = new ExcelUtil("src/test/resources/Test_Status.xlsx");
        //		blocksXl.setAvtiveSheet("Blocks");
        //		int rowCount = blocksXl.getNumberOfDataRows()+1;
        //		String channelId = System.getProperty("channelID");
        //		for(String channel: channelId.split(",")) {
        //			SlackUtil slack = new SlackUtil(channel);
        //			for(int i=0;i<rowCount;i++) {
        //				String mssg = blocksXl.getParam(0, i);
        //				mssg=mssg.replace("_See Next Bot Message_", "<"+reportLink+"|Extent Report Link>");
        //				slack.postFormattedMessageWithThread(mssg);
        //			}
        //		}
        //	}
        //
        //	public static void PublishReportOnSlackThread3(){
        //		//<${{ needs.presigned-url.outputs.s3_url }}|Extent Report Link>
        //		String reportLink = System.getProperty("ReportLink");
        //		ExcelUtil blocksXl = new ExcelUtil("src/test/resources/Test_Status.xlsx");
        //		blocksXl.setAvtiveSheet("Blocks");
        //		int rowCount = blocksXl.getNumberOfDataRows()+1;
        //		String channelId = System.getProperty("channelID");
        //		String slackThreadId = System.getProperty("slackThread");
        //		for(String channel: channelId.split(",")) {
        //			SlackUtil slack = new SlackUtil(channel);
        //			for(int i=0;i<rowCount;i++) {
        //				String mssg = blocksXl.getParam(0, i);
        //				mssg=mssg.replace("_See Next Bot Message_", "<"+reportLink+"|Extent Report Link>");
        //				//postMessageInThread(channel,slackThreadId,mssg);
        //				slack.postFormattedMessageWithThread(mssg);
        //			}
        //		}
        //	}
        //
        //	public static void PublishReportOnSlack4() {
        //		String testStatus = System.getProperty("testStatus");
        //		String reportLink = System.getProperty("ReportLink");
        //		String channelId = System.getProperty("channelID");
        //		String isDeploy = System.getProperty("isDeploy");
        //		for(String channel: channelId.split(",")) {
        //			SlackUtil slack = new SlackUtil(channel);
        //			if(testStatus!=null) {
        //				List<String>blocks = Arrays.asList(testStatus.split(";"));
        //				for(String mssg:blocks) {
        //					List<String>messageLines = Arrays.asList(mssg.split(","));
        //					String newMssg ="";
        //					for(String s:messageLines) {
        //						newMssg+=(s+"\n");
        //					}
        //
        //					mssg=newMssg.replace("_See Next Bot Message_", "<"+reportLink+"|Extent Report Link>");
        //					//null pointer handling
        //					if(isDeploy!=null) {
        //						if (isDeploy.equals("true")) {
        //							String slackThreadId = System.getProperty("slackThread");
        //							postMessageInThread(channel, slackThreadId, mssg);
        //						}
        //					}
        //					else{
        //						slack.postFormattedMessageWithThread(mssg);
        //					}
        //				}
        //			}
        //		}
        //	}
        //	/*public static void PublishReportOnSlack() {
        //		for(String url: WebHookURL.split(",")) {
        //			SlackUtil slack = new SlackUtil(url);
        //			String message = "<!here>"
        //					+ "\n *"+ReportName+"*"
        //					+ "\n>*Environment : <"+Environment+">*"
        //					+ "\n>*Total Tests : "+totalTests+"*"
        //					+ "\n>*Passed : "+totalPassTests+"*"
        //					+ "\n>*Failed : "+totalFailTests+"*"
        //					+ "\n>*Test Report : <"+ReportLink+"|Extent Report Link>*"
        //					+ "\n*Failed Tests*";
        //			for(String key:FailTests.keySet()) {
        //				message=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        //			}
        //			String mssg=slack.postFormattedMessage(message);
        //			if(!mssg.equalsIgnoreCase("ok")) {
        //				String message2 = "<!here>"
        //						+ "\n *"+ReportName+"*"
        //						+ "\n>*Environment : <"+Environment+">*"
        //						+ "\n>*Total Tests : "+totalTests+"*"
        //						+ "\n>*Passed : "+totalPassTests+"*"
        //						+ "\n>*Failed : "+totalFailTests+"*"
        //						+ "\n>*Test Report : <"+ReportLink+"|Extent Report Link>* /* _see next bot message_*"
        //						+ "\n*Failed Tests*"
        //						+"\n>_Check Test Extent Report_";
        //				slack.postFormattedMessage(message2);
        //			}
        //		}
        //	}
        //	 public static void PublishReportOnSlack() {
        //		SlackUtil slack = new SlackUtil(WebHookURL);
        //		String message = "<!here>"
        //				+ "\n *"+ReportName+"*"
        //				+ "\n>*Environment : <"+Environment+">*"
        //				+ "\n>*Total Tests : "+totalTests+"*"
        //				+ "\n>*Passed : "+totalPassTests+"*"
        //				+ "\n>*Failed : "+totalFailTests+"*"
        //				+ "\n>*Test Report : <"+ReportLink+"|Extent Report Link>*"
        //				+ "\n*Failed Tests*";
        //		for(String key:FailTests.keySet()) {
        //			message=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        //		}
        //		slack.postFormattedMessage(message);
        //	}
        //	 */
        //	public static void EndReport() {
        //		report.flush();
        //		tcReport.flush();
        //		LogFactory.LogInfo("Test Report Path:- "+reportFilePath);
        //		try {
        //			Files.copy(new File(reportFilePath), new File("target/ExtentReport.html"));
        //		} catch (IOException e) {
        //			e.printStackTrace();
        //		}
        //		//ZipDirectory(reportFolder);
        //		//EmailReport();
        //	}
        //	public static void saveCucumberReports() {
        //		String bdd_folder = reportFolder+"/BDD_Reports";
        //		String htmlreport_folder = bdd_folder+"/htmlreport";
        //		File folder = new File(bdd_folder);
        //		folder.mkdir();
        //		folder = new File(htmlreport_folder);
        //		folder.mkdir();
        //		try {
        //			//Files.copy(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
        //			org.apache.commons.io.FileUtils.copyDirectory(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
        //			Files.copy(new File("target/bdd_report.json"), new File(bdd_folder+"/bdd_report.json"));
        //			Files.copy(new File("target/bdd_report.xml"), new File(bdd_folder+"/bdd_report.xml"));
        //			/*org.apache.commons.io.FileUtils.copyDirectory(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
        //			org.apache.commons.io.FileUtils.copyFileToDirectory(new File("target/bdd_report.json"), new File(bdd_folder));
        //			org.apache.commons.io.FileUtils.copyFileToDirectory(new File("target/bdd_report.xml"), new File(bdd_folder));*/
        //			LogFactory.LogInfo("BDD Report Path:- "+bdd_folder);
        //		} catch (IOException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        //
        //	}
        //	public static void UploadReportToFileIO() {
        //		RequestSpecification req = RestAssured.given();
        //		req.contentType("multipart/form-data");
        //		req.multiPart("file", new File(reportFilePath));
        //		Response resp = req.post("https://file.io");
        //		System.out.println(resp.getBody().asString());
        //		String uploadedfilepath = JsonPath.read(resp.getBody().asString(),"$.link");
        //		ReportLink=uploadedfilepath;
        //		LogFactory.LogInfo("File.io Report Link: "+uploadedfilepath);
        //	}
        //
        //	public static void PrintStepInReport(Method method, Object ...  params) {
        //		Annotation  myAnnotation = method.getAnnotations()[0];
        //		System.out.println("myAnnotation=" + myAnnotation);
        //		String annotation = myAnnotation.toString();
        //		String step = annotation.substring(annotation.indexOf("value=", 0)+6, annotation.indexOf(")", 0));
        //		if(params.length>0) {
        //			int index = -1;
        //			for(Object o:params) {
        //				if(o instanceof String) {
        //					index = step.indexOf("{string}");
        //					step = step.substring(0, index)+o+step.substring(index+8);
        //				}else if(o instanceof Integer) {
        //					index = step.indexOf("{int}");
        //					step = step.substring(0, index)+String.valueOf(o)+step.substring(index+5);
        //				}else if(o instanceof Double) {
        //					index = step.indexOf("{double}");
        //					step = step.substring(0, index)+String.valueOf(o)+step.substring(index+8);
        //				}
        //			}
        //		}
        //		ReportFactory.testInfo("<p style=\"font-size:15px\"><b>STEP: "+step+"</b></p>");
        //
        //	}
        //
        //	/*public static void SetTestCase(Scenario scenario) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        //		CurrentScenario = scenario;
        //		Field f = scenario.getClass().getDeclaredField("delegate");
        //		f.setAccessible(true);
        //		io.cucumber.core.backend.TestCaseState sc = (io.cucumber.core.backend.TestCaseState) f.get(scenario);
        //		Field f1 = sc.getClass().getDeclaredField("testCase");
        //		f1.setAccessible(true);
        //		io.cucumber.plugin.event.TestCase testCase = (io.cucumber.plugin.event.TestCase) f1.get(sc);
        //
        //		List<PickleStepTestStep> testSteps = testCase.getTestSteps().stream().filter(x -> x instanceof PickleStepTestStep).map(x -> (PickleStepTestStep) x).collect(Collectors.toList());
        //		TestCaseSteps = testSteps;
        //		TotalTestSteps = testSteps.size();
        //		StepNumber=0;
        //	}*/
        //	public static void SetTestCase(Scenario scenario) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        //		CurrentScenario.remove();
        //		CurrentScenario.set(scenario);
        //		Field f = scenario.getClass().getDeclaredField("delegate");
        //		f.setAccessible(true);
        //		io.cucumber.core.backend.TestCaseState sc = (io.cucumber.core.backend.TestCaseState) f.get(scenario);
        //		Field f1 = sc.getClass().getDeclaredField("testCase");
        //		f1.setAccessible(true);
        //		io.cucumber.plugin.event.TestCase testCase = (io.cucumber.plugin.event.TestCase) f1.get(sc);
        //
        //		List<PickleStepTestStep> testSteps = testCase.getTestSteps().stream().filter(x -> x instanceof PickleStepTestStep).map(x -> (PickleStepTestStep) x).collect(Collectors.toList());
        //		TestCaseSteps.remove();
        //		TestCaseSteps.set(testSteps);
        //		TotalTestSteps.remove();
        //		TotalTestSteps.set(testSteps.size());
        //		StepNumber.remove();
        //		StepNumber.set(0);
        //
        //	}
        //
        ////	public static void PrintTestCase() {
        ////
        ////		String testcaseHTML = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> TEST CASE (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        ////		String testcaseHTML2 = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> "+CurrentScenario.getName()+" (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        ////		for (PickleStepTestStep ts : TestCaseSteps) {
        ////			System.out.println(ts.getStep().getKeyWord() + ts.getStep().getText());
        ////			testcaseHTML+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
        ////			testcaseHTML2+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
        ////		}
        ////		testcaseHTML+="</ol></details></div>";
        ////		testcaseHTML2+="</ol></div>";
        ////
        ////		final String testcaseHTML3=testcaseHTML2;
        ////
        ////		ReportFactory.testInfo(testcaseHTML);
        ////
        ////		TestCases.put(CurrentScenario.getName(),new HashMap<String, String>() {{
        ////			put("TC", testcaseHTML3);
        ////			put("Status", "PASS");
        ////		}});
        ////	}
        //	public static void PrintTestCase() {
        //
        //		String testcaseHTML = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> TEST CASE (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        //		String testcaseHTML2 = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> "+CurrentScenario.get().getName()+" (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        //		for (PickleStepTestStep ts : TestCaseSteps.get()) {
        //			System.out.println(ts.getStep().getKeyWord() + ts.getStep().getText());
        //			testcaseHTML+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
        //			testcaseHTML2+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
        //		}
        //		testcaseHTML+="</ol></details></div>";
        //		testcaseHTML2+="</ol></div>";
        //
        //		final String testcaseHTML3=testcaseHTML2;
        //
        //		ReportFactory.testInfo(testcaseHTML);
        //
        //		TestCases.put(CurrentScenario.get().getName(),new HashMap<String, String>() {{
        //			put("TC", testcaseHTML3);
        //			put("Status", "PASS");
        //		}});
        //	}
        //
        //	public static void PrintAllTestCases() {
        //		ExtentHtmlReporter tcReporter = new ExtentHtmlReporter("target"+"/"+"TestCases"+".html");
        //		ExtentReports tcReport = new ExtentReports();
        //		tcReport.attachReporter(tcReporter);
        //		ExtentTest tcTest = tcReport.createTest("TEST CASES");
        //
        //		ExtentTest test = report.createTest("TEST CASES");
        //		for(String testcase:TestCases.keySet()) {
        //			if(TestCases.get(testcase).get("Status").equalsIgnoreCase("Pass")) {
        //				String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
        //				test.log(Status.PASS, TestCases.get(testcase).get("TC")+str);
        //			}else {
        //				String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
        //				test.log(Status.FAIL, TestCases.get(testcase).get("TC")+str);
        //			}
        //			tcTest.log(Status.INFO, TestCases.get(testcase).get("TC"));
        //		}
        //		tcReport.flush();
        //	}
        //
        ////	public static void PrintCurrentStepText() {
        ////		PickleStepTestStep ts = (PickleStepTestStep) TestCaseSteps.get(StepNumber);
        ////		ReportFactory.testInfo("<div style=\"background-color:#E7E9EB;\"><p style=\"font-size:15px\"><b>STEP "+(StepNumber+1)+": "+ts.getStep().getKeyWord() + ts.getStep().getText()+"</b></p></div>");
        ////	}
        //	public static void PrintCurrentStepText() {
        //		PickleStepTestStep ts = (PickleStepTestStep) TestCaseSteps.get().get(StepNumber.get());
        //		ReportFactory.testInfo("<div style=\"background-color:#E7E9EB;\"><p style=\"font-size:15px\"><b>STEP "+(StepNumber.get()+1)+": "+ts.getStep().getKeyWord() + ts.getStep().getText()+"</b></p></div>");
        //	}
        //
        //	public static void EmailReport() {
        //
        //		// Create the attachment
        //		EmailAttachment attachment = new EmailAttachment();
        //		//attachment.setPath("C:\\Users\\Chandrakant.Shetty\\eclipse-workspace\\NXT_Web/Reports/03182021-1218964/RegressionTestSuite.html");
        //		//reportFolder = "C:\\Users\\Chandrakant.Shetty\\eclipse-workspace\\NXT_Web/Reports/03182021-1218964";
        //		//reportFolderName="03182021-1218964";
        //		attachment.setPath(reportFolder+".zip");
        //		attachment.setDisposition(EmailAttachment.ATTACHMENT);
        //		attachment.setDescription("Test Report Package");
        //		//attachment.setName("SeleniumTest.html");
        //		attachment.setName(reportFolderName+".zip");
        //
        //		// Create the email message
        //		HtmlEmail email = new HtmlEmail();
        //		//MultiPartEmail email = new MultiPartEmail();
        //		email.setHostName("smtp.gmail.com");
        //		email.setSmtpPort(465);
        //		email.setAuthenticator(new DefaultAuthenticator("cbshetty360@gmail.com", "Test@12345$"));
        //		email.setSSLOnConnect(true);
        //		try {
        //			URL LogoURL = new URL("file:///"+System.getProperty("user.dir")+"/src/main/resources/NXT_logo.png");
        //			String Logocid = email.embed(LogoURL,"Logo");
        //			email.addTo("chandrakant.shetty1989@gmail.com", "CBS");
        //			email.addTo("chandrakant.shetty@angelbroking.com", "CBS2");
        //			email.setFrom("cbshetty9@gmail.com", "Me");
        //			email.setSubject(testResult+" : Sanity Test ["+reportFolderName+"]");
        //			//email.setMsg("Please find attached the test report folder!!");
        //			String htmlhead="<head><style>table {  font-family: arial, sans-serif;  border-collapse: collapse;  width: 100%;}td, th {  border: 1px solid #dddddd;  text-align: left;  padding: 8px;}tr:nth-child(even) {  background-color: #dddddd;}</style></head>";
        //			String htmlmssg = "<html>"+htmlhead;
        //			htmlmssg+="<table><tbody style=\"background: linear-gradient(to right, #25c481, #25b7c4);color: #fff;\"><tr><td colspan=\"3\"><img height=\"25\" width=\"10%\" src=\"cid:"+Logocid+"\"></td></tr>";
        //			htmlmssg+="<tr><td colspan=\"3\"><b>TEST TYPE : SANITY TEST</b></td></tr>";
        //			htmlmssg+="<tr><td colspan=\"3\"><b>TEST STATUS : "+testResult+" ("+testStatus.get()+" failures)</b></td></tr>";
        //			htmlmssg+="<tr><td colspan=\"3\"><b>TEST TIMESTAMP : "+reportFolderName+"</b></td></tr>";
        //			if(testStatus.get()!=0) {
        //				htmlmssg+="<tr><td colspan=\"3\">"+"<table><thead><tr><th><b>#</b></th><th><b>Error Details</b></th><th><b>Snapshot</b></th></tr></thead><tbody>";
        //				int count=0;
        //				for(String msg:messageList) {
        //					URL url = new URL("file:///"+snapshotsList.get(count));
        //					String cid = email.embed(url, msg);
        //					htmlmssg+="<tr><td>"+(count+1)+"</td><td>"+msg+"</td><td>"+"<img width=\"10%\" src=\"cid:"+cid+"\">"+"</td></tr>";
        //					count+=1;
        //				}
        //				htmlmssg+="</tbody></table></td></tr>";
        //			}
        //			htmlmssg+="</tbody></table></html>";
        //			email.setHtmlMsg(htmlmssg);
        //			email.setTextMsg("Please find attached the test report folder!!");
        //			// add the attachment
        //			email.attach(attachment);
        //
        //			// send the email
        //			email.send();
        //			LogFactory.LogInfo("Report Zipped and Mailed!!");
        //		} catch (EmailException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		} catch (MalformedURLException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        //
        //	}
        //	private static List<String> fileList = new ArrayList<String>();
        //	public static void ZipDirectory(String dirPath) {
        //		String zipDirPath = dirPath+".zip";
        //		File zipDir = new File(dirPath);
        //		populateFileList(zipDir.listFiles());
        //		try {
        //			LogFactory.LogInfo("Zipping Files.........");
        //			FileOutputStream fos = new FileOutputStream(zipDirPath);
        //			ZipOutputStream zos = new ZipOutputStream(fos);
        //			for(String filepath:fileList) {
        //				LogFactory.LogInfo("Adding File to Zip: "+filepath);
        //				ZipEntry ze = new ZipEntry(filepath.substring(dirPath.length()+1, filepath.length()));
        //				zos.putNextEntry(ze);
        //				FileInputStream fis = new FileInputStream(filepath);
        //				byte[] buffer= new byte[1024];
        //				int len;
        //				while((len=fis.read(buffer))>0) {
        //					zos.write(buffer, 0, len);
        //				}
        //				zos.closeEntry();
        //				fis.close();
        //			}
        //			zos.close();
        //			fos.close();
        //			LogFactory.LogInfo("Zipped Results: "+zipDirPath);
        //		} catch (FileNotFoundException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		} catch (IOException e) {
        //			// TODO Auto-generated catch block
        //			e.printStackTrace();
        //		}
        //
        //	}
        //	public static void populateFileList(File[] files) {
        //		for(File file:files) {
        //			if(file.isFile()) {
        //				fileList.add(file.getAbsolutePath());
        //			}else {
        //				populateFileList(file.listFiles());
        //			}
        //		}
        //	}
        //
        //	public static double calculateExecutionTime(){
        //		long totalExecutionTime = endTime - startTime;
        //		return totalExecutionTimeInSeconds = totalExecutionTime / 60000.0;
        //	}
        //
        //	public static String getCurrentTimestamp() {
        //		// Get the current date and time
        //		LocalDateTime now = LocalDateTime.now();
        //		// Define the format for the timestamp
        //		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        //		// Format the current date and time
        //		return now.format(formatter);
        //	}
        //
        //	public static void postMessageInThread(String channelId, String parentTs, String message) {
        //		Slack slack = Slack.getInstance();
        //
        //		try {
        //			ChatPostMessageResponse response = slack.methods(System.getProperty("SLACK_BOT_TOKEN")).chatPostMessage(req -> req
        //					.channel(channelId)
        //					.text(message)
        //					.threadTs(parentTs) // Specify the parent message's timestamp to post in the thread
        //			);
        //			if (response.isOk()) {
        //				System.out.println("Message posted successfully in thread: " + response.getTs());
        //			} else {
        //				System.err.println("Failed to post message: " + response.getError());
        //			}
        //
        //		} catch (IOException | SlackApiException e) {
        //			System.err.println("Error occurred: " + e.getMessage());
        //		}
        //	}
        //
        //	public static void UpdateTestStatusCount(String status){
        //		if(status.equalsIgnoreCase("Total")){
        //			totalTestsCount.add(1);
        //		}else if(status.equalsIgnoreCase("Fail")){
        //			totalFailTestsCount.add(1);
        //		}else if(status.equalsIgnoreCase("Pass")){
        //			totalPassTestsCount.add(1);
        //		}
        //	}
        //
        //    public static void publishTestResultsToDashboard() {
        //
        //        String isCIRun = "false";
        //
        //        if (System.getProperty("isCIRun") != null) {
        //            isCIRun = System.getProperty("isCIRun");
        //        }
        //
        //        if (isCIRun.equals("true")) {
        //
        //            String baseURI = "http://localhost:8282"; // Will be changed after hosting
        //            String basePath = "/utils/publish_test_results.php";
        //            double percentage = ((double) (totalTestsCount.size()+1) / totalPassTests) * 100;
        //            String reportLink = "";
        //            if (System.getProperty("ReportLink") != null) {
        //                reportLink = System.getProperty("ReportLink");
        //            }
        //
        //			calculateExecutionTime();
        //
        //            Results results = new Results();
        //            results.setProjectName(applicationName);
        //            results.setEnvironment(Environment);
        //            results.setGroupName("Regression");
        //            results.setDuration((int) totalExecutionTimeInSeconds);
        //            results.setTotalCases(totalTestsCount.size()+1);
        //            results.setPercentage((int) Math.round(percentage));
        //            results.setFailedCases(totalFailTests);
        //            results.setPassedCases(totalPassTests);
        //            results.setResultLink(reportLink);
        //
        //
        //            Response response = RestAssured
        //                    .given()
        //                    .baseUri(baseURI)
        //                    .basePath(basePath)
        //                    .body(results)
        //                    .contentType(ContentType.JSON)
        //                    .post();
        //
        //            Assert.assertEquals(response.getStatusCode(), 200);
        //        }
        //    }
        //
        //
        //}t Report Link>
        String reportLink = System.getProperty("ReportLink");
        ExcelUtil blocksXl = new ExcelUtil("src/test/resources/Test_Status.xlsx");
        blocksXl.setAvtiveSheet("Blocks");
        int rowCount = blocksXl.getNumberOfDataRows() + 1;
        String channelId = System.getProperty("channelID");
        for (String channel : channelId.split(",")) {
            SlackUtil slack = new SlackUtil(channel);
            for (int i = 0; i < rowCount; i++) {
                String mssg = blocksXl.getParam(0, i);
                mssg = mssg.replace("_See Next Bot Message_", "<" + reportLink + "|Extent Report Link>");
                slack.postFormattedMessageWithThread(mssg);
            }
        }
    }

    public static void PublishReportOnSlackThread3() {
        //<${{ needs.presigned-url.outputs.s3_url }}|Extent Report Link>
        String reportLink = System.getProperty("ReportLink");
        ExcelUtil blocksXl = new ExcelUtil("src/test/resources/Test_Status.xlsx");
        blocksXl.setAvtiveSheet("Blocks");
        int rowCount = blocksXl.getNumberOfDataRows() + 1;
        String channelId = System.getProperty("channelID");
        String slackThreadId = System.getProperty("slackThread");
        for (String channel : channelId.split(",")) {
            SlackUtil slack = new SlackUtil(channel);
            for (int i = 0; i < rowCount; i++) {
                String mssg = blocksXl.getParam(0, i);
                mssg = mssg.replace("_See Next Bot Message_", "<" + reportLink + "|Extent Report Link>");
                //postMessageInThread(channel,slackThreadId,mssg);
                slack.postFormattedMessageWithThread(mssg);
            }
        }
    }

    public static void PublishReportOnSlack4() {
        String testStatus = System.getProperty("testStatus");
        if (System.getProperty("Slack_Mentions") != null) {
            testStatus = testStatus.replaceAll("@arom-qa-team", System.getProperty("Slack_Mentions"));
        }
        String reportLink = System.getProperty("ReportLink");
        String channelId = System.getProperty("channelID");
        String isDeploy = System.getProperty("isDeploy");
        for (String channel : channelId.split(",")) {
            SlackUtil slack = new SlackUtil(channel);
            if (testStatus != null) {
                List<String> blocks = Arrays.asList(testStatus.split(";"));
                for (String mssg : blocks) {
                    List<String> messageLines = Arrays.asList(mssg.split(","));
                    String newMssg = "";
                    for (String s : messageLines) {
                        newMssg += (s + "\n");
                    }

                    mssg = newMssg.replace("_See Next Bot Message_", "<" + reportLink + "|Extent Report Link>");
                    //null pointer handling
                    if (isDeploy != null) {
                        if (isDeploy.equals("true")) {
                            String slackThreadId = System.getProperty("slackThread");
                            postMessageInThread(channel, slackThreadId, mssg);
                        }
                    } else {
                        slack.postFormattedMessageWithThread(mssg);
                    }
                }
            }
        }
    }

    /*public static void PublishReportOnSlack() {
        for(String url: WebHookURL.split(",")) {
            SlackUtil slack = new SlackUtil(url);
            String message = "<!here>"
                    + "\n *"+ReportName+"*"
                    + "\n>*Environment : <"+Environment+">*"
                    + "\n>*Total Tests : "+totalTests+"*"
                    + "\n>*Passed : "+totalPassTests+"*"
                    + "\n>*Failed : "+totalFailTests+"*"
                    + "\n>*Test Report : <"+ReportLink+"|Extent Report Link>*"
                    + "\n*Failed Tests*";
            for(String key:FailTests.keySet()) {
                message=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
            }
            String mssg=slack.postFormattedMessage(message);
            if(!mssg.equalsIgnoreCase("ok")) {
                String message2 = "<!here>"
                        + "\n *"+ReportName+"*"
                        + "\n>*Environment : <"+Environment+">*"
                        + "\n>*Total Tests : "+totalTests+"*"
                        + "\n>*Passed : "+totalPassTests+"*"
                        + "\n>*Failed : "+totalFailTests+"*"
                        + "\n>*Test Report : <"+ReportLink+"|Extent Report Link>* /* _see next bot message_*"
                        + "\n*Failed Tests*"
                        +"\n>_Check Test Extent Report_";
                slack.postFormattedMessage(message2);
            }
        }
    }
     public static void PublishReportOnSlack() {
        SlackUtil slack = new SlackUtil(WebHookURL);
        String message = "<!here>"
                + "\n *"+ReportName+"*"
                + "\n>*Environment : <"+Environment+">*"
                + "\n>*Total Tests : "+totalTests+"*"
                + "\n>*Passed : "+totalPassTests+"*"
                + "\n>*Failed : "+totalFailTests+"*"
                + "\n>*Test Report : <"+ReportLink+"|Extent Report Link>*"
                + "\n*Failed Tests*";
        for(String key:FailTests.keySet()) {
            message=message+"\n>_"+key+" - "+FailTests.get(key)+" failure(s)_";
        }
        slack.postFormattedMessage(message);
    }
     */
    public static void EndReport() {
        report.flush();
        tcReport.flush();
        LogFactory.LogInfo("Test Report Path:- " + reportFilePath);
        try {
            Files.copy(new File(reportFilePath), new File("target/ExtentReport.html"));
        } catch (IOException e) {
            e.printStackTrace();
        }
        //ZipDirectory(reportFolder);
        //EmailReport();
    }

    public static void saveCucumberReports() {
        String bdd_folder = reportFolder + "/BDD_Reports";
        String htmlreport_folder = bdd_folder + "/htmlreport";
        File folder = new File(bdd_folder);
        folder.mkdir();
        folder = new File(htmlreport_folder);
        folder.mkdir();
        try {
            //Files.copy(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
            org.apache.commons.io.FileUtils.copyDirectory(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
            Files.copy(new File("target/bdd_report.json"), new File(bdd_folder + "/bdd_report.json"));
            Files.copy(new File("target/bdd_report.xml"), new File(bdd_folder + "/bdd_report.xml"));
			/*org.apache.commons.io.FileUtils.copyDirectory(new File("target/bdd_htmlreport"), new File(htmlreport_folder));
			org.apache.commons.io.FileUtils.copyFileToDirectory(new File("target/bdd_report.json"), new File(bdd_folder));
			org.apache.commons.io.FileUtils.copyFileToDirectory(new File("target/bdd_report.xml"), new File(bdd_folder));*/
            LogFactory.LogInfo("BDD Report Path:- " + bdd_folder);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void UploadReportToFileIO() {
        RequestSpecification req = RestAssured.given();
        req.contentType("multipart/form-data");
        req.multiPart("file", new File(reportFilePath));
        Response resp = req.post("https://file.io");
        System.out.println(resp.getBody().asString());
        String uploadedfilepath = JsonPath.read(resp.getBody().asString(), "$.link");
        ReportLink = uploadedfilepath;
        LogFactory.LogInfo("File.io Report Link: " + uploadedfilepath);
    }

    public static void PrintStepInReport(Method method, Object... params) {
        Annotation myAnnotation = method.getAnnotations()[0];
        System.out.println("myAnnotation=" + myAnnotation);
        String annotation = myAnnotation.toString();
        String step = annotation.substring(annotation.indexOf("value=", 0) + 6, annotation.indexOf(")", 0));
        if (params.length > 0) {
            int index = -1;
            for (Object o : params) {
                if (o instanceof String) {
                    index = step.indexOf("{string}");
                    step = step.substring(0, index) + o + step.substring(index + 8);
                } else if (o instanceof Integer) {
                    index = step.indexOf("{int}");
                    step = step.substring(0, index) + String.valueOf(o) + step.substring(index + 5);
                } else if (o instanceof Double) {
                    index = step.indexOf("{double}");
                    step = step.substring(0, index) + String.valueOf(o) + step.substring(index + 8);
                }
            }
        }
        ReportFactory.testInfo("<p style=\"font-size:15px\"><b>STEP: " + step + "</b></p>");

    }

    /*public static void SetTestCase(Scenario scenario) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        CurrentScenario = scenario;
        Field f = scenario.getClass().getDeclaredField("delegate");
        f.setAccessible(true);
        io.cucumber.core.backend.TestCaseState sc = (io.cucumber.core.backend.TestCaseState) f.get(scenario);
        Field f1 = sc.getClass().getDeclaredField("testCase");
        f1.setAccessible(true);
        io.cucumber.plugin.event.TestCase testCase = (io.cucumber.plugin.event.TestCase) f1.get(sc);

        List<PickleStepTestStep> testSteps = testCase.getTestSteps().stream().filter(x -> x instanceof PickleStepTestStep).map(x -> (PickleStepTestStep) x).collect(Collectors.toList());
        TestCaseSteps = testSteps;
        TotalTestSteps = testSteps.size();
        StepNumber=0;
    }*/
    public static void SetTestCase(Scenario scenario) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
        CurrentScenario.remove();
        CurrentScenario.set(scenario);
        Field f = scenario.getClass().getDeclaredField("delegate");
        f.setAccessible(true);
        io.cucumber.core.backend.TestCaseState sc = (io.cucumber.core.backend.TestCaseState) f.get(scenario);
        Field f1 = sc.getClass().getDeclaredField("testCase");
        f1.setAccessible(true);
        io.cucumber.plugin.event.TestCase testCase = (io.cucumber.plugin.event.TestCase) f1.get(sc);

        List<PickleStepTestStep> testSteps = testCase.getTestSteps().stream().filter(x -> x instanceof PickleStepTestStep).map(x -> (PickleStepTestStep) x).collect(Collectors.toList());
        TestCaseSteps.remove();
        TestCaseSteps.set(testSteps);
        TotalTestSteps.remove();
        TotalTestSteps.set(testSteps.size());
        StepNumber.remove();
        StepNumber.set(0);

    }

    //	public static void PrintTestCase() {
//
//		String testcaseHTML = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> TEST CASE (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
//		String testcaseHTML2 = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> "+CurrentScenario.getName()+" (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
//		for (PickleStepTestStep ts : TestCaseSteps) {
//			System.out.println(ts.getStep().getKeyWord() + ts.getStep().getText());
//			testcaseHTML+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
//			testcaseHTML2+="<li><span><b>"+ts.getStep().getKeyWord()+"</b> "+ts.getStep().getText()+"</span></li>";
//		}	
//		testcaseHTML+="</ol></details></div>";
//		testcaseHTML2+="</ol></div>";
//
//		final String testcaseHTML3=testcaseHTML2;
//
//		ReportFactory.testInfo(testcaseHTML);
//
//		TestCases.put(CurrentScenario.getName(),new HashMap<String, String>() {{
//			put("TC", testcaseHTML3);
//			put("Status", "PASS");
//		}});
//	}
    public static void PrintTestCase() {

        String testcaseHTML = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> TEST CASE (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        String testcaseHTML2 = "<div style=\"background-color:#E7E9EB;font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> " + CurrentScenario.get().getName() + " (<span style=\"color:#039be5\">Click to View</span>)</summary><ol>";
        for (PickleStepTestStep ts : TestCaseSteps.get()) {
            System.out.println(ts.getStep().getKeyWord() + ts.getStep().getText());
            testcaseHTML += "<li><span><b>" + ts.getStep().getKeyWord() + "</b> " + ts.getStep().getText() + "</span></li>";
            testcaseHTML2 += "<li><span><b>" + ts.getStep().getKeyWord() + "</b> " + ts.getStep().getText() + "</span></li>";
        }
        testcaseHTML += "</ol></details></div>";
        testcaseHTML2 += "</ol></div>";

        final String testcaseHTML3 = testcaseHTML2;

        ReportFactory.testInfo(testcaseHTML);

        TestCases.put(CurrentScenario.get().getName(), new HashMap<String, String>() {{
            put("TC", testcaseHTML3);
            put("Status", "PASS");
        }});
    }

    public static void PrintAllTestCases() {
        ExtentHtmlReporter tcReporter = new ExtentHtmlReporter("target" + "/" + "TestCases" + ".html");
        ExtentReports tcReport = new ExtentReports();
        tcReport.attachReporter(tcReporter);
        ExtentTest tcTest = tcReport.createTest("TEST CASES");

        ExtentTest test = report.createTest("TEST CASES");
        for (String testcase : TestCases.keySet()) {
            if (TestCases.get(testcase).get("Status").equalsIgnoreCase("Pass")) {
                String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: PASSED</summary><ol>";
                test.log(Status.PASS, TestCases.get(testcase).get("TC") + str);
            } else {
                String str = "\n<div style=\"font-family:'Source Sans Pro', sans-serif;\"><details><summary><b> STATUS: FAILED</summary><ol>";
                test.log(Status.FAIL, TestCases.get(testcase).get("TC") + str);
            }
            tcTest.log(Status.INFO, TestCases.get(testcase).get("TC"));
        }
        tcReport.flush();
    }

    //	public static void PrintCurrentStepText() {
//		PickleStepTestStep ts = (PickleStepTestStep) TestCaseSteps.get(StepNumber);
//		ReportFactory.testInfo("<div style=\"background-color:#E7E9EB;\"><p style=\"font-size:15px\"><b>STEP "+(StepNumber+1)+": "+ts.getStep().getKeyWord() + ts.getStep().getText()+"</b></p></div>");
//	}
    public static void PrintCurrentStepText() {
        PickleStepTestStep ts = (PickleStepTestStep) TestCaseSteps.get().get(StepNumber.get());
        ReportFactory.testInfo("<div style=\"background-color:#E7E9EB;\"><p style=\"font-size:15px\"><b>STEP " + (StepNumber.get() + 1) + ": " + ts.getStep().getKeyWord() + ts.getStep().getText() + "</b></p></div>");
    }

    public static void EmailReport() {

        // Create the attachment
        EmailAttachment attachment = new EmailAttachment();
        //attachment.setPath("C:\\Users\\Chandrakant.Shetty\\eclipse-workspace\\NXT_Web/Reports/03182021-1218964/RegressionTestSuite.html");
        //reportFolder = "C:\\Users\\Chandrakant.Shetty\\eclipse-workspace\\NXT_Web/Reports/03182021-1218964";
        //reportFolderName="03182021-1218964";
        attachment.setPath(reportFolder + ".zip");
        attachment.setDisposition(EmailAttachment.ATTACHMENT);
        attachment.setDescription("Test Report Package");
        //attachment.setName("SeleniumTest.html");
        attachment.setName(reportFolderName + ".zip");

        // Create the email message
        HtmlEmail email = new HtmlEmail();
        //MultiPartEmail email = new MultiPartEmail();
        email.setHostName("smtp.gmail.com");
        email.setSmtpPort(465);
        email.setAuthenticator(new DefaultAuthenticator("cbshetty360@gmail.com", "Test@12345$"));
        email.setSSLOnConnect(true);
        try {
            URL LogoURL = new URL("file:///" + System.getProperty("user.dir") + "/src/main/resources/NXT_logo.png");
            String Logocid = email.embed(LogoURL, "Logo");
            email.addTo("chandrakant.shetty1989@gmail.com", "CBS");
            email.addTo("chandrakant.shetty@angelbroking.com", "CBS2");
            email.setFrom("cbshetty9@gmail.com", "Me");
            email.setSubject(testResult + " : Sanity Test [" + reportFolderName + "]");
            //email.setMsg("Please find attached the test report folder!!");
            String htmlhead = "<head><style>table {  font-family: arial, sans-serif;  border-collapse: collapse;  width: 100%;}td, th {  border: 1px solid #dddddd;  text-align: left;  padding: 8px;}tr:nth-child(even) {  background-color: #dddddd;}</style></head>";
            String htmlmssg = "<html>" + htmlhead;
            htmlmssg += "<table><tbody style=\"background: linear-gradient(to right, #25c481, #25b7c4);color: #fff;\"><tr><td colspan=\"3\"><img height=\"25\" width=\"10%\" src=\"cid:" + Logocid + "\"></td></tr>";
            htmlmssg += "<tr><td colspan=\"3\"><b>TEST TYPE : SANITY TEST</b></td></tr>";
            htmlmssg += "<tr><td colspan=\"3\"><b>TEST STATUS : " + testResult + " (" + testStatus.get() + " failures)</b></td></tr>";
            htmlmssg += "<tr><td colspan=\"3\"><b>TEST TIMESTAMP : " + reportFolderName + "</b></td></tr>";
            if (testStatus.get() != 0) {
                htmlmssg += "<tr><td colspan=\"3\">" + "<table><thead><tr><th><b>#</b></th><th><b>Error Details</b></th><th><b>Snapshot</b></th></tr></thead><tbody>";
                int count = 0;
                for (String msg : messageList) {
                    URL url = new URL("file:///" + snapshotsList.get(count));
                    String cid = email.embed(url, msg);
                    htmlmssg += "<tr><td>" + (count + 1) + "</td><td>" + msg + "</td><td>" + "<img width=\"10%\" src=\"cid:" + cid + "\">" + "</td></tr>";
                    count += 1;
                }
                htmlmssg += "</tbody></table></td></tr>";
            }
            htmlmssg += "</tbody></table></html>";
            email.setHtmlMsg(htmlmssg);
            email.setTextMsg("Please find attached the test report folder!!");
            // add the attachment
            email.attach(attachment);

            // send the email
            email.send();
            LogFactory.LogInfo("Report Zipped and Mailed!!");
        } catch (EmailException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (MalformedURLException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static List<String> fileList = new ArrayList<String>();

    public static void ZipDirectory(String dirPath) {
        String zipDirPath = dirPath + ".zip";
        File zipDir = new File(dirPath);
        populateFileList(zipDir.listFiles());
        try {
            LogFactory.LogInfo("Zipping Files.........");
            FileOutputStream fos = new FileOutputStream(zipDirPath);
            ZipOutputStream zos = new ZipOutputStream(fos);
            for (String filepath : fileList) {
                LogFactory.LogInfo("Adding File to Zip: " + filepath);
                ZipEntry ze = new ZipEntry(filepath.substring(dirPath.length() + 1, filepath.length()));
                zos.putNextEntry(ze);
                FileInputStream fis = new FileInputStream(filepath);
                byte[] buffer = new byte[1024];
                int len;
                while ((len = fis.read(buffer)) > 0) {
                    zos.write(buffer, 0, len);
                }
                zos.closeEntry();
                fis.close();
            }
            zos.close();
            fos.close();
            LogFactory.LogInfo("Zipped Results: " + zipDirPath);
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    public static void populateFileList(File[] files) {
        for (File file : files) {
            if (file.isFile()) {
                fileList.add(file.getAbsolutePath());
            } else {
                populateFileList(file.listFiles());
            }
        }
    }

    public static double calculateExecutionTime() {
        long totalExecutionTime = endTime - startTime;
        return totalExecutionTimeInSeconds = totalExecutionTime / 60000.0;
    }

    public static long calculateSuiteExecutionTime() {
        long totalExecutionTime = suiteEndTime - suiteStartTime;
        return totalSuiteExecutionTimeInSeconds = (totalExecutionTime / 1000);
    }

    public static String getCurrentTimestamp() {
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();
        // Define the format for the timestamp
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        // Format the current date and time
        return now.format(formatter);
    }

    public static void postMessageInThread(String channelId, String parentTs, String message) {
        Slack slack = Slack.getInstance();

        try {
            ChatPostMessageResponse response = slack.methods(System.getProperty("SLACK_BOT_TOKEN")).chatPostMessage(req -> req
                    .channel(channelId)
                    .text(message)
                    .threadTs(parentTs) // Specify the parent message's timestamp to post in the thread
            );
            if (response.isOk()) {
                System.out.println("Message posted successfully in thread: " + response.getTs());
            } else {
                System.err.println("Failed to post message: " + response.getError());
            }

        } catch (IOException | SlackApiException e) {
            System.err.println("Error occurred: " + e.getMessage());
        }
    }

    public static void UpdateTestStatusCount(String status) {
        if (status.equalsIgnoreCase("Total")) {
            totalTestsCount.add(1);
        } else if (status.equalsIgnoreCase("Fail")) {
            totalFailTestsCount.add(1);
        } else if (status.equalsIgnoreCase("Pass")) {
            totalPassTestsCount.add(1);
        }
    }


    public static void publishTestResultsToDashboard() {

//        String isCIRun = "false";
//
//        if (System.getProperty("isCIRun") != null) {
//            isCIRun = System.getProperty("isCIRun");
//        }
//
//		String isCron = "false";
//
//		if (System.getProperty("isCron") != null) {
//			isCron = System.getProperty("isCron");
//		}

        String testStatus = System.getProperty("testStatus");

        String isCIExecution = "false";
        if (System.getProperty("isDeploy") != null) {
            isCIExecution = System.getProperty("isDeploy");
        }

        String baseURI = "http://sre-qa-dashboard0.gpx.uat.angelone.in:8080"; // Will be changed after hosting
        String basePath = "/api/publish_test_results.php";
        String reportLink = "";
        if (System.getProperty("ReportLink") != null) {
            reportLink = System.getProperty("ReportLink");
        }

        // Extract total tests
        String totalTests = extractVariable(testStatus, "Total Tests : (\\d+)");
        System.out.println("Total Tests: " + totalTests);

        // Extract passed tests
        String passedTests = extractVariable(testStatus, "Passed : (\\d+)");
        System.out.println("Passed: " + passedTests);

        // Extract duration
        String duration = extractVariable(testStatus, "TimeTaken: ([\\d\\.]+)mins");
        double timeInMinutes = Double.parseDouble(duration);
        double timeInSeconds = timeInMinutes * 60;
        System.out.println("Duration: " + (int) timeInSeconds);

        // Extract Report Name
        String reportName = extractVariable(testStatus, "\\*([^*]*) Test Execution Summary\\*");

        Results results = new Results();
        results.setProjectName(reportName);
        results.setEnvironment("uat");
        results.setPodName(testStatus.contains("MPM") ? "margin" : "nonmargin");
        results.setGroupName(isCIExecution.equalsIgnoreCase("true") ? "ci" : "automation");

        try {
            assert totalTests != null;
            assert passedTests != null;
            results.setDuration((int) timeInSeconds);
            results.setTotalCases(Integer.valueOf(totalTests));
            results.setPassedCases(Integer.valueOf(passedTests));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        results.setResultLink(reportLink);

        System.out.println(results.toString());
        System.out.println(System.getProperty("DASHBOARD_APIKEY"));

//        Response response = RestAssured
//                .given()
//                .baseUri(baseURI)
//                .basePath(basePath)
//                .header("X-API-KEY", System.getProperty("DASHBOARD_APIKEY"))
//                .body(results)
//                .contentType(ContentType.JSON)
//                .post();
//
//        System.out.println("Response ::" + response.getBody().asPrettyString());
//
//        Assert.assertEquals(response.getStatusCode(), 200);

        // Publish to Grafana
        publishToGrafana(
                reportName,
                testStatus.contains("MPM") ? "AROM-Margin" : "AROM-NonMargin",
                Integer.parseInt(totalTests),
                Integer.parseInt(passedTests),
                Integer.parseInt(totalTests) - Integer.parseInt(passedTests),
                (int) timeInMinutes);

    }

    private static void publishToGrafana(String featureName, String teamName,
                                         int totalTests, int passedTests,
                                         int failedTests, int duration) {

        try {
            boolean dataExists = checkIfDataExists(featureName, teamName);

            if (dataExists) {
                System.out.println("Updating the data as it already exists");
                updateTestResults(featureName, teamName, totalTests, passedTests, failedTests, duration);
            } else {
                insertTestResults(featureName, teamName, totalTests, passedTests, failedTests, duration);
            }

        } catch (SQLException sqlException) {
            sqlException.printStackTrace();
        }
    }

    private static boolean checkIfDataExists(String featureName, String teamName) throws SQLException {
        String selectQuery = "SELECT COUNT(*) FROM test_suite_summary WHERE teamname = ? AND testfeaturename = ? AND entry_date = CURRENT_DATE";

        PreparedStatement preparedStatement = DatabaseManager.getInstance().getConnection().prepareStatement(selectQuery);

        // Set parameters for the SELECT query
        preparedStatement.setString(1, teamName);
        preparedStatement.setString(2, featureName);

        // Execute the query
        ResultSet resultSet = preparedStatement.executeQuery();

        // Check if a matching record exists
        if (resultSet.next()) {
            int count = resultSet.getInt(1);
            System.out.println("Data Entry Found on DB");
            return count > 0; // If count > 0, data exists, otherwise it doesn't
        }
        return false; // Default return false if something goes wrong
    }


    public static void insertTestResults(String featureName, String teamName, int totalTests, int passedTests, int failedTests, int duration) throws SQLException {
        String insertQuery = "INSERT INTO test_suite_summary (testfeaturename, teamname, totaltests, passedtests, failedtests, testduration, entry_date) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        PreparedStatement preparedStatement = DatabaseManager.getInstance().getConnection().prepareStatement(insertQuery);
        preparedStatement.setString(1, featureName);
        preparedStatement.setString(2, teamName);
        preparedStatement.setInt(3, totalTests);
        preparedStatement.setInt(4, passedTests);
        preparedStatement.setInt(5, failedTests);
        preparedStatement.setInt(6, duration);
        preparedStatement.setDate(7, java.sql.Date.valueOf(LocalDate.now()));

        int rowsInserted = preparedStatement.executeUpdate();
        System.out.println("Rows inserted: " + rowsInserted);
    }

    // Method to update test results if data already exists (on conflict)
    public static void updateTestResults(String featureName, String teamName, int totalTests, int passedTests, int failedTests, int duration) throws SQLException {
        String updateQuery = "UPDATE test_suite_summary "
                + "SET totaltests = ?, passedtests = ?, failedtests = ?, testduration = ? "
                + "WHERE teamname = ? AND testfeaturename = ? AND entry_date = ?";

        PreparedStatement preparedStatement = DatabaseManager.getInstance().getConnection().prepareStatement(updateQuery);
        preparedStatement.setInt(1, totalTests);
        preparedStatement.setInt(2, passedTests);
        preparedStatement.setInt(3, failedTests);
        preparedStatement.setInt(4, duration);
        preparedStatement.setString(5, teamName);
        preparedStatement.setString(6, featureName);
        preparedStatement.setDate(7, java.sql.Date.valueOf(LocalDate.now())); // Setting current date

        int rowsUpdated = preparedStatement.executeUpdate();
        System.out.println("Rows updated: " + rowsUpdated);
    }

    private static String extractVariable(String input, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(input);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return null;
    }

    public static void SetSlackDetails(String channelID, String reportName, String environmentURL) {
        ChannelID=channelID;
        ReportName=reportName;
        Environment=environmentURL;
        ReportLink="";
    }

    //Response Message Map
    public static Map<String,List<String>> ResponseMap;
    public static String ResponseMapTest;
    public static String ResponseMapMessage;

    public static void SetResponseMap(String test, String response) {
        ResponseMapTest=test;
        ResponseMapMessage=response;
        UpdateResponseMap(ResponseMapTest,ResponseMapMessage);
    }

    public static void UpdateResponseMap(String test, String response) {
        if(ResponseMap.keySet().contains(test)) {
            List<String> respList = new ArrayList<String>();
            for(String resp:ResponseMap.get(test)) {
                respList.add(resp);
            }
            respList.add(response);
            ResponseMap.put(test, respList);
        }else {
            ResponseMap.put(test, Arrays.asList(response));
        }
    }

    public static void createFailedTestCaseReport() {
//		ChannelID="C06K6S65744";
        System.setProperty("channelID",ChannelID);
        try{
            SlackUtil slack = new SlackUtil(ChannelID);
            String mentions = "";
            String message="";
            int cnt=0;
            List<String> blocks = new ArrayList<String>();
            if(totalFailTests!=0) {
                message = "*Total Tests : " + totalTests + "*," + " *Passed : " + totalPassTests + "*," + " >*Failed : " + totalFailTests + "*," + " >*Test Report :*  _See Next Bot Message_ *,"+ " >*Failed Tests* ";

                for (String key : FailTests.keySet()) {
                    message=message+" * "+key+" - "+FailTests.get(key)+" failure(s)_";
                }

            }else{
                message="NO ERROR";
            }
            String tempfileName = sdt.format(new Date()) + ".xlsx";
            ExcelUtil.CreateWorkbook("src/test/resources/" + tempfileName);
            ExcelUtil blocksXl = new ExcelUtil("src/test/resources/" + tempfileName);
            blocksXl.getWorbook().createSheet("Blocks");
            blocksXl.setAvtiveSheet("Blocks");
            blocksXl.setParam(0, 0, message);
            blocksXl.setParam(1, 0, ChannelID);
            blocksXl.CopyWorkbook("src/test/resources/Test_Status.xlsx");
            blocksXl.closeWorkbook();

            //save test status text to file
            File file1 = new File("src/test/resources/Test_Status_Text.txt");
            FileWriter myWriter = null;
            try {
                myWriter = new FileWriter("src/test/resources/Test_Status_Text.txt");
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            try {
                myWriter.write(message);
                myWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }catch (Exception e){
            System.out.print("error"+e.getMessage());
        }
    }

}
