package com.bdd.base;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import com.api.reporting.ReportFactory;
//import io.cucumber.core.internal.gherkin.ast.Scenario;
import io.cucumber.java.After;
import io.cucumber.java.AfterStep;
import io.cucumber.java.Before;
import io.cucumber.java.BeforeStep;
import io.cucumber.java.Scenario;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import net.minidev.json.JSONArray;


public class API_StepDefs {

	@Before
	public void before(Scenario scn) {
		if(ReportFactory.reporting) {
			ReportFactory.StartTest(scn.getName());
			try {
				ReportFactory.SetTestCase(scn);
				ReportFactory.PrintTestCase();
			} catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			Collection<String> tags = scn.getSourceTagNames();
			ReportFactory.AssignCategories(tags);
		}	
	}
	@BeforeStep
	public void beforeStep(Scenario scenario) throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException{
		if(ReportFactory.reporting) {
			ReportFactory.PrintCurrentStepText();
		}
	}


	@AfterStep
	public void afterStep() {
		if(ReportFactory.reporting) {
			//ReportFactory.StepNumber++;
			int StepNum = ReportFactory.StepNumber.get()+1;
			ReportFactory.StepNumber.set(StepNum);
		}
	}

	@Given("Wait for {int} ms([^\\\"]*)")
	public void wait_for_after_placing_order(Integer time_ms) {
		try {
			Thread.sleep(time_ms);
			//Thread.sleep(time_min);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Given("Wait for {int} sec([^\\\"]*)")
	public void wait_for_sec_after_placing_order(Integer time_sec) {
		try {
			Thread.sleep(time_sec*1000);
			//Thread.sleep(time_min);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Given("Wait for {int} min([^\\\"]*)")
	public void wait_for_min_after_placing_order(Integer time_min) {
		try {
			Thread.sleep(time_min*1000*60);
			//Thread.sleep(time_min);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@When("Application calls the {string} API")
	public void application_calls_the_get_ipo_master_api(String API) {
		Parallel_BaseClass.getAPIBaseClass().API_Setup(API,Parallel_BaseClass.getAPIBaseClass().getConstantsClassObject());
		Parallel_BaseClass.getAPIBaseClass().Send_API_Request();
	}

	@When("Application calls the {string} API with invalid JWT {string}")
	public void application_calls_the_with_invalid_JWT(String API, String token) {
		Parallel_BaseClass.getAPIBaseClass().API_Setup(API,Parallel_BaseClass.getAPIBaseClass().getConstantsClassObject());
		Parallel_BaseClass.getAPIBaseClass().addAPIRequestHeader("Authorization", "Bearer "+token);
		Parallel_BaseClass.getAPIBaseClass().Send_API_Request(); 
	}
	@When("Application calls the {string} API with parameters {string}")
	public void application_calls_the_API_with_query_parameters(String API, String queryParamValues) {
		Parallel_BaseClass.getAPIBaseClass().API_Setup(API,Parallel_BaseClass.getAPIBaseClass().getConstantsClassObject());
		Parallel_BaseClass.getAPIBaseClass().Send_API_Request(queryParamValues.split(","));  	
	}
	@Then("API response status is {int}")
	public void api_response_status_is(Integer statusCode) {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseStatus(statusCode);
	}
	@Then("WS response status is {int}")
	public void ws_response_status_is(Integer statusCode) {
		Parallel_BaseClass.getAPIBaseClass().Validate_WebSocketResponseStatus(statusCode);
	}
	@Then("API returns the expected reponse")
	public void api_returns_the_expected_reponse() {
		//Parallel_BaseClass.getAPIBaseClass().Validate_APIResponse();
		Parallel_BaseClass.getAPIBaseClass().validateJsonSchema();
	}

	@Then("WS Response pong")
	public void ws_response_pong() {
		if(Parallel_BaseClass.getAPIBaseClass().getWSResponse().equalsIgnoreCase("pong")) {
			ReportFactory.PassTest("SUCCESS :: WS returned pong");
		} else {
			ReportFactory.FailTest("FAILURE :: WS did not returned pong");
		}
	}
	@Then("API resppnse is null")
	public void api_resppnse_is_null() {
		if(Parallel_BaseClass.getAPIBaseClass().getAPIResponse().equalsIgnoreCase("null")) {
			ReportFactory.PassTest("SUCCESS :: API returned the expected reponse.");
		}else {
			ReportFactory.FailTest("FAILURE :: API did not retur the expected reponse.");
		}
	}

	@Then("API reponse contains the expected headers - {string}")
	public void api_reponse_contains_the_expected_headers(String headers) {
		for(String header:headers.split(",")) {
			Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseHeader(header);
		}
	}
	@Then("API reponse contains the expected headers")
	public void api_reponse_contains_the_expected_headers() {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseHeaders();
	}

	@Then("API reponse contains the expected parameters")
	public void api_reponse_contains_the_expected_paramters() {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseParamters();
	}

	@Then("API reponse contains the expected parameters - {string}")
	public void api_reponse_contains_the_expected_paramters(String params) {
		for(String param:params.split(",")) {
			Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody(param);
		}
	}

	@Then("API repsonse time is below {int} ms")
	public void api_repsonse_time_is_below_ms(int millis) {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseTime(millis);
		//ReportFactory.endTest();
		//ReportFactory.EndReport();
	}
	@Then("API returns {string} in reponse")
	public void api_returns_in_reponse(String text) {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBodyContains(text);
	}

	@Then("API returns an error in reponse")
	public void api_return_an_error_in_reponse() {
		Parallel_BaseClass.getAPIBaseClass().validateJsonSchema("AMXError_Schema");
	}

	@Then("API returns an error reponse in Reports")
	public void api_return_an_error_reponse_in_reports() {
		Parallel_BaseClass.getAPIBaseClass().validateJsonSchema("AMXError_Schema_Null");
	}

	@Then("Symbol Detail API returns error")
	public void api_return_an_error_in_symbol_detail_response() {
		Parallel_BaseClass.getAPIBaseClass().validateJsonSchema("AMXError_SymbolDetail");
	}

	@Then("Value of parameter {string} is {string}")
	public void value_of_paramter_is(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody(parameter, value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is "+value);
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not "+value);
		}
	}

	@Then("Value of paramter {string} is {string} or {string}")
	public void value_of_paramter_is_or(String parameter, String value1, String value2) {
		if(Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody(parameter, value1)) {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is "+value1);
		}else if(Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody(parameter, value2)) {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is "+value2);
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not "+value1+" or "+value2);
		}
	}

	@Then("Value of string parameter {string} is {string}")
	public void value_of_string_parameter_is(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of boolean parameter {string} is {string}")
	public void value_of_boolean_parameter_is(String parameter, String value) {	
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, Boolean.valueOf(value))){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of byte parameter {string} is {int}")
	public void value_of_byte_parameter_is(String parameter, Integer value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of integer parameter {string} is {int}")
	public void value_of_integer_parameter_is(String parameter, Integer value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of long parameter {string} is {string}")
	public void value_of_long_parameter_is(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, Long.valueOf(value))){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of float parameter {string} is {double}")
	public void value_of_float_parameter_is(String parameter, Double value) {
		if(Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody(parameter, value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of string parameter {string} is not {string}")
	public void value_of_string_parameter_is_not(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}

	}

	@Then("Value of boolean parameter {string} is not {string}")
	public void value_of_boolean_parameter_is_not(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, Boolean.valueOf(value))){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of integer parameter {string} is not {int}")
	public void value_of_integer_parameter_is_not(String parameter, Integer value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of float parameter {string} is not {double}")
	public void value_of_float_parameter_is_not(String parameter, Double value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of long parameter {string} is not {string}")
	public void value_of_long_parameter_is_not(String parameter, String value) {
		if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, Long.valueOf(value))){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is "+value);
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not "+value);
		}
	}



	@Then("Value of parameter {string} is null")
	public void value_of_parameter_is_null(String parameter) {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody_ParameterIsNull(parameter);
	}

	@Then("Value of parameter {string} is not null")
	public void value_of_parameter_is_not_null(String parameter) {
		Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody_ParameterIsNotNull(parameter);
	}

	@Then("Value of parameters {string} is not null")
	public void value_of_parameters_is_not_null(String paramList) {
		for(String parameter:paramList.split(",")) {
			Parallel_BaseClass.getAPIBaseClass().Validate_APIResponseBody_ParameterIsNotNull(parameter);
		}
	}

	@Then("Value of parameters {string} is not empty")
	public void value_of_parameters_is_not_empty(String paramList) {
		for(String parameter:paramList.split(",")) {
			String value="";
			if(Parallel_BaseClass.getAPIBaseClass().AssertParameterValue(parameter, "")){
				ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
			}else {
				ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
			}
		}
	}


	@After
	public void after() {
		if(ReportFactory.reporting) {
			ReportFactory.endTest();
		}
	}

	@Given("Report is published on slack")
	public void report_is_published_on_slack() {
		ReportFactory.PublishReportOnSlack2();
	}

	@Given("Report is published on slack thread")
	public void report_is_published_on_slack_thread() {
		ReportFactory.PublishReportOnSlackThread2();
	}

	@Given("Start Scenario {string}")
	public void start_Scenario(String Scenario) {
	    Parallel_BaseClass.SetScenarioStatus(Scenario, "STARTED");
	}
	
	@Given("End Scenario {string}")
	public void end_Scenario(String Scenario) {
		Parallel_BaseClass.SetScenarioStatus(Scenario, "COMPLETED");
	}
	
	@Given("Status of Scenario {string} is set to {string}")
	public void status_of_Scenario_is_set_to(String Scenario, String Status) {
		Parallel_BaseClass.SetScenarioStatus(Scenario, Status);
	}

	@Given("Wait until status of scenario {string} is {string}")
	public void wait_until_status_of_scenario_is(String Scenario, String Status) throws InterruptedException {
		int StatusMatched=0;
	    for(int i=0;i<3;i++) {
	    	if(Parallel_BaseClass.GetScenarioStatus(Scenario).equals(Status)) {
	    		StatusMatched=1;
	    		ReportFactory.PassTest("INFO :: Status of Scenario"+Scenario+" is "+Status);
	    		break;
	    	}
	    	Thread.sleep(1000);
	    	System.out.println("Waiting for status of scenario "+Scenario+" to update, Current Status is '"+Parallel_BaseClass.GetScenarioStatus(Scenario)+"'");
	    }
	    if(StatusMatched==0) {
	    	ReportFactory.testInfo("INFO :: Status of Scenario"+Scenario+" is not "+Status);
	    }
	}
	
	//Commmon Step Defs
	
	@Then("API returns an error in response")
	public void api_returns_an_error_in_response() {
		Parallel_BaseClass.getAPIBaseClass().validateJsonSchema(Parallel_BaseClass.getAPIBaseClass().API_Name+"_Error_Schema");
	}

	@Then("API returns an empty response")
	public void api_returns_an_empty_response() {
		String response = Parallel_BaseClass.getAPIBaseClass().getAPIResponse();
		if(response.equals("{}")) {
			ReportFactory.PassTest("SUCCESS :: API returned and empty response");
		}else{
			ReportFactory.FailTest("FAILURE :: API did not return an empty response");
		}
	}

	@Then("Value of string json parameter {string} is {string}")
	public void value_of_string_json_parameter_is(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		String objValue = (String) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue.equals(value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of boolean json parameter {string} is {string}")
	public void value_of_boolean_json_parameter_is(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		Boolean objValue =  (Boolean) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(String.valueOf(objValue).equals(value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of integer json parameter {string} is {int}")
	public void value_of_integer_json_parameter_is(String parameter, Integer value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		int objValue = (Integer) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue==value){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of Long json parameter {string} is {string}")
	public void value_of_Long_json_parameter_is(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		long objValue = (Long) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue==Long.valueOf(value)){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}
	@Then("Value of double json parameter {string} is {double}")
	public void value_of_double_parameter_is(String parameter, Double value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		double objValue = Double.valueOf(String.valueOf(Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter))).doubleValue();
		if(objValue==value.doubleValue()){
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	
	@Then("Value of string json parameter {string} is not {string}")
	public void value_of_string_json_parameter_is_not(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		String objValue = (String) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue.equals(value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of boolean json parameter {string} is not {string}")
	public void value_of_boolean_json_parameter_is_not(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		Boolean objValue =  (Boolean) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(String.valueOf(objValue).equals(value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of integer json parameter {string} is not {int}")
	public void value_of_integer_json_parameter_is_not(String parameter, Integer value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		int objValue = (Integer) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue==value){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}

	@Then("Value of long json parameter {string} is not {string}")
	public void value_of_long_json_parameter_is_not(String parameter, String value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		long objValue =(Long) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue==Long.valueOf(value)){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}
	
	@Then("Value of double json parameter {string} is not {double}")
	public void value_of_double_parameter_is_not(String parameter, Double value) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		double objValue = Double.valueOf(String.valueOf(Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter))).doubleValue();
		if(objValue==value.doubleValue()){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is \""+value+"\"");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not \""+value+"\"");
		}
	}


	@Then("Value of json parameter {string} is not null")
	public void value_of_json_parameter_is_not_null(String parameter) {
		if(parameter.charAt(0)!='$') {
			parameter="$."+parameter;
		}
		Object objValue = Parallel_BaseClass.getAPIBaseClass().getJsonParameter(parameter);
		if(objValue==null){
			ReportFactory.FailTest("FAILURE :: Value of parameter "+parameter+" is null");
		}else {
			ReportFactory.PassTest("SUCCESS :: Value of parameter "+parameter+" is not null");
		}
	}

	@Then("Array parameter {string} returns {int} items")
	public void array_parameter_returns_items(String paramName, Integer paramCount) {
		if(paramName.charAt(0)!='$') {
			paramName="$."+paramName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(paramName);
		int found=0;
		for(Object jo : array) {
			JSONArray ja = (JSONArray) jo;
			if(ja.size()!=paramCount) {
				found++;
			}
		}
		if(found==0) {
			ReportFactory.PassTest("SUCCESS :: Array parameter "+paramName+" has "+paramCount+" items");
		}else {
			ReportFactory.FailTest("FAILURE :: Array parameter "+paramName+" does not have "+paramCount+" items for all isntances");
		}
	}
	
	@Then("Parameter {string} has {int} item\\(s)")
	public void parameter_has_item_s(String paramName, Integer paramCount) {
		if(paramName.charAt(0)!='$') {
			paramName="$."+paramName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(paramName);
		if(array.size()==paramCount) {
			ReportFactory.PassTest("SUCCESS :: Array parameter "+paramName+" returned "+array.size()+" items [Expected : "+paramCount+"]");
		}else {
			ReportFactory.FailTest("FAILURE :: Array parameter "+paramName+" returned "+array.size()+" items [Expected : "+paramCount+"]");
		}
	}

	@Then("For items in array parameter {string} if {string} is {string} then {string} is {string}")
	public void for_items_in_array_parameter_if_is_then_is(String arrayParamName, String ifParamName, String ifParamvalue, String thenParamName, String thenParamValue) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName+"[?(@."+ifParamName+"==\""+ifParamvalue+"\")]");
		int count=0;
		for(Object o:array) {
			Map<String,Object> m = (Map<String, Object>) o;
			if(String.valueOf(m.get(thenParamName)).equals(thenParamValue)) {
				count++;
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: For items in array parameter "+arrayParamName+", if "+ifParamName+" is "+ifParamvalue+" then "+thenParamName+" is "+thenParamValue);
		}else {
			ReportFactory.FailTest("FAILURE :: For "+count+" item(s in array parameter "+arrayParamName+", if "+ifParamName+" is "+ifParamvalue+" then "+thenParamName+" is "+thenParamValue);
		}
	}

	@Then("Value of string json parameter {string} contains current date in one of the formats {string}")
	public void value_of_string_json_parameter_contains_current_date_in_one_of_the_formats(String paramName, String dateFormats) {
		if(paramName.charAt(0)!='$') {
			paramName="$."+paramName;
		}
		//		if(paramName.charAt(paramName.length()-1)!='$') {
		//			paramName=paramName+".*";
		//		}
		List<String> dates = new ArrayList<String>();
		for(String s:dateFormats.split(",")) {
			dates.add((new SimpleDateFormat(s)).format(Calendar.getInstance(TimeZone.getTimeZone("Asia/Kolkata")).getTime()));
		}
		List<String> fileNames = new ArrayList<String>();
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(paramName);
		int count=0;
		for(Object o:array) {
			String s = (String) o; 
			int found=0;
			for(String date:dates) {
				found=(!s.contains(date))?(found+1):found;
			}
			if(found==0) {
				count++;
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: All instances for json parameter "+paramName+" contain current date in the expected format");
		}else {
			ReportFactory.FailTest("FAILURE :: "+count+" instances for json parameter "+paramName+" donot contain current date in the expected format");
			ReportFactory.testInfo("<a><details><summary>FIles names without current date(click to view)</summary><font color=black>"+fileNames.toString()+"</font></details></a>");
		}
	}

	@Then("Array parameter {string} has items with paramter {string} equal to {string}")
	public void array_parameter_has_items_with_paramter_equal_to(String arrayParamName, String itemParamName, String itemParamValues) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		List<String> itemparamActualValues = new ArrayList<String>();
		for(Object o:array) {
			Map<String,Object> m = (Map<String, Object>) o;
			itemparamActualValues.add(String.valueOf(m.get(itemParamName)));
		}
		int count=0;
		for(String value:itemParamValues.split(",")) {
			if(!itemparamActualValues.contains(value)) {
				ReportFactory.FailTest("FAILURE :: Item with \""+itemParamName+"\" equal to \""+value+" is not present");
				count++;
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: All expected items are present");
		}
	}
	
	@Then("Array parameter {string} has items with value of Double paramter {string} equal to {double}")
	public void array_parameter_has_items_with_value_of_Double_paramter_equal_to(String arrayParamName, String itemParamName, Double itemParamValue) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		List<Double> itemparamActualValues = new ArrayList<Double>();
		for(Object o:array) {
			Map<String,Object> m = (Map<String, Object>) o;
			itemparamActualValues.add(Double.valueOf(String.valueOf(m.get(itemParamName))));
		}
		int count=0;
		for(Double value:itemparamActualValues) {
			if(!(value.doubleValue()==itemParamValue.doubleValue())) {
				//ReportFactory.FailTest("FAILURE :: Item with \""+itemParamName+"\" equal to \""+value+" is not present");
				count++;
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: The given parameter has the expected value for all items in the array");
		}else {
			ReportFactory.FailTest("FAILURE :: The given parameter does not have the expected value for "+count+" items in the array");
		}
	}

	@Then("Array parameter {string} does not have parameter\\(s) {string}")
	public void array_parameter_does_not_have_parameter_s(String arrayParamName, String paramNames) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		int items = 0;
		for(Object o:array) {
			Map<String,Object> m = (Map<String, Object>) o;
			int count=0;
			for(String param:paramNames.split(",")) {
				if(m.containsKey(param)) {
					count++;
				}
			}
			if(count!=0) {
				items++;
			}
		}
		if(items==0) {
			ReportFactory.PassTest("SUCCESS :: Array parameter "+arrayParamName+" does not have parameter(s) "+paramNames);
		}else {
			ReportFactory.FailTest("FAILURE :: Array parameter "+arrayParamName+" has parameter(s) "+paramNames);
		}
	}

	@Then("Array parameter {string} has parameter\\(s) {string}")
	public void array_parameter_has_parameter_s(String arrayParamName, String paramNames) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		int items = 0;
		for(Object o:array) {
			Map<String,Object> m = (Map<String, Object>) o;
			int count=0;
			for(String param:paramNames.split(",")) {
				if(!m.containsKey(param)) {
					count++;
				}
			}
			if(count!=0) {
				items++;
			}
		}
		if(items==0) {
			ReportFactory.PassTest("SUCCESS :: Array parameter "+arrayParamName+" has parameter(s) "+paramNames);
		}else {
			ReportFactory.FailTest("FAILURE :: Array parameter "+arrayParamName+" does not have parameter(s) "+paramNames+" for "+items+" item(s)");
		}
	}

	@Then("Array parameter {string} has value\\(s) {string}")
	public void array_parameter_has_values(String arrayParamName, String items) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		int notFound=0;
		List<String> actualItems = new ArrayList<String>();
			for(Object o:array) {
				JSONArray j = (JSONArray) o;
				for(Object o2:j) {
					actualItems.add(String.valueOf(o2));
				}
				for(String item:items.split(",")) {
					if(!actualItems.contains(item)) {
						notFound++;
						//ReportFactory.FailTest("FAILURE :: \""+item+"\" not present");
					}
				}
			}	

		if(notFound==0) {
			ReportFactory.PassTest("SUCCESS :: All expected values are present");
		}else {
			ReportFactory.FailTest("FAILURE :: All expected values are not present for one or more items");
		}
	}
	
	@Then("Array parameter {string} does not have value\\(s) {string}")
	public void array_parameter_does_not_have_value_s(String arrayParamName, String paramValues) {
		if(arrayParamName.charAt(0)!='$') {
			arrayParamName="$."+arrayParamName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(arrayParamName);
		int Found=0;
		List<String> actualItems = new ArrayList<String>();
			for(Object o:array) {
				JSONArray j = (JSONArray) o;
				for(Object o2:j) {
					actualItems.add(String.valueOf(o2));
				}
				for(String item:paramValues.split(",")) {
					if(actualItems.contains(item)) {
						Found++;
					}
				}
			}	

		if(Found==0) {
			ReportFactory.PassTest("SUCCESS :: Value(s) "+paramValues+" not found");
		}else {
			ReportFactory.FailTest("FAILURE  :: Value(s) "+paramValues+" found for one or more instances");
		}
	}

	
	@Then("Parameter {string} has value\\(s) {string}")
	public void parameter_has_values(String paramName, String paramValues) {
		if(paramName.charAt(0)!='$') {
			paramName="$."+paramName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(paramName);
		int notFound=0;
		List<String> actualItems = new ArrayList<String>();
		for(Object o:array) {
			actualItems.add(String.valueOf(o));
		}
		for(String item:paramValues.split(",")) {
			if(!actualItems.contains(item)) {
				notFound++;
				ReportFactory.FailTest("FAILURE :: \""+item+"\" not present");
			}
		}
		if(notFound==0) {
			ReportFactory.PassTest("SUCCESS :: All expected values are present");
		}	
	}
	
	@Then("Parameter {string} does not have value\\(s) {string}")
	public void parameter_does_not_have_value_s(String paramName, String paramValues) {
		if(paramName.charAt(0)!='$') {
			paramName="$."+paramName;
		}
		JSONArray array = (JSONArray) Parallel_BaseClass.getAPIBaseClass().getJsonParameter(paramName);
		int found=0;
		List<String> actualItems = new ArrayList<String>();
		for(Object o:array) {
			actualItems.add(String.valueOf(o));
		}
		for(String item:paramValues.split(",")) {
			if(actualItems.contains(item)) {
				found++;
				ReportFactory.FailTest("FAILURE :: \""+item+"\" is present");
			}
		}
		if(found==0) {
			ReportFactory.PassTest("SUCCESS :: All expected values are present");
		}
	}
	
	@Then("API response matches schema {string}")
	public void api_response_matches_schema(String SchemaFileName) {
	    Parallel_BaseClass.getAPIBaseClass().validateJsonSchema(SchemaFileName);
	}
	
	
}
