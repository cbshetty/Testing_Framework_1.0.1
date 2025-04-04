package com.api.base;

import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static uk.org.webcompere.modelassert.json.JsonAssertions.assertJson;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONTokener;
import org.json.simple.JSONObject;

import com.api.logging.LogFactory;
import com.api.reporting.ReportFactory;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.jayway.jsonpath.PathNotFoundException;
import com.neovisionaries.ws.client.WebSocketException;

import io.restassured.RestAssured;
import io.restassured.http.Header;
import io.restassured.http.Headers;
import io.restassured.http.Method;
import io.restassured.module.jsv.JsonSchemaValidationException;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

public class API_BaseClass_V2 {

	public String API_Name;
	public Object ConstantClassObject;
	public Boolean silent;
	public RequestSpecification apiRequest;
	public String apiRequestEndPoint;
	public Response apiResponse;
	public JSONObject apiRequestBody;
	public String apiRequestBodyString;
	public String apiListenText;
	public boolean apiRequestHasListenText;
	private LinkedList<String> apiRequestStringList=null;
	public Boolean apiRequestHasBody;
	public Headers apiRequestHeaders;
	public Boolean apiRequestHasHeaders;
	public Method GET = Method.GET;
	public Method POST = Method.POST;
	public Method PUT = Method.PUT;
	public Method PATCH = Method.PATCH;
	public Method DELETE = Method.DELETE;
	public Method method;
	public HashMap<String,String> queryParam;
	public Boolean apiRquestHasQueryParam;
	public HashMap<Integer,String> pathParams;
	public Boolean apiRquestHasPathParam;
	public Headers apiResponseHeaders;
	public int apiStatusCode;
	public JsonPath apiResponseJsonPath;
	public List<String> apiResponseParameters;
	public List<String> apiResponseHeaderNames;
	public Object BaseURI;
	public String APIType="REST";
	//	public WebSocket ws;
	private String wsResponse=null;
	private String apiResponseStatus;
	private String apiResponseString=null;
	private Object apiResponseObject=null;
	private LinkedList<Object> apiResponsePongList=null;
	private LinkedList<String> apiResponseStringList=null;
	private LinkedList<Object> apiResponseObjectList=null;
	private String wsReqestURI;
	private String wsRequestBody;


	private WebSocketListenerImpl sparkListenerImpl;
	private WebSocket_Client sparkTicker;
	private String EndPoint;

	public Object PojoMapClassObject;

	public void setConstantsClassObject(Object constantsClassObject) {
		ConstantClassObject=constantsClassObject;
	}
	public Object getConstantsClassObject() {
		return ConstantClassObject;
	}

	public void API_Setup(String APIName,Object ContantClassInstance, Boolean ...silentrun) {	
		//RestAssured.baseURI = baseURL;
		API_Name=APIName;
		ConstantClassObject=ContantClassInstance;

		//Set Env URL if passed in the mvn command
		/*if(System.getProperty("EnvURL")!=null) {
			RestAssured.baseURI=System.getProperty("EnvURL");
		}else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName+"_BaseURI");
		}*/
		if(System.getProperty("Env")!=null) {
			if(Get_API_Constants(APIName+"_BaseURI_"+System.getProperty("Env").toUpperCase())!=null) {
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI_"+System.getProperty("Env").toUpperCase());
			}else if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else if(Get_API_Constants("BaseURI_"+System.getProperty("Env").toUpperCase())!=null){
				this.BaseURI=(String) Get_API_Constants("BaseURI_"+System.getProperty("Env").toUpperCase());
			}else {
				this.BaseURI=(String) System.getProperty("EnvURL");
			}
		}else if(System.getProperty("EnvURL")!=null)  {
			if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else {
				this.BaseURI=(String) System.getProperty("EnvURL");
			}
		}else {
			if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else {
				this.BaseURI=(String) Get_API_Constants("BaseURI");
			}
		}

		//reset base URL if changed
//		BaseURI = this.BaseURI;
		if(this.BaseURI==null) {
			ReportFactory.FailTest("FAILURE :: Base URL not set for API : "+API_Name);
		}

		//Set APIType
		if(BaseURI.toString().contains("wss://")||BaseURI.toString().contains("ws://")) {
			APIType = "WS";
		}else {
			APIType = "REST";
		}

		//set silent variable if API request/response details need not be published
		if(silentrun.length>0) {
			silent=silentrun[0];
		}else {
			silent=false;
		}

		//Intialize API constant

		//REST API
		apiRequest = RestAssured.given().baseUri((String) this.BaseURI);
		apiRequestBody = new JSONObject();
		apiRequestBodyString="";
		apiRequestHeaders= new Headers();
		apiRequestHasBody=false;
		apiRequestHasHeaders= false;
		method=null;
		apiRequestEndPoint="";
		apiResponseHeaders=null;
		apiResponseJsonPath=null;
		apiResponseParameters=(List<String>) Get_API_Constants(APIName+"_RespParams");
		apiResponseHeaderNames=(List<String>) Get_API_Constants(APIName+"_RespHeaders");

		//WS Setup
		EndPoint = (String)Get_API_Constants(APIName+"_EndPoint");
		sparkListenerImpl = new WebSocketListenerImpl();
		sparkTicker = new WebSocket_Client(sparkListenerImpl,(String)BaseURI);
		apiRequestStringList = new LinkedList<String>();
		apiResponsePongList = new LinkedList<Object>();
		apiResponseStringList= new LinkedList<String>();
		apiResponseObjectList=new LinkedList<Object>();
	}

	public void API_Setup(String APIName,Object ContantClassInstance, Object PojoMapClassInstance, Boolean ...silentrun) {	
		//RestAssured.baseURI = baseURL;
		API_Name=APIName;
		ConstantClassObject=ContantClassInstance;
		PojoMapClassObject = PojoMapClassInstance;

		//Set Env URL if passed in the mvn command
		if(System.getProperty("Env")!=null) {
			if(Get_API_Constants(APIName+"_BaseURI_"+System.getProperty("Env").toUpperCase())!=null) {
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI_"+System.getProperty("Env").toUpperCase());
			}else if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else if(Get_API_Constants("BaseURI_"+System.getProperty("Env").toUpperCase())!=null){
				this.BaseURI=(String) Get_API_Constants("BaseURI_"+System.getProperty("Env").toUpperCase());
			}else {
				this.BaseURI=(String) System.getProperty("EnvURL");
			}
		}else if(System.getProperty("EnvURL")!=null)  {
			if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else {
				this.BaseURI=(String) System.getProperty("EnvURL");
			}
		}else {
			if(Get_API_Constants(APIName+"_BaseURI")!=null){
				this.BaseURI=(String) Get_API_Constants(APIName+"_BaseURI");
			}else {
				this.BaseURI=(String) Get_API_Constants("BaseURI");
			}
		}

		//reset base URL if changed
//		BaseURI = this.BaseURI;
		if(this.BaseURI==null) {
			ReportFactory.FailTest("FAILURE :: Base URL not set for API : "+API_Name);
		}

		//Set APIType
		if(BaseURI.toString().contains("wss://")||BaseURI.toString().contains("ws://")) {
			APIType = "WS";
		}else {
			APIType = "REST";
		}

		//set silent variable if API request/response details need not be published
		if(silentrun.length>0) {
			silent=silentrun[0];
		}else {
			silent=false;
		}

		//Intialize API constant

		//REST API
		apiRequest = RestAssured.given().baseUri((String) this.BaseURI);
		apiRequestBody = new JSONObject();
		apiRequestBodyString="";
		apiRequestHeaders= new Headers();
		apiRequestHasBody=false;
		apiRequestHasHeaders= false;
		method=null;
		apiRequestEndPoint="";
		apiResponseHeaders=null;
		apiResponseJsonPath=null;
		apiResponseParameters=(List<String>) Get_API_Constants(APIName+"_RespParams");
		apiResponseHeaderNames=(List<String>) Get_API_Constants(APIName+"_RespHeaders");

		//WS Setup
		EndPoint = (String)Get_API_Constants(APIName+"_EndPoint");
		sparkListenerImpl = new WebSocketListenerImpl();
		sparkTicker = new WebSocket_Client(sparkListenerImpl,(String)BaseURI);
		apiRequestStringList = new LinkedList<String>();
		apiResponseStringList= new LinkedList<String>();
		apiResponseObjectList=new LinkedList<Object>();
	}

	/*public void API_Setup_ws(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		//        sparkTicker.getSparkListener().get
		sparkTicker.connect();
		sparkTicker.ping();
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			wsResponse = sparkListenerImpl.response;
		}

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
		//		BaseURI = "wss://mds.angelone.in/spark";
	}

	public void quote1StdSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=206|65=1|1=1$7=1594|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote1StdResponse = sparkListenerImpl.quote1StdResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			wsReqestURI= System.getProperty("EnvURL");
		} else {
			wsReqestURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}

	public void quote1NcdexSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=206|65=1|1=7$7=DHANIYA20JUL2022|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote1NcdexResponse = sparkListenerImpl.quote1NcdexResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}

	public void quote3StdSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=104|65=1|1=1$7=1594|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote3StdResponse = sparkListenerImpl.quote3StdResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}

	public void quote3NcdexSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=104|65=1|1=7$7=DHANIYA20JUL2022|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote3NcdexResponse = sparkListenerImpl.quote3NcdexResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}

	public void quote4StdSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=106|65=1|1=1$7=1594|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote4StdResponse = sparkListenerImpl.quote4StdResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}

	public void quote4NcdexSetUp(String APIName, Object ContantClassInstance, Boolean... silentrun) throws Exception {
		//RestAssured.baseURI = baseURL;
		API_Name = APIName;
		ConstantClassObject = ContantClassInstance;
		SparkListenerImpl sparkListenerImpl = new SparkListenerImpl();
		SparkTicker sparkTicker = new SparkTicker(sparkListenerImpl);
		sparkTicker.connect();
		sparkTicker.subscribe("63=FT3.0|64=106|65=1|1=7$7=DHANIYA20JUL2022|230=1");
		Thread.sleep(1000);
		if(sparkListenerImpl.getStatusCode().equals("100")){
			apiStatusCode = Integer.parseInt(sparkListenerImpl.statusCode);
			Thread.sleep(1000);
			quote4NcdexResponse = sparkListenerImpl.quote4NcdexResponse;
			//            apiResponse = sparkListenerImpl.response;
		}
		//		Thread.sleep(3000);
		//        WebSocketAdapter ada = sparkTicker.getWebsocketAdapter();
		//        ada.onBinaryMessage(sparkTicker.getWs(), sparkTicker.getWs().getConnectedSocket().getInputStream().readAllBytes());

		if (System.getProperty("EnvURL") != null) {
			RestAssured.baseURI = System.getProperty("EnvURL");
		} else {
			RestAssured.baseURI = (String) Get_API_Constants(APIName + "_BaseURI");
		}
	}*/


