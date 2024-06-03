package parallel;

import com.dbUtils.InsertTestResultToDB;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;

import com.api.base.API_BaseClass_V2;
import com.api.reporting.ReportFactory;
import com.bdd.base.Parallel_BaseClass;
import com.data.Arom_Constants;

import io.cucumber.testng.AbstractTestNGCucumberTests;
import io.cucumber.testng.CucumberOptions;

import java.sql.SQLException;

@CucumberOptions(features= {"src/test/resources/parallel/"},
glue={"com.bdd.base","parallel"},
monochrome=true,
plugin =
{
		"pretty" ,
		"junit:target/bdd_report.xml",
		"json:target/bdd_report.json",
		"html:target/bdd_htmlreport"/*,
		"com.api.reporting.TestCaseListener",
		"com.aventstack.extentreports.cucumber.adapter.ExtentCucumberAdapter:"*/
},
dryRun=false,
tags = "@SPAN"
		)
public class ParallelTestRunner extends AbstractTestNGCucumberTests{
	
	@BeforeClass
	public static void setup() {
		if(ReportFactory.reporting) {
			ReportFactory.StartReport("TestExtent");
			ReportFactory.setCucumberReports();
			ReportFactory.Setup_SlackIntegration();
		}
	}

	@AfterClass
	public static void tearDown() throws SQLException {
		if(ReportFactory.reporting) {
			//ReportFactory.PrintAllTestCases();
			ReportFactory.EndReport();
			ReportFactory.saveCucumberReports();
			ReportFactory.UploadReportToFileIO();
			ReportFactory.PublishReportOnSlack();
			InsertTestResultToDB.insertAutomationTestResults();
		}
	}
	
	@BeforeMethod
	public static void ScenarioSetup() {
		System.out.println("Scenario Setup");
		Parallel_BaseClass.SetAPIBaseClass(new API_BaseClass_V2());
		Constants.SetAPIConstants(new Arom_Constants());
	}
	
	@AfterMethod
	public static void ScenarioTearDown() {
		System.out.println("Scenario Tear Down");
		Parallel_BaseClass.RemoveAPIBaseClass();
		Constants.RemoveAPIConstants();
	}
	
	@Override
	@DataProvider(parallel=true)
	public Object[][] scenarios(){
		return super.scenarios();
	}

}