	public RequestSpecification getAPIRequest() {
		return apiRequest;
	}
	public void setAPIRequestBody(String ... varStr) {
		for(String str:varStr) {
			apiRequestBody.put(str.split(":")[0], str.split(":")[1]);
		}
		apiRequestBodyString = apiRequestBody.toJSONString();
		apiRequestHasBody=true;
	}
	public void setAPIRequestBody(String jsonString) {	
		apiRequestBodyString = jsonString;
		apiRequestHasBody=true;
	}
	public void setAPIRequestBody(Object requestObject) {	
		apiRequestBodyString = getJSONStringFromObject(requestObject);
		apiRequestHasBody=true;
	}

	public void addFormData(String formKey, Object formObject) {
		apiRequest.multiPart(formKey, formObject);	
	}
	public void addFormParam(String formKey, String formValue) {
		apiRequest.multiPart(formKey, formValue);	
	}

	public void setWSAPIListenJsonText(String jsonString) {	
		apiListenText = jsonString;
		apiRequestHasListenText=true;
	}
	public void setWSAPIListenJsonText(Object requestObject) {	
		apiListenText = getJSONStringFromObject(requestObject);
		apiRequestHasListenText=true;
	}
	public void setWSAPIListenText(String text) {	
		apiListenText = text;
		apiRequestHasListenText=true;
	}

	/*public void setAPIRequestHeaders(String ... varStr) {
		List<Header> headerList = new ArrayList<Header>();
		for(String str:varStr) {
			Header header = new Header(str.split(":")[0], str.split(":")[1]);
			headerList.add(header);
		}
		apiRequestHeaders = new Headers(headerList);
		apiRequestHasHeaders=true;
	}
	public void setAPIRequestHeaders(Headers headers) {
		apiRequestHeaders =headers;
		apiRequestHasHeaders=true;
	}
	public void setAPIRequestHeader(String headerName, String headerValue) {
		List<Header> headerList = new ArrayList<Header>();
		for(Header header1:apiRequestHeaders) {
			headerList.add(header1);
		}
		headerList.add(new Header(headerName,headerValue));
		apiRequestHeaders = new Headers(headerList);
		apiRequestHasHeaders=true;
	}*/
	public void addAPIRequestHeader(Header header) {
		List<Header> headerList = new ArrayList<Header>();
		headerList.add(header);
		try {
			for(Header header1:apiRequestHeaders) {
				if(!header1.getName().equals(header.getName())) {
					headerList.add(header1);
				}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//Continue
		}

		apiRequestHeaders = new Headers(headerList);
		apiRequestHasHeaders=true;
	}
	public void addAPIRequestHeader(String headerName, String headerValue) {
		List<Header> headerList = new ArrayList<Header>();
		try {
			for(Header header1:apiRequestHeaders) {
				if(!header1.getName().equals(headerName)) {
					headerList.add(header1);}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//Continue
		}
		headerList.add(new Header(headerName,headerValue));
		apiRequestHeaders = new Headers(headerList);
		apiRequestHasHeaders=true;
	}
	public void addAPIRequestHeaders(String ... varStr) {
		List<Header> headerList = new ArrayList<Header>();
		for(String str:varStr) {
			Header header = new Header(str.split(":")[0], str.split(":")[1]);
			headerList.add(header);
		}
		Headers tmpheaders = new Headers(headerList);
		try {
			for(Header header:apiRequestHeaders) {
				if(tmpheaders.get(header.getName())==null) {
					headerList.add(header);
				}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//Continue
		}
		apiRequestHeaders = new Headers(headerList);
		apiRequestHasHeaders=true;
	}
	public void addAPIRequestHeaders(Headers headers) {
		List<Header> headerList = new ArrayList<Header>();
		/*for(Header header: headers) {
			headerList.add(header);
		}
		Headers tmpheaders = new Headers(headerList);
		try {
			for(Header header:apiRequestHeaders) {
				if(tmpheaders.get(header.getName())==null) {
					headerList.add(header);
				}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//Continue
		}*/

		for(Header header: apiRequestHeaders) {
			headerList.add(header);
		}
		Headers tmpheaders = new Headers(headerList);
		try {
			for(Header header:headers) {
				if(tmpheaders.get(header.getName())==null) {
					headerList.add(header);
				}
			}
		} catch (NullPointerException e) {
			// TODO Auto-generated catch block
			//Continue
		}
		apiRequestHeaders =new Headers(headerList);
		apiRequestHasHeaders=true;
	}
	private void set_API_RequestBody_And_Headers() {
		if(apiRequestHasHeaders) {	
			apiRequest.headers(apiRequestHeaders);
			sparkTicker.AddHeaders(apiRequestHeaders);
		}
		if(apiRequestHasBody) {
			//apiRequestBodyString=apiRequestBody.toJSONString();
			apiRequest.body(apiRequestBodyString);
		}
		if(apiRquestHasQueryParam) {
			//queryParam.forEach((k,v)->apiRequest.queryParam(k, v));
			for(String key:queryParam.keySet()) {
				apiRequest.queryParam(key, queryParam.get(key));
			}
		}
		if(apiRquestHasPathParam) {
			//pathParams.forEach((k,v)->apiRequest.pathParam("param"+k, v));
			for(int key:pathParams.keySet()) {
				apiRequest.pathParam("param"+key, pathParams.get(key));
				if(APIType.equalsIgnoreCase("WS")) {
					apiRequestEndPoint.replaceAll("{param"+key+"}", pathParams.get(key));
				}
			}

		}
	}
	private void Send_API_Request() {
		set_API_RequestBody_And_Headers();
		try {
			if(APIType.contains("WS")){
				sparkTicker.SetEndPoint(apiRequestEndPoint);
				sparkTicker.connect();
				Thread.sleep(10000);
				int i=0;
				while(sparkListenerImpl.statusCode.equals("0")&&i<30) {
					Thread.sleep(1000);
					LogFactory.LogInfo("INFO :: Wating for Websocket to get connected");
					i++;
				}
				apiResponseStatus = sparkListenerImpl.statusLine;
				apiStatusCode=Integer.valueOf(sparkListenerImpl.statusCode);
				apiResponseString = sparkListenerImpl.response;
				//Reset Value of status code for next requests
				sparkListenerImpl.statusCode="0";
			}else {
				apiResponse = apiRequest.request(method,apiRequestEndPoint);
				apiResponseString = getAPIResponse();
				apiResponseHeaders=apiResponse.getHeaders();
				apiResponseJsonPath = apiResponse.jsonPath();
				apiStatusCode = apiResponse.statusCode();
			}	
			if(!silent) {
				print_API_Request_Response_Details();}
		} catch (Exception e) {
			ReportFactory.FailTest("FAILURE :: API Request was unsuccessful");
			String strhtml = "<a><details><summary>Error Details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
			print_API_Request_Details();
			e.printStackTrace();
			throw new RuntimeException();
		}
	}
	public Headers getReponseHeaders() {
		return apiResponseHeaders;
	}
	public JsonPath getReponseJsonPath() {
		return apiResponseJsonPath;
	}	

	private void print_API_Request_Details() {
		//Request URL
		String apiurl=(String) reqDetails.get(0)+(String) reqDetails.get(1);
		if(apiRquestHasPathParam) {
			for(int i=0;i<pathParams.size();i++) {
				apiurl=apiurl.replace("{param"+(i+1)+"}", pathParams.get(i+1));
			}
		}
		String strhtml = "<a><details><summary>REQUEST URL(click to view)</summary><font color=black>"+apiurl+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Request Type
		strhtml = "<a><details><summary>REQUEST TYPE(click to view)</summary><font color=black>"+reqDetails.get(4)+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Request Header
		if(apiRequestHasHeaders) {
			strhtml = "<a><details><summary>REQUEST HEADER(click to view)</summary><font color=black>"+apiRequestHeaders.toString()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);}
		//Request Body
		if(apiRequestHasBody) {
			strhtml = "<a><details><summary>REQUEST BODY(click to view)</summary><font color=black>"+apiRequestBodyString+"</font></details></a>";
			ReportFactory.testInfo(strhtml);}
	}
	private void print_API_Request_Response_Details() {	
		apiResponseString = getAPIResponse();
		apiResponseStatus = getAPIResponseStatus();
		apiStatusCode = getAPIResponseStatusCode();
		//Request URL
		String apiurl=(String) reqDetails.get(0)+(String) reqDetails.get(1);
		if(apiRquestHasPathParam) {
			for(int i=0;i<pathParams.size();i++) {
				apiurl=apiurl.replace("{param"+(i+1)+"}", pathParams.get(i+1));
			}
		}
		String strhtml = "<a><details><summary>REQUEST URL(click to view)</summary><font color=black>"+apiurl+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Request Type
		strhtml = "<a><details><summary>REQUEST TYPE(click to view)</summary><font color=black>"+reqDetails.get(4)+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Request Header
		if(apiRequestHasHeaders) {
			strhtml = "<a><details><summary>REQUEST HEADER(click to view)</summary><font color=black>"+apiRequestHeaders.toString()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);}
		//Request Body
		if(apiRequestHasBody) {
			strhtml = "<a><details><summary>REQUEST BODY(click to view)</summary><font color=black>"+apiRequestBodyString+"</font></details></a>";
			ReportFactory.testInfo(strhtml);}
		//Response Status
		strhtml = "<a><details><summary>RESPONSE STATUS(click to view)</summary><font color=black>"+apiResponseStatus+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Response Body
		strhtml = "<a><details><summary>RESPONSE BODY(click to view)</summary><font color=black>"+apiResponseString+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
	}

	private void print_ping_response_details(){
		apiResponseString = getAPIResponse();
		apiResponseStatus = getAPIResponseStatus();
		//apiStatusCode = getAPIResponseStatusCode();

		//Response Status
		String strhtml = "<a><details><summary>PING STATUS(click to view)</summary><font color=black>"+apiResponseStatus+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Response Body
		strhtml = "<a><details><summary>PING RESPONSE(click to view)</summary><font color=black>"+apiResponseString+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
	}

	private void print_listen_response_details() {
		apiResponseString = getAPIResponse();
		apiResponseStatus = getAPIResponseStatus();
		//apiStatusCode = getAPIResponseStatusCode();

		String strhtml;
		//Listen Text
		if(apiRequestHasListenText) {
			strhtml = "<a><details><summary>REQUEST BODY(click to view)</summary><font color=black>"+apiListenText+"</font></details></a>";
			ReportFactory.testInfo(strhtml);}
		//Response Status
		strhtml = "<a><details><summary>RESPONSE STATUS(click to view)</summary><font color=black>"+apiResponseStatus+"</font></details></a>";
		ReportFactory.testInfo(strhtml);
		//Response Body
		strhtml = "<a><details><summary>RESPONSE BODY(click to view)</summary><font color=black>"+apiResponseString+"</font></details></a>";
		ReportFactory.testInfo(strhtml);

	}

	public String getJSONStringFromObject(Object o) {
		ObjectMapper obj = new ObjectMapper();
		obj.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
		String jSONReq="";
		try {
			jSONReq = obj.writerWithDefaultPrettyPrinter().writeValueAsString(o);
			//jSONReq = obj.writeValueAsString(o);
			Gson gson = new Gson();
			jSONReq = gson.toJson(o);
			return jSONReq;
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			return null;
		}
	}

	public String getJSONStringFromBinary(Object binary) {
		return new Gson().toJson(Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",(byte[])binary)).toString();
	}
	public void Send_API_Request(Method requestType,String path) {
		method = requestType;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse = apiRequest.request(requestType, path);
	}
	public void Send_API_GET_Request(String path) {
		method=Method.GET;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.GET, path);
	}
	public void Send_API_GET_Request(Headers headers,String path) {
		method=Method.GET;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.GET, path);
	}
	public void Send_API_POST_Request_WithEmptyBody(String path) {
		apiRequestEndPoint=path;
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.post(this.BaseURI+path);
		apiResponse=apiRequest.post(this.BaseURI+path);
		apiStatusCode=apiResponse.getStatusCode();
		apiResponseString=getAPIResponse();
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_POST_Request_WithEmptyBody(Headers headers,String path) {
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.post(this.BaseURI+path);
		apiResponse=apiRequest.post(this.BaseURI+path);
		apiResponseStatus=getAPIResponseStatus();
		apiStatusCode=apiResponse.getStatusCode();
		apiResponseString=getAPIResponse();
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_POST_Request(String path) {
		method=Method.POST;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.POST, path);	
	}
	public void Send_API_POST_Request(JSONObject requestBody,String path) {
		method=Method.POST;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.POST, path);
	}
	public void Send_API_POST_Request(Headers requestHeaders,JSONObject requestBody,String path) {
		method=Method.POST;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.POST, path);
	}
	public void Send_API_POST_Request(Headers requestHeaders,String JsonRequestBody,String path) {
		method=Method.POST;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(JsonRequestBody);
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.POST, path);
	}


	public void Send_API_Request(String BaseURI,Method requestType,String path) {
		//API_Setup(BaseURI);
		method = requestType;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse = apiRequest.request(requestType, path);
	}
	public void Send_API_GET_Request(String BaseURI,String path) {
		//API_Setup(BaseURI);
		method=Method.GET;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.GET, path);
	}
	public void Connect_WS_API(String BaseURI,String path) {
		apiRequestEndPoint=path;
		Send_API_Request();
	}
	public void Send_API_GET_Request(String BaseURI,Headers headers,String path) {
		//API_Setup(BaseURI);
		method=Method.GET;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.GET, path);
	}
	public void Connect_WS_API(String BaseURI,Headers headers,String path) {
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		Send_API_Request();
	}
	public void WS_Ping() {
		try {
			apiRequestStringList.add("ping");
			apiRequestBodyString = "ping";
			sparkListenerImpl.resetResponses();
			sparkTicker.ping();
			int i=0;
			try {
				while(sparkListenerImpl.statusCode.equals("0")&&i<30) {
					Thread.sleep(1000);
					System.out.println("INFO :: Waiting for pong");
					i++;
				}		
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			apiResponseStatus = sparkListenerImpl.PingStatusLine;
			apiStatusCode=Integer.valueOf(sparkListenerImpl.statusCode);
			/*apiResponseObjectList.add(sparkListenerImpl.responseObject);
			apiResponseString=sparkListenerImpl.response;
			apiResponseStringList.add(apiResponseString);*/
			apiResponsePongList = sparkListenerImpl.pongList;
			apiResponseStringList = sparkListenerImpl.TextList;
			apiResponseObjectList=sparkListenerImpl.BinaryList;
			if(apiResponsePongList.size()>0) {
				apiResponseString="pong";
			}
			print_ping_response_details();
		} catch (WebSocketException e) {
			ReportFactory.FailTest("FAILURE :: Failed to ping");
			String strhtml = "<a><details><summary>Error Details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
			e.printStackTrace();
		}
	}
	public void WS_Listen() {
		apiRequestStringList.add(apiListenText);
		sparkListenerImpl.resetResponses();
		sparkTicker.listen(apiListenText);
		int i=0;
		try {
			while(!sparkListenerImpl.statusCode.equals("400")&&!(sparkListenerImpl.TextList.size()>=5||sparkListenerImpl.BinaryList.size()>5)&&i<30) {
				Thread.sleep(1000);
				System.out.println("INFO :: Waiting for response");
				i++;
			}
			apiResponseStatus = sparkListenerImpl.MessageStatusLine;
			apiStatusCode=Integer.valueOf(sparkListenerImpl.MessageStatusCode);
			/*Object response = sparkListenerImpl.responseObject;
				if(response instanceof String) {
					apiResponseObjectList.add(response);
					apiResponseStringList.add((String) response);
				}else if(response instanceof byte[]) {
					Object responseObject = Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",response);
					apiResponseObjectList.add(responseObject);
					apiResponseStringList.add(getJSONStringFromObject(responseObject));
				}else {
					apiResponseObjectList.add(sparkListenerImpl.responseObject);
					apiResponseStringList.add(sparkListenerImpl.response);
				}
				apiResponseObject = apiResponseObjectList.getLast();
				apiResponseString = apiResponseStringList.getLast();*/
			apiResponsePongList = sparkListenerImpl.pongList;
			apiResponseStringList = sparkListenerImpl.TextList;
			apiResponseObjectList=sparkListenerImpl.BinaryList;
			if(apiResponseObjectList.size()>0) {
				apiResponseObject = Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",(byte[])apiResponseObjectList.getLast());
				apiResponseString = new Gson().toJson(Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",(byte[])apiResponseObjectList.getLast())).toString();
			}else if(apiResponseStringList.size()>0) {
				apiResponseString=apiResponseStringList.getLast();
			}else if(apiResponsePongList.size()>0) {
				apiResponseString="pong";
			}
			print_listen_response_details();
		} catch (Exception e) {
			ReportFactory.FailTest("FAILURE :: Failed to listen to Websocket response");
			String strhtml = "<a><details><summary>Error Details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
			e.printStackTrace();
			e.printStackTrace();
		}
	}
	public void WS_Listen(String message) {
		try {
			apiRequestStringList.add(message);
			apiRequestBodyString=message;
			sparkListenerImpl.resetResponses();
			sparkTicker.listen(message);
			int i=0;		
			while(!sparkListenerImpl.statusCode.equals("400")&&!(sparkListenerImpl.TextList.size()>=5||sparkListenerImpl.BinaryList.size()>5)&&i<30) {
				Thread.sleep(1000);
				System.out.println("INFO :: Waiting for response");
				i++;
			}
			apiResponseStatus = sparkListenerImpl.MessageStatusLine;
			apiStatusCode=Integer.valueOf(sparkListenerImpl.MessageStatusCode);
			/*apiResponseObjectList.add(sparkListenerImpl.responseObject);
				if(sparkListenerImpl.responseObject instanceof String) {
					apiResponseObjectList.add(sparkListenerImpl.responseObject);
					apiResponseStringList.add((String) sparkListenerImpl.responseObject);
				}else if(sparkListenerImpl.responseObject instanceof byte[]) {
					Object responseObject = Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",sparkListenerImpl.responseObject);
					apiResponseObjectList.add(responseObject);
					apiResponseStringList.add(getJSONStringFromObject(responseObject));
				}
				apiResponseObject = apiResponseObjectList.getLast();
				apiResponseString = apiResponseStringList.getLast();*/
			apiResponsePongList = sparkListenerImpl.pongList;
			apiResponseStringList = sparkListenerImpl.TextList;
			apiResponseObjectList=sparkListenerImpl.BinaryList;
			if(apiResponseObjectList.size()>0) {
				apiResponseObject = Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",(byte[])apiResponseObjectList.getLast());
				apiResponseString = new Gson().toJson(Get_API_Methods(PojoMapClassObject,API_Name+"_BinarytoPojo",(byte[])apiResponseObjectList.getLast())).toString();
			}else if(apiResponseStringList.size()>0) {
				apiResponseString=apiResponseStringList.getLast();
			}else if(apiResponsePongList.size()>0) {
				apiResponseString="pong";
			}
			print_listen_response_details();
		} catch (Exception e) {
			ReportFactory.FailTest("FAILURE :: Failed to listen to Websocket response");
			String strhtml = "<a><details><summary>Error Details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
			e.printStackTrace();
			e.printStackTrace();
		}
	}
	public void Convert_WS_Object_To_String(Object object) {

	}
	public void Disconnect_WS_API() {
		try {
			sparkListenerImpl.resetResponses();
			sparkTicker.disconnect();
			int i=0;
			while(sparkListenerImpl.statusCode.equals("0")&&i<30) {
				Thread.sleep(1000);
				LogFactory.LogInfo("INFO :: Wating for Websocket to get disconnected");
				i++;
			}
			apiResponseStatus = sparkListenerImpl.statusLine;
			//Response Status
			String strhtml = "<a><details><summary>RESPONSE STATUS(click to view)</summary><font color=black>"+apiResponseStatus+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (Exception e) {
			ReportFactory.FailTest("FAILURE :: Failed to disconnect wesocket connection");
			String strhtml = "<a><details><summary>Error Details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
			e.printStackTrace();
			e.printStackTrace();
		}
	}
	public void Validate_WS_Connection_Open() {
		if(sparkTicker.isConnectionOpen()) {
			ReportFactory.PassTest("SUCCESS :: WS Connection is open.");
		}else {
			ReportFactory.FailTest("FAILURE :: WS Connection is not open.");
		}
	}

	public void Validate_WS_Connection_Closed() {
		if(sparkTicker.isConnectionClosed()) {
			ReportFactory.PassTest("SUCCESS :: WS Connection is closed.");
		}else {
			ReportFactory.FailTest("FAILURE :: WS Connection is open.");
		}
	}
	public void Send_API_POST_Request_WithEmptyBody(String BaseURI,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.post(this.BaseURI+path);
		apiResponse=apiRequest.post(this.BaseURI+path);
		apiResponseStatus=getAPIResponseStatus();
		apiStatusCode=apiResponse.getStatusCode();
		apiResponseString=getAPIResponse();
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_POST_Request_WithEmptyBody(String BaseURI,Headers headers,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		set_API_RequestBody_And_Headers();	
		//apiResponse=RestAssured.post(this.BaseURI+path);
		apiResponse=apiRequest.post(this.BaseURI+path);
		apiResponseStatus=getAPIResponseStatus();
		apiStatusCode=apiResponse.getStatusCode();
		apiResponseString=getAPIResponse();
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_POST_Request(String BaseURI,String path) {
		//API_Setup(BaseURI);
		method=Method.POST;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.POST, path);	
	}
	public void Send_API_POST_Request(String BaseURI,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.POST;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.POST, path);
	}
	public void Send_API_POST_Request(String BaseURI,String requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.POST;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.POST, path);
	}
	public void Send_API_POST_Request(String BaseURI,Headers requestHeaders,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.POST;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(requestBody.toJSONString());
		set_API_RequestBody_And_Headers();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.POST, path);
	}
	public void Send_API_POST_Request(String BaseURI,Headers requestHeaders,String JsonRequestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.POST;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(JsonRequestBody);
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.POST, path);
	}

	public void Send_API_PUT_Request_WithEmptyBody(String BaseURI,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.put(this.BaseURI+path);
		apiResponse=apiRequest.put(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_PUT_Request_WithEmptyBody(String BaseURI,Headers headers,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		set_API_RequestBody_And_Headers();	
		//apiResponse=RestAssured.put(this.BaseURI+path);
		apiResponse=apiRequest.put(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_PUT_Request(String BaseURI,String path) {
		//API_Setup(BaseURI);
		method=Method.PUT;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PUT, path);	
	}
	public void Send_API_PUT_Request(String BaseURI,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PUT;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PUT, path);
	}
	public void Send_API_PUT_Request(String BaseURI,String requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PUT;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PUT, path);
	}
	public void Send_API_PUT_Request(String BaseURI,Headers requestHeaders,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PUT;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(requestBody.toJSONString());
		set_API_RequestBody_And_Headers();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.PUT, path);
	}
	public void Send_API_PUT_Request(String BaseURI,Headers requestHeaders,String JsonRequestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PUT;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(JsonRequestBody);
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.PUT, path);
	}

	public void Send_API_PATCH_Request_WithEmptyBody(String BaseURI,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.patch(this.BaseURI+path);
		apiResponse=apiRequest.patch(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_PATCH_Request_WithEmptyBody(String BaseURI,Headers headers,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		set_API_RequestBody_And_Headers();	
		//apiResponse=RestAssured.patch(this.BaseURI+path);
		apiResponse=apiRequest.patch(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_PATCH_Request(String BaseURI,String path) {
		//API_Setup(BaseURI);
		method=Method.PATCH;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PATCH, path);	
	}
	public void Send_API_PATCH_Request(String BaseURI,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PATCH;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PATCH, path);
	}
	public void Send_API_PATCH_Request(String BaseURI,String requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PATCH;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.PATCH, path);
	}
	public void Send_API_PATCH_Request(String BaseURI,Headers requestHeaders,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PATCH;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(requestBody.toJSONString());
		set_API_RequestBody_And_Headers();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.PATCH, path);
	}
	public void Send_API_PATCH_Request(String BaseURI,Headers requestHeaders,String JsonRequestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.PATCH;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(JsonRequestBody);
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.PATCH, path);
	}

	public void Send_API_DELETE_Request_WithEmptyBody(String BaseURI,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		set_API_RequestBody_And_Headers();
		//apiResponse=RestAssured.delete(this.BaseURI+path);
		apiResponse=apiRequest.delete(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_DELETE_Request_WithEmptyBody(String BaseURI,Headers headers,String path) {
		//API_Setup(BaseURI);
		apiRequestEndPoint=path;
		addAPIRequestHeaders(headers);
		set_API_RequestBody_And_Headers();	
		//apiResponse=RestAssured.delete(this.BaseURI+path);
		apiResponse=apiRequest.delete(this.BaseURI+path);
		if(!silent) {
			print_API_Request_Response_Details();
		}
	}
	public void Send_API_DELETE_Request(String BaseURI,String path) {
		//API_Setup(BaseURI);
		method=Method.DELETE;
		apiRequestEndPoint=path;
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.DELETE, path);	
	}
	public void Send_API_DELETE_Request(String BaseURI,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.DELETE;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody.toJSONString());
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.DELETE, path);
	}
	public void Send_API_DELETE_Request(String BaseURI,String requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.DELETE;
		apiRequestEndPoint=path;
		setAPIRequestBody(requestBody);
		Send_API_Request();
		//apiResponse=apiRequest.request(Method.DELETE, path);
	}
	public void Send_API_DELETE_Request(String BaseURI,Headers requestHeaders,JSONObject requestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.DELETE;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(requestBody.toJSONString());
		set_API_RequestBody_And_Headers();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.DELETE, path);
	}
	public void Send_API_DELETE_Request(String BaseURI,Headers requestHeaders,String JsonRequestBody,String path) {
		//API_Setup(BaseURI);
		method=Method.DELETE;
		apiRequestEndPoint=path;
		addAPIRequestHeaders(requestHeaders);
		setAPIRequestBody(JsonRequestBody);
		Send_API_Request();
		//apiResponse=apiRequest.body(requestBody.toJSONString()).headers(requestHeaders).when().request(Method.DELETE, path);
	}

	public String getAPIResponseStatus() {
		if(APIType.equalsIgnoreCase("WS")) {
			return apiResponseStatus;
		}else {
			return apiResponse.getStatusLine();
		}
	}
	public int getAPIResponseStatusCode() {
		if(APIType.equalsIgnoreCase("WS")) {
			return Integer.valueOf(sparkListenerImpl.statusCode);
		}else {
			return apiResponse.getStatusCode();
		}

	}
	public Object getJsonParameter(String jsonPath) {
		Object objvalue="";
		try {
			String jsonString;
			if(apiResponseString!=null) {
				jsonString = apiResponseString;
			}else {
				jsonString = apiResponse.getBody().asString();
			}
			objvalue = com.jayway.jsonpath.JsonPath.read(jsonString,jsonPath);
			//objvalue = com.jayway.jsonpath.JsonPath.read(apiResponse.getBody().asString(),jsonPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ReportFactory.FailTest("FALURE: Json Parameter "+jsonPath+" not found");
		}
		return objvalue;
	}
	public Object getJsonParameter(String jsonResponse,String jsonPath) {
		Object objvalue="";
		try {
			objvalue = com.jayway.jsonpath.JsonPath.read(jsonResponse,jsonPath);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			ReportFactory.FailTest("FALURE: Json Parameter "+jsonPath+" not found");
		}
		return objvalue;
		//return com.jayway.jsonpath.JsonPath.read(jsonResponse,jsonPath);
	}
	public Boolean validateJsonParameterValue(String name, String jsonPath, Object ...values ) {
		Object actualValue = getJsonParameter(jsonPath);
		int count=0;
		List <Object> valueList= new ArrayList<Object>();
		for(Object o:values) {
			valueList.add(o);
			if(o.equals(actualValue)) {
				count++;
				ReportFactory.PassTest("SUCCESS: "+name+" is "+o);
			}
		}
		if(count==0) {
			ReportFactory.FailTest("FAILURE: "+name+" is not havinng the expected value(s) "+valueList.toString());
			return false;
		}else {
			return true;
		}

	}
	public Boolean validateJsonParameterValue(String name, String jsonPath, List<Object> values  ) {
		Object actualValue = getJsonParameter(jsonPath);
		int count=0;
		List <Object> valueList= new ArrayList<Object>();
		for(Object o:values) {
			valueList.add(o);
			if(o.equals(actualValue)) {
				count++;
				//ReportFactory.PassTest("SUCCESS: "+name+" is "+o);
			}
		}
		if(count==0) {
			ReportFactory.FailTest("FAILURE: "+name+" is not having the expected value(s) "+valueList.toString());
			return false;
		}else {
			ReportFactory.PassTest("SUCCESS: "+name+" is "+actualValue+" (Expected values"+valueList.toString()+")");
			return true;
		}

	}
	public void Validate_APIResponseStatus() {
		//if(apiResponse.getStatusCode()==200) {
		if(apiStatusCode==200) {	
			//System.out.println("SUCCESS :: API_request completed successfully ");
			ReportFactory.PassTest("SUCCESS :: API_request completed successfully");
		}else {
			//System.out.println("FAILURE :: API_request not completed successfully ");
			ReportFactory.FailTest("FAILURE :: API_request not completed successfully ");		
		}
		//String strhtml = "<a><details><summary>Click to view response</summary><font color=black>"+apiResponse.getStatusLine()+"</font></details></a>";
		//ReportFactory.testInfo(strhtml);

	}
	public void Validate_APIResponseStatus(int statusCode) {
		if(apiStatusCode==statusCode) {
			//System.out.println("SUCCESS :: API_request completed successfully ");
			ReportFactory.PassTest("SUCCESS :: API request completed with the expected status");
		}else {
			//System.out.println("FAILURE :: API_request not completed successfully ");
			ReportFactory.FailTest("FAILURE :: API request not completed with the expected status "+statusCode);		
		}
		//String reponsehtml = "<a><details><summary>Click to view response status</summary>"+apiResponse.getStatusLine()+"</details></a>";
		//ReportFactory.testInfo(reponsehtml);
	}
	public void Validate_APIResponseStatus(String statusCode) {
		if(apiResponseStatus.equalsIgnoreCase(statusCode)) {
			//System.out.println("SUCCESS :: API_request completed successfully ");
			ReportFactory.PassTest("SUCCESS :: API request completed with the expected status");
		}else {
			//System.out.println("FAILURE :: API_request not completed successfully ");
			ReportFactory.FailTest("FAILURE :: API request not completed with the expected status "+statusCode);		
		}
		//String reponsehtml = "<a><details><summary>Click to view response status</summary>"+apiResponse.getStatusLine()+"</details></a>";
		//ReportFactory.testInfo(reponsehtml);
	}

	public void Validate_WebSocketResponseStatus(int statusCode) {
		if (apiStatusCode == statusCode) {
			//System.out.println("SUCCESS :: API_request completed successfully ");
			ReportFactory.PassTest("SUCCESS :: Websocket request completed with the expected status");
		} else {
			//System.out.println("FAILURE :: API_request not completed successfully ");
			ReportFactory.FailTest("FAILURE :: Websocket request not completed with the expected status " + statusCode);
		}
		//String reponsehtml = "<a><details><summary>Click to view response status</summary>"+apiResponse.getStatusLine()+"</details></a>";
		//ReportFactory.testInfo(reponsehtml);
	}

	public String getAPIResponse() {
		if(APIType.equalsIgnoreCase("WS")) {
			return apiResponseString;
		}else {
			return apiResponse.getBody().asString();
		}

	}
	public Object getAPIResponseObject() {
		return apiResponseObject;
	}

	public List<String> getAPIReponseStringList() {
		return apiResponseStringList;
	}
	public List<Object> getAPIReponseObjectList() {
		return apiResponseObjectList;
	}
	public List<Object> getAPIReponsePongList() {
		return apiResponsePongList;
	}
	public List<Long> getAPIResponseTimeStamps(){
		return sparkTicker.GetMessageTimeStamps();
	}
	public String getWSResponse() {
		return apiResponseString;
	}
	public void Validate_APIResponse() {

		//if(apiResponse.getBody().asString().equals("{}")) {
		if(apiResponseString.equals("{}")) {
			ReportFactory.FailTest("FAILURE :: API request returned an invalid response");
		}else if(reqDetails.get(4).toString().equalsIgnoreCase("GET")) {
			Validate_APIResponseStatus(200);
		}else if(reqDetails.get(4).toString().equalsIgnoreCase("POST")) {
			Validate_APIResponseStatus(201);	
		}
		//String reponsehtml = "<a><details><summary>Click to view response</summary>"+apiResponse.getBody().asString()+"</details><a>";
		//ReportFactory.testInfo(reponsehtml);	
	}
	public void Validate_APIResponseBody(String parameterKey) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":")) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+"</font>");
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+"</font>");
		}
	}
	public void Validate_APIResponseBody_ParameterIsNotNull(String parameterKey) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":")) {
			if(!(responseJson.contains("\""+parameterKey+"\":\"\"")&&responseJson.contains("\""+parameterKey+"\":null"))) {
				ReportFactory.PassTest("SUCCESS ::  Value for parameter <font color=black>"+parameterKey+"</font> in the API response is not null");
			}else {
				ReportFactory.FailTest("FAILURE :: Value for parameter <font color=black>"+parameterKey+"</font> in the API response is null");
			}
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+"</font>");
		}
	}
	public void Validate_APIResponseBody_ParameterIsNull(String parameterKey) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":")) {
			if(responseJson.contains("\""+parameterKey+"\":\"\"")||responseJson.contains("\""+parameterKey+"\":null")) {
				ReportFactory.PassTest("SUCCESS ::  Value for parameter <font color=black>"+parameterKey+"</font> in the API response is null");
			}else {
				ReportFactory.FailTest("FAILURE :: Value for parameter <font color=black>"+parameterKey+"</font> in the API response is not null");
			}
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+"</font>");
		}
	}
	public Boolean Validate_APIResponseBody(String parameterKey, String paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":\""+paramterValue+"\"")) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean Validate_APIResponseBody(String parameterKey, int paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean Validate_APIResponseBody(String parameterKey, Boolean paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean Validate_APIResponseBody(String parameterKey, double paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}

	public Boolean AssertParameterValue(String parameterKey, String paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":\""+paramterValue+"\"")) {
			//ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			//ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean AssertParameterValue(String parameterKey, int paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			//ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			//ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean AssertParameterValue(String parameterKey, long paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			//ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			//ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean AssertParameterValue(String parameterKey, Boolean paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			//ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			//ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public Boolean AssertParameterValue(String parameterKey, double paramterValue) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains("\""+parameterKey+"\":"+paramterValue)) {
			//ReportFactory.PassTest("SUCCESS :: API response contains the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return true;
		}else {
			//ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected parameter <font color=black>"+parameterKey+" : "+paramterValue+"</font>");
			return false;
		}
	}
	public void Validate_APIResponseBody(List<String> parameters ) {
		int count=0;
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		for(String str:parameters) {
			if(!responseJson.contains("\""+str+"\":")){
				count++;
				ReportFactory.FailTest("FAILURE :: parameter <font color=black>"+str+"</font> missing in the response");
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: All expected parameters are returned in the reponse.");
		}
	}
	public void Validate_APIResponseBodyContains(String SearchText) {
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		if(responseJson.contains(SearchText)) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected text: <font color=black>"+SearchText+"</font>");
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected text: <font color=black>"+SearchText+"</font>");
		}
	}
	public void Validate_APIResponseParamters() {
		int count=0;
		//String responseJson = apiResponse.getBody().asString();
		String responseJson = apiResponseString;
		for(String str:apiResponseParameters) {
			if(!responseJson.contains("\""+str+"\":")){
				count++;
				ReportFactory.FailTest("FAILURE :: parameter <font color=black>"+str+"</font> missing in the response");
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: All expected parameters are returned in the reponse.");
			String strhtml = "<a><details><summary>Reponse Parameters(click to view)</summary><font color=black>"+apiResponseParameters.toString()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}
	}

	public void Validate_WS_APIResponseParameters() {
		if(apiResponseParameters!=null) {
			int count=0;
			String responseJson = getWSResponse();
			for(String str:apiResponseParameters) {
				if(!responseJson.contains("\""+str+"\":")){
					count++;
					ReportFactory.FailTest("FAILURE :: parameter <font color=black>"+str+"</font> missing in the response");
				}
			}
			if(count==0) {
				ReportFactory.PassTest("SUCCESS :: All expected parameters are returned in the reponse.");
				String strhtml = "<a><details><summary>Reponse Parameters(click to view)</summary><font color=black>"+apiResponseParameters.toString()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}
		}
	}

	public void Validate_WS_APIResponseParameterTypes() {
		int count=0;
		//String responseJson = getWSResponse();
		String responseJson = getAPIResponse();
		List<String> paramTypeList = (List<String>) API_BaseClass.Get_API_Constants(API_BaseClass.API_Name+"_RespParamTypes");
		List<String> paramList = new ArrayList<String>();
		if((apiResponseParameters!=null)&&(paramTypeList!=null)) {
			for(String str:apiResponseParameters) {
				if(!responseJson.contains("\""+str+"\":")){
					count++;
					ReportFactory.FailTest("FAILURE :: parameter <font color=black>"+str+"</font> missing in the response");
				}else {
					paramList.add(str+" : "+paramTypeList.get(apiResponseParameters.indexOf(str)));
					validateJsonParamterType(str, paramTypeList.get(apiResponseParameters.indexOf(str)));
				}
			}
			if(count==0) {
				ReportFactory.PassTest("SUCCESS :: All parameters have the expected data type in the reponse.");
				String strhtml = "<a><details><summary>Reponse Parameters(click to view)</summary><font color=black>"+apiResponseParameters.toString()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}
		}
	}

	public void Validate_APIResponseHeaders() {
		for(String str:apiResponseHeaderNames) {
			Validate_APIResponseHeader(str);
		}
	}
	public long getAPIResponseTime() {

		return apiResponse.time();
	}

	public void Validate_APIResponseTime(long millis) {

		if(apiResponse.getTime()<millis) {
			//System.out.println("SUCCESS :: Reponse time below specified threshold of "+millis+" ms");
			ReportFactory.PassTest("SUCCESS :: Response time is <font color=black>"+apiResponse.getTime()+" ms</font> (< threshold of "+millis+" ms)");
		}else {
			//System.out.println("FAILURE :: Reponse time above specified threshold of "+millis+" ms");
			ReportFactory.FailTest("FAILURE :: Response time is <font color=black>"+apiResponse.getTime()+" ms</font> (> threshold of "+millis+" ms)");
		}
	}
	public void Validate_APIResponseHeader(String headerName) {
		Headers headers = apiResponse.headers();
		if(headers.get(headerName)!=null) {
			ReportFactory.PassTest("SUCCESS :: API response contains the expected header <font color=black>"+headerName+"</font>");
		}else {
			ReportFactory.FailTest("FAILURE :: API reponse does not contain the expected header <font color=black>"+headerName+"</font>");
		}
	}
	public void Validate_APIResponseHeader(Header header) {
		Headers responseHeaders = apiResponse.getHeaders();
		if(responseHeaders.getValue(header.getName()).equals(header.getValue())) {
			ReportFactory.PassTest("SUCCESS :: Header <font color=black>"+header.getName()+" : "+header.getValue()+"</font> is present in the response");
		}else {
			ReportFactory.FailTest("FAILURE :: Header <font color=black>"+header.getName()+" : "+header.getValue()+"</font> is missing in the response");
		}
	}
	public void Validate_APIResponseHeader(String headerName,String headerValue) {
		Headers responseHeaders = apiResponse.getHeaders();
		if(responseHeaders.getValue(headerName).equals(headerValue)) {
			ReportFactory.PassTest("SUCCESS :: Header <font color=black>"+headerName+" : "+headerValue+"</font> is present in the response");
		}else {
			ReportFactory.FailTest("FAILURE :: Header <font color=black>"+headerName+" : "+headerValue+"</font> is missing in the response");
		}
	}
	public void Validate_APIResponseHeaders(Headers headers) {
		int count=0;
		Headers responseHeaders = apiResponse.getHeaders();
		for(Header header:headers) {
			if(responseHeaders.getValue(header.getName())==null) {
				count++;
				ReportFactory.FailTest("FAILURE :: Header <font color=black>"+header.getName()+"</font> missing in the response");
			}
		}
		if(count==0) {
			ReportFactory.PassTest("SUCCESS :: All expected headers are returned in the reponse.");
		}
	}

	private List<Object> reqDetails;
	public Object Set_API_Request_Details(String APIName) {	
		reqDetails=new ArrayList<Object>();
		//Request Base URI
		reqDetails.add(BaseURI);
		if((reqDetails.get(0)==null)||reqDetails.get(0).toString().equals("")) {
			ReportFactory.FailTest("FAILURE :: Base URL not set in constants file for API : "+APIName);
		}
		//Request End Point
		reqDetails.add(Get_API_Constants(APIName+"_EndPoint"));
		if((reqDetails.get(1)==null)||reqDetails.get(1).toString().equals("")) {
			ReportFactory.FailTest("FAILURE :: End Point not set in constants file for API : "+APIName);
		}
		//Request Headers
		if(Get_API_Constants(APIName+"_ReqHeaders_Names")!=null) {
			String _ReqHeaders_Names = (String) Get_API_Constants(APIName+"_ReqHeaders_Names");
			String _ReqHeaders_Values = (String) Get_API_Constants(APIName+"_ReqHeaders_Values");
			HashMap <String,String> headersMap = new HashMap<String, String>();
			for(int i=0;i<_ReqHeaders_Names.split(",").length;i++) {
				headersMap.put(_ReqHeaders_Names.split(",")[i], _ReqHeaders_Values.split(",")[i]);
			}
			List<Header> headerList = new ArrayList<Header>();
			//headersMap.forEach((k,v) -> headerList.add(new Header(k,v)));
			for(String key:headersMap.keySet()) {
				headerList.add(new Header(key,headersMap.get(key)));
			}
			reqDetails.add(new Headers(headerList));
		}else {
			reqDetails.add(Get_API_Methods(APIName+"_GetReqHeaders"));
		}
		//Request Body
		if(Get_API_Constants(APIName+"_RequestBody")!=null) {
			apiRequestHasBody=true;
			reqDetails.add(Get_API_Constants(APIName+"_RequestBody"));
		}else if(Get_API_Methods(APIName+"_RequestBody")!=null) {
			apiRequestHasBody=true;
			reqDetails.add(Get_API_Methods(APIName+"_RequestBody"));
		}else {
			reqDetails.add(null);
		}
		//Request Type
		reqDetails.add(Get_API_Constants(APIName+"_MethodType"));
		if((reqDetails.get(4)==null)||reqDetails.get(1).toString().equals("")) {
			ReportFactory.FailTest("FAILURE :: Request Type not set in constants file for API : "+APIName);
		}
		//Query Parameters
		if(Get_API_Constants(APIName+"_ReqParam_Keys")!=null) {
			String _ReqParam_keys = (String) Get_API_Constants(APIName+"_ReqParam_Keys");
			String _ReqParam_values = (String) Get_API_Constants(APIName+"_ReqParam_Values"); 
			HashMap<String,String> qparam = new HashMap<String, String>();		
			for(int i=0;i<_ReqParam_keys.split(",").length;i++) {
				qparam.put(_ReqParam_keys.split(",")[i], _ReqParam_values.split(",")[i]);
			}
			reqDetails.add(qparam);
		}else{
			reqDetails.add(Get_API_Methods(APIName+"_QueryParams"));
		}
		//ListenBody
		if(Get_API_Constants(APIName+"_ListenText")!=null) {
			apiRequestHasListenText=true;
			apiListenText=(String) Get_API_Constants(APIName+"_ListenText");
		}
		System.out.println(reqDetails.toString());
		return reqDetails;
	}
	/*public void Send_API_Request(String APIName,String ... queryParamValues) {
		Set_API_Request_Details(APIName);
		apiRquestHasPathParam=false;
		apiRquestHasQueryParam=false;
		queryParam = (HashMap<String, String>) reqDetails.get(5);	
		if(reqDetails.get(1).toString().contains("{param")) {
			apiRquestHasPathParam=true;
			pathParams = new HashMap<Integer,String>();
			for(int i=0;i<queryParamValues.length;i++) {
				pathParams.put(i+1, queryParamValues[i]);
			}
		}else if(queryParamValues.length>0) {
			apiRquestHasQueryParam=true;
			int paramCount=0;
			for(String key:queryParam.keySet()) {
				if(queryParam.get(key).equals("")) {
					queryParam.put(key, queryParamValues[paramCount]);
					paramCount++;
				}
			}
		}
		if(reqDetails.get(4).toString().equalsIgnoreCase("POST")) {

			if((reqDetails.get(2)==null)&&(reqDetails.get(3)==null)) {Send_API_POST_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(2)==null) {Send_API_POST_Request(reqDetails.get(0).toString(), reqDetails.get(3).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(3)==null) {Send_API_POST_Request_WithEmptyBody(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(1).toString());}
			else {Send_API_POST_Request(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(3).toString(), reqDetails.get(1).toString());}

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("GET")) {

			if(reqDetails.get(2)==null) {Send_API_GET_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else {Send_API_GET_Request(reqDetails.get(0).toString(),(Headers)reqDetails.get(2),reqDetails.get(1).toString());}

		}
	}*/
	public void Send_API_Request(String ... queryParamValues) {
		Set_API_Request_Details(API_Name);
		apiRquestHasPathParam=false;
		apiRquestHasQueryParam=false;
		queryParam = (HashMap<String, String>) reqDetails.get(5);	
		if(reqDetails.get(1).toString().contains("{param")) {
			apiRquestHasPathParam=true;
			pathParams = new HashMap<Integer,String>();
			for(int i=0;i<queryParamValues.length;i++) {
				pathParams.put(i+1, queryParamValues[i]);
			}
		}else if(queryParamValues.length>0) {
			apiRquestHasQueryParam=true;
			int paramCount=0;
			for(String key:queryParam.keySet()) {
				if(queryParam.get(key).equals("")) {
					queryParam.put(key, queryParamValues[paramCount]);
					paramCount++;
				}
			}
		}
		if(reqDetails.get(4).toString().equalsIgnoreCase("POST")) {

			if((reqDetails.get(2)==null)&&(reqDetails.get(3)==null)) {Send_API_POST_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(2)==null) {Send_API_POST_Request(reqDetails.get(0).toString(), reqDetails.get(3).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(3)==null) {Send_API_POST_Request_WithEmptyBody(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(1).toString());}
			else {Send_API_POST_Request(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(3).toString(), reqDetails.get(1).toString());}

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("GET")) {

			if(reqDetails.get(2)==null) {Send_API_GET_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else {Send_API_GET_Request(reqDetails.get(0).toString(),(Headers)reqDetails.get(2),reqDetails.get(1).toString());}

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("PUT")) {

			if((reqDetails.get(2)==null)&&(reqDetails.get(3)==null)) {Send_API_PUT_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(2)==null) {Send_API_PUT_Request(reqDetails.get(0).toString(), reqDetails.get(3).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(3)==null) {Send_API_PUT_Request_WithEmptyBody(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(1).toString());}
			else {Send_API_PUT_Request(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(3).toString(), reqDetails.get(1).toString());}	

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("PATCH")) {

			if((reqDetails.get(2)==null)&&(reqDetails.get(3)==null)) {Send_API_PATCH_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(2)==null) {Send_API_PATCH_Request(reqDetails.get(0).toString(), reqDetails.get(3).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(3)==null) {Send_API_PATCH_Request_WithEmptyBody(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(1).toString());}
			else {Send_API_PATCH_Request(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(3).toString(), reqDetails.get(1).toString());}	

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("DELETE")) {

			if((reqDetails.get(2)==null)&&(reqDetails.get(3)==null)) {Send_API_DELETE_Request(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(2)==null) {Send_API_DELETE_Request(reqDetails.get(0).toString(), reqDetails.get(3).toString(), reqDetails.get(1).toString());}
			else if(reqDetails.get(3)==null) {Send_API_DELETE_Request_WithEmptyBody(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(1).toString());}
			else {Send_API_DELETE_Request(reqDetails.get(0).toString(), (Headers)reqDetails.get(2), reqDetails.get(3).toString(), reqDetails.get(1).toString());}	

		}else if(reqDetails.get(4).toString().equalsIgnoreCase("PING")||reqDetails.get(4).toString().equalsIgnoreCase("TEXT")) {
			if(reqDetails.get(2)==null) {Connect_WS_API(reqDetails.get(0).toString(), reqDetails.get(1).toString());}
			else {Connect_WS_API(reqDetails.get(0).toString(),(Headers)reqDetails.get(2),reqDetails.get(1).toString());}
		}
	}
	public Object Get_API_Methods(String methodName, Object ... params) {
		//IPO_Contants obj = new IPO_Contants();
		//API_Constants obj = new API_Constants();
		//Object obj = API_Constants.GetObject();
		//Class<?> classObj = obj.getClass();
		Class<?> classObj = ConstantClassObject.getClass();
		Object returnObj = null;
		try {
			java.lang.reflect.Method method = classObj.getDeclaredMethod(methodName, null);
			returnObj =  method.invoke(ConstantClassObject, null);
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnObj;
	}

	public Object Get_API_Methods(Object ClassObject,String methodName, Object ... params) {
		//IPO_Contants obj = new IPO_Contants();
		//API_Constants obj = new API_Constants();
		//Object obj = API_Constants.GetObject();
		//Class<?> classObj = obj.getClass();
		Class<?> classObj = ClassObject.getClass();
		Object returnObj = null;
		java.lang.reflect.Method method;
		try {
			if(params.length>0) {
				int len = params.length;
				switch(len){
				case 1:
					method = classObj.getDeclaredMethod(methodName, params[0].getClass());
					returnObj =  method.invoke(ConstantClassObject, params[0]);
					break;
				case 2:
					method = classObj.getDeclaredMethod(methodName, params[0].getClass(), params[1].getClass());
					returnObj =  method.invoke(ConstantClassObject, params[0],params[1]);
					break;
				case 3:
					method = classObj.getDeclaredMethod(methodName, params[0].getClass(), params[1].getClass(), params[2].getClass());
					returnObj =  method.invoke(ConstantClassObject, params[0],params[1],params[2]);
					break;
				case 4:
					method = classObj.getDeclaredMethod(methodName, params[0].getClass(), params[1].getClass(), params[2].getClass(), params[2].getClass());
					returnObj =  method.invoke(ConstantClassObject, params[0],params[1],params[2], params[2]);
					break;
				}
			}else {
				method = classObj.getDeclaredMethod(methodName, null);
				returnObj =  method.invoke(ConstantClassObject, null);
			}

		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnObj;
	}

	public Object Get_API_Constants(String methodName) {
		//IPO_Contants obj = new IPO_Contants();
		//API_Constants obj = new API_Constants();
		//Object obj = API_Constants.GetObject();
		//Class<?> classObj = obj.getClass();
		Class<?> classObj = ConstantClassObject.getClass();
		Object returnObj = null;
		try {
			java.lang.reflect.Field field = classObj.getDeclaredField(methodName);
			returnObj = field.get(ConstantClassObject);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnObj;
	}

	public Object Get_API_Constants(Object ClassObject,String methodName) {
		//IPO_Contants obj = new IPO_Contants();
		//API_Constants obj = new API_Constants();
		//Object obj = API_Constants.GetObject();
		//Class<?> classObj = obj.getClass();
		Class<?> classObj = ClassObject.getClass();
		Object returnObj = null;
		try {
			java.lang.reflect.Field field = classObj.getDeclaredField(methodName);
			returnObj = field.get(ConstantClassObject);
		} catch (NoSuchFieldException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		} catch (SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return returnObj;
	}

	public void validateJsonSchema(){		
		if(APIType.equalsIgnoreCase("WS")) {
			try {
				org.json.JSONObject jsonSchema = new org.json.JSONObject(
						new JSONTokener(API_BaseClass.class.getResourceAsStream("/"+API_Name+"_Schema.json")));
				org.json.JSONObject jsonObject = new org.json.JSONObject(new Gson().toJson(apiResponseObject));
				try {
					Schema schema = SchemaLoader.load(jsonSchema);
					schema.validate(jsonObject);
					ReportFactory.PassTest("SUCCESS :: API response matches the expected schema");	
				}catch(ValidationException e) {
					ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
					System.out.println(e.getMessage());
					List<ValidationException> eList = e.getCausingExceptions();
					String eDetails="";
					for(ValidationException ex:eList) {
						eDetails+="<li>field: "+ex.getPointerToViolation()+", error message: "+ex.getMessage()+";</li>";
						System.out.println(eDetails);	
					}
					String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black><ol>"+eDetails+"</ol></font></details></a>";
					ReportFactory.testInfo(strhtml);
				}catch(Exception e) {
					ReportFactory.FailTest("FAILURE :: Error occured while validating the response schema");
					String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
					ReportFactory.testInfo(strhtml);
				}
			} catch (Exception e) {
				ReportFactory.FailTest("FAILURE :: Error occured while loading response schema and response object");
				String strhtml = "<a><details><summary> Error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
				e.printStackTrace();
			}

		}else {

			try {	
				apiResponse.then().assertThat().body(matchesJsonSchemaInClasspath(API_Name+"_Schema.json"));
				ReportFactory.PassTest("SUCCESS :: API response matches the expected schema");	
			} catch (AssertionError error) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}catch(JsonSchemaValidationException e) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}catch(PathNotFoundException e) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}
		}
	}
	public void validateJsonSchema(String SchemaFileName){	

		if(APIType.equalsIgnoreCase("WS")) {
			try {
				org.json.JSONObject jsonSchema = new org.json.JSONObject(
						new JSONTokener(API_BaseClass.class.getResourceAsStream(SchemaFileName+".json")));
				org.json.JSONObject jsonObject = new org.json.JSONObject(new Gson().toJson(apiResponseObject));
				try {
					Schema schema = SchemaLoader.load(jsonSchema);
					schema.validate(jsonObject);
				}catch(ValidationException e) {
					System.out.println(e.getMessage());
					List<ValidationException> eList = e.getCausingExceptions();
					String eDetails="";
					for(ValidationException ex:eList) {
						eDetails+="field: "+ex.getPointerToViolation()+", error message: "+ex.getMessage()+"\n";
						System.out.println(eDetails);				
					}
					String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+eDetails+"</font></details></a>";
					ReportFactory.testInfo(strhtml);		
				}catch(Exception e) {
					ReportFactory.FailTest("FAILURE :: Error occured while validating the response schema");
					String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
					ReportFactory.testInfo(strhtml);
				}
			} catch (Exception e) {
				ReportFactory.FailTest("FAILURE :: Error occured while loading response schema and response object");
				String strhtml = "<a><details><summary> Error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
				e.printStackTrace();
			}

		}else {
			try {
				apiResponse.then().assertThat().body(matchesJsonSchemaInClasspath(SchemaFileName+".json"));
				ReportFactory.PassTest("SUCCESS :: API response matches the expected schema");
			} catch (AssertionError error) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}catch(JsonSchemaValidationException e) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}catch(PathNotFoundException e) {
				ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
				String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
				ReportFactory.testInfo(strhtml);
			}
		}
	}

	public Boolean validateJsonParamterType(String paramPath,String paramType) {
		Boolean match=true;
		paramPath.replaceAll(".", "/");
		if(paramPath.charAt(0)!='/') {
			paramPath="/"+paramPath;
		}	
		String wsResponse = apiResponseString;
		try {
			switch (paramType.toLowerCase()) {
			case "string":
				assertJson(wsResponse).at(paramPath).isText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "integer":
				assertJson(wsResponse).at(paramPath).isInteger();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "long":
				assertJson(wsResponse).at(paramPath).isLong();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "double":
				assertJson(wsResponse).at(paramPath).isDouble();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "float":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "boolean":
				assertJson(wsResponse).at(paramPath).isBoolean();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "number":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "null":
				assertJson(wsResponse).at(paramPath).isNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "notnull":
				assertJson(wsResponse).at(paramPath).isNotNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not null");
				break;
			case "empty":
				assertJson(wsResponse).at(paramPath).isEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is empty");
				break;
			case "notempty":
				assertJson(wsResponse).at(paramPath).isNotEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			case "notemptytext":
				assertJson(wsResponse).at(paramPath).isNotEmptyText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			case "byte":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			}
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: Parameter \""+paramPath+"\" is not \""+paramType+"\"");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}

		return match;

	}

	public Boolean validateJsonParameterType(String paramName,String paramType) {
		Boolean match = true;
		String wsResponse = apiResponseString;
		try {
			switch (paramType.toLowerCase()) {
			case "string":
				assertJson(wsResponse).at(paramName).isText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "integer":
				assertJson(wsResponse).at(paramName).isInteger();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "long":
				assertJson(wsResponse).at(paramName).isLong();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "double":
				assertJson(wsResponse).at(paramName).isDouble();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "float":
				assertJson(wsResponse).at(paramName).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "boolean":
				assertJson(wsResponse).at(paramName).isBoolean();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "number":
				assertJson(wsResponse).at(paramName).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "null":
				assertJson(wsResponse).at(paramName).isNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			case "notnull":
				assertJson(wsResponse).at(paramName).isNotNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is not null");
				break;
			case "empty":
				assertJson(wsResponse).at(paramName).isEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is empty");
				break;
			case "notempty":
				assertJson(wsResponse).at(paramName).isNotEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is not empty");
				break;
			case "notemptytext":
				assertJson(wsResponse).at(paramName).isNotEmptyText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is not empty");
				break;
			case "byte":
				assertJson(wsResponse).at(paramName).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramName+"\" is \""+paramType+"\"");
				break;
			}
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: Parameter \""+paramName+"\" is not \""+paramType+"\"");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}

		return match;

	}

	public Boolean validateJsonParamterType(Object JsonObject,String paramPath,String paramType) {
		Boolean match=true;
		paramPath.replaceAll(".", "/");
		if(paramPath.charAt(0)!='/') {
			paramPath="/"+paramPath;
		}	
		ObjectMapper mapper = new ObjectMapper();
		try {
			String wsResponse = mapper.writeValueAsString(JsonObject);
			switch (paramType.toLowerCase()) {
			case "string":
				assertJson(wsResponse).at(paramPath).isText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "integer":
				assertJson(wsResponse).at(paramPath).isInteger();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "long":
				assertJson(wsResponse).at(paramPath).isLong();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "double":
				assertJson(wsResponse).at(paramPath).isDouble();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "float":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "boolean":
				assertJson(wsResponse).at(paramPath).isBoolean();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "number":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "null":
				assertJson(wsResponse).at(paramPath).isNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "notnull":
				assertJson(wsResponse).at(paramPath).isNotNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not null");
				break;
			case "empty":
				assertJson(wsResponse).at(paramPath).isEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is empty");
				break;
			case "notempty":
				assertJson(wsResponse).at(paramPath).isNotEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			case "notemptytext":
				assertJson(wsResponse).at(paramPath).isNotEmptyText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			}
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: Parameter \""+paramPath+"\" is not \""+paramType+"\"");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			ReportFactory.FailTest("FAILURE :: Could not conver Json Object to String");
			e.printStackTrace();
		}

		return match;

	}

	public Boolean validateJsonParamterType(String JsonString,String paramPath,String paramType) {
		Boolean match=true;
		paramPath.replaceAll(".", "/");
		if(paramPath.charAt(0)!='/') {
			paramPath="/"+paramPath;
		}	
		try {
			String wsResponse = JsonString;
			switch (paramType.toLowerCase()) {
			case "string":
				assertJson(wsResponse).at(paramPath).isText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "integer":
				assertJson(wsResponse).at(paramPath).isInteger();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "long":
				assertJson(wsResponse).at(paramPath).isLong();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "double":
				assertJson(wsResponse).at(paramPath).isDouble();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "float":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "boolean":
				assertJson(wsResponse).at(paramPath).isBoolean();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "number":
				assertJson(wsResponse).at(paramPath).isNumber();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "null":
				assertJson(wsResponse).at(paramPath).isNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is \""+paramType+"\"");
				break;
			case "notnull":
				assertJson(wsResponse).at(paramPath).isNotNull();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not null");
				break;
			case "empty":
				assertJson(wsResponse).at(paramPath).isEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is empty");
				break;
			case "notempty":
				assertJson(wsResponse).at(paramPath).isNotEmpty();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			case "notemptytext":
				assertJson(wsResponse).at(paramPath).isNotEmptyText();
				ReportFactory.PassTest("SUCCESS :: Parameter \""+paramPath+"\" is not empty");
				break;
			}
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: Parameter \""+paramPath+"\" is not \""+paramType+"\"");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}

		return match;

	}

	/*public void quote1StdJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/oi").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/oiChange").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/token").isNumber();
			ReportFactory.PassTest("SUCCESS :: MDS Quote 1 Standard Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote1NcdexJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/oi").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/oiChange").isNumber();
			assertJson(mapper.writeValueAsString(quote1NcdexResponse)).at("/token").isNotEmpty();
			ReportFactory.PassTest("SUCCESS :: MDS Quote 1 NCDEX Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 1 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote2StdJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/oi").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/oiChange").isNumber();
			assertJson(mapper.writeValueAsString(quote1StdResponse)).at("/token").isNumber();
			ReportFactory.PassTest("SUCCESS :: WS Quote 1 response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: API response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote3StdJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/token").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/iyearHigh").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/iyearLow").isNumber();
			assertJson(mapper.writeValueAsString(quote3StdResponse)).at("/ilastUpdatedTime").isNumber();
			ReportFactory.PassTest("SUCCESS :: MDS Quote 3 Standard Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 3 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 3 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 3 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote3NcdexJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/token").isNotEmpty();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/iyearHigh").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/iyearLow").isNumber();
			assertJson(mapper.writeValueAsString(quote3NcdexResponse)).at("/ilastUpdatedTime").isNumber();
			ReportFactory.PassTest("SUCCESS :: MDS Quote 3 NCDEX Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE ::  MDS Quote 3 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE ::  MDS Quote 3 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE ::  MDS Quote 3 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote4StdJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/oi").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/oiChange").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/token").isNumber();
			assertJson(mapper.writeValueAsString(quote4StdResponse)).at("/itotalVolume").isNumber();
			ReportFactory.PassTest("SUCCESS :: MDS Quote 4 Standard Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 4 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: MDS Quote 4 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE ::MDS Quote 4 Standard Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void quote4NcdexJsonValidation(){
		try {
			ObjectMapper mapper = new ObjectMapper();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/ichange").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/ilastPrice").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/imktSegID").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/itransCode").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/oi").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/oiChange").isNumber();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/token").isNotEmpty();
			assertJson(mapper.writeValueAsString(quote4NcdexResponse)).at("/itotalVolume").isNumber();
			ReportFactory.PassTest("SUCCESS :: WS Quote 4 NCDEX Response matches the expected schema");
		} catch (AssertionError error) {
			ReportFactory.FailTest("FAILURE :: WS Quote 4 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+error.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(JsonSchemaValidationException e) {
			ReportFactory.FailTest("FAILURE :: WS Quote 4 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		}catch(PathNotFoundException e) {
			ReportFactory.FailTest("FAILURE :: WS Quote 4 NCDEX Response does not match the expected schema");
			String strhtml = "<a><details><summary>Schema error details(click to view)</summary><font color=black>"+e.getMessage()+"</font></details></a>";
			ReportFactory.testInfo(strhtml);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}*/

	public List<String> Convert_Pipe_String_To_List(String pipeString){
		String[] charArray = pipeString.split("|");


		ArrayList<String> list = new ArrayList<String>();
		String param = "";
		for(String c:charArray) {
			if(c.equals("|")) {
				list.add(param);
				param="";
			}else if((int)c.charAt(0)==194) {
				//skip character
			}else if((int)c.charAt(0)==160) {
				param+=" ";
			}else {
				param+=c;
			}
		}
		list.add(param);
		return list;
	}

	public List<String> Convert_Pipe_String_Array_To_List(String pipeString){
		List<String> orders = new ArrayList<String>();
		String order = "";
		for(int i=0;i<pipeString.length();i++) {
			char c = pipeString.charAt(i);
			if(c=='^') {
				orders.add(order);
				order="";
			}else {
				order+=c;
			}
		}
		return orders;
	}

	public String Convert_Object_To_Pipe_String(Object obj) {
		String pipeString="";
		if(obj instanceof Map<?, ?>) {
			Map<String,String> m = (Map<String, String>) obj;
			for(String k:m.keySet()) {
				pipeString+=m.get(k).toString()+"|";
			}

		}else if(obj instanceof List<?>){
			List<String> l = (List<String>) obj;
			for(String v:l) {
				pipeString+=v+"|";
			}
		}
		return pipeString;
	}

	public String cleanTextContent(String text)
	{
		return text;
		// strips off all non-ASCII characters
		//text = API_BaseClass.getAPIResponse();
		/*char space = '�';
		text = text.replaceAll(space+"", " ");
		text = text.replaceAll("[^\\x00-\\x7F]", "");


        // erases all the ASCII control characters
        text = text.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");

        // removes non-printable characters from Unicode
        text = text.replaceAll("\\p{C}", "");

        text = text.replaceAll("[^ -~]","");

        text = text.replaceAll("[^\\p{ASCII}]", "");

        text = text.replaceAll("\\\\x\\p{XDigit}{2}", "");

        text = text.replaceAll("\\n","");

        text = text.replaceAll("[^\\x20-\\x7e]", "");

        return text.trim();  */
	}

	public void roughFunnction() {
		//https://jsonplaceholder.typicode.com/posts/1
		//RestAssured.baseURI="https://jsonplaceholder.typicode.com";
		String file = "C:/Users/Chandrakant.Shetty/Desktop/demo.txt";
		RequestSpecification req = RestAssured.given().baseUri((String) this.BaseURI);
		req.contentType("multipart/form-data");
		req.multiPart("file", new File(file));
		Response resp = req.post("https://file.io");
		System.out.println(resp.getBody().asString());

		String command = "curl -F \"file=@"+file+"\" https://file.io";
		try {
			Process process = Runtime.getRuntime().exec(command);
			System.out.println(process.getInputStream());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		/*Response resp = req
				.baseUri("https://jsonplaceholder.typicode.com")
				.headers(IPO_API_Constants.GetIPODetails_GetReqHeaders())
				.pathParam("pathParam", 1)
				.get("/posts/{pathParam}");*/
		//HashMap<String, String> m = new HashMap<String, String>();
		//m.put("ipoid", "GRINFRA");
		//m.forEach((k,v)->req.queryParam(k, v));
		//req.queryParam("ipoid", "GRINFRA");
		//req.queryParams(IPO_API_Constants.GetIPODetails_QueryParams());

		//Response resp=RestAssured.post(RestAssured.baseURI+IPO_API_Constants.GetIPODetails_EndPoint);
		//System.out.println(resp.body().asString());

	}

}
