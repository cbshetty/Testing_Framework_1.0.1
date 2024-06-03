package com.data;

import java.util.HashMap;
import java.util.Map;
import com.api.payloads.AMXGetHolidayMasterV2;
import com.api.payloads.MPMOrder;
import com.api.payloads.SpanCalculateMargin;


public class Arom_Constants {
	
	//* * * * * * * SPAN Calculator * * * * * * *
	public static String BaseURI = "https://margin-calc-arom-prod.angelbroking.com"; // priority 1 if environment name and environment url are not passed in the argument
	public static String BaseURI_UAT="https://margin-calc-arom-prod.angelbroking.com"; // priority 1 if environment name is passed in the argument and environment base uri & default base uri is not set for api
	public static String BaseURI_SPAN_UAT = "https://margin-calc-arom-prod.angelbroking.com";
	public static String BaseURI_SPAN_UAT_ONPREM = "https://margin-calc-arom-prod.angelbroking.com:8080";
	public static String BaseURI_MPM_UAT="http://172.31.28.38:8080";
	public static String BaseURI_MPM_ONPREM_UAT="http://172.31.28.38:8080";
	
	//https://www.angelone.in/margin-calculator
	// http://span-calculator-uat.angelbroking.com:8080 - intranet
	// http://span-calculator-uat.angelbroking.com - public
	public static Map<String,String> property_Map = new HashMap<String,String>();
	
	//Get Exchange
	//public static String SPAN_GetExchange_BaseURI_UAT = "http://span-calculator-uat.angelbroking.com";
	public static String SPAN_GetExchange_EndPoint = "/exchange";
	public static String SPAN_GetExchange_MethodType = "GET";	
	public static String SPAN_GetExchange_ReqHeaders_Names = "accept";
	public static String SPAN_GetExchange_ReqHeaders_Values= "application/json";
	
	//Get Exchange Product
	//public static String SPAN_GetExchangeProduct_BaseURI_UAT = "http://span-calculator-uat.angelbroking.com";
	public static String SPAN_GetExchangeProduct_EndPoint = "/exchange/{param1}/product";
	public static String SPAN_GetExchangeProduct_MethodType = "GET";	
	public static String SPAN_GetExchangeProduct_ReqHeaders_Names = "accept";
	public static String SPAN_GetExchangeProduct_ReqHeaders_Values= "application/json";
	
	//Get Product Contract
	public static String SPAN_GetProductContract_EndPoint = "/exchange/{param1}/product/{param2}/contract";
	public static String SPAN_GetProductContract_MethodType = "GET";	
	public static String SPAN_GetProductContract_ReqHeaders_Names = "accept";
	public static String SPAN_GetProductContract_ReqHeaders_Values= "application/json";
	
	//Get Contract Strike Price
	public static String SPAN_GetStrikePrice_EndPoint = "/exchange/{param1}/product/{param2}/contract/{param3}/strike-price?optionType={param4}";
	public static String SPAN_GetStrikePrice_MethodType = "GET";	
	public static String SPAN_GetStrikePrice_ReqHeaders_Names = "accept";
	public static String SPAN_GetStrikePrice_ReqHeaders_Values= "application/json";
	//public static List<String> SPAN_GetStrikePrice_RespHeaders = Arrays.asList("")
	
	//Marin Calculator
	public static String SPAN_MarginCalculator_EndPoint = "/margin-calculator/SPAN";
	public static String SPAN_MarginCalculator_MethodType = "POST";	
	public static String SPAN_MarginCalculator_ReqHeaders_Names = "accept";
	public static String SPAN_MarginCalculator_ReqHeaders_Values= "application/json";
	public SpanCalculateMargin CalculateMarginPayload;
	
	//App-State
	public static String SPAN_AppState_EndPoint = "/app-state/files";
	public static String SPAN_AppState_MethodType = "GET";	
	public static String SPAN_AppState_ReqHeaders_Names = "accept";
	public static String SPAN_AppState_ReqHeaders_Values= "application/json";
	
	public static String AMXGetHolidayMasterV2_BaseURI = "https://amxuat.angelbroking.com";
	public static String AMXGetHolidayMasterV2_EndPoint = "/reports/v2/holidayMaster";
	public static String AMXGetHolidayMasterV2_MethodType = "POST";
	public static String AMXGetHolidayMasterV2_ReqHeaders_Names = "X-SourceID,X-UserType,X-ClientLocalIP,X-ClientPublicIP,X-MACAddress,X-SystemInfo,X-Location,X-AppID,X-Request-Id";
	public static String AMXGetHolidayMasterV2_ReqHeaders_Values= "5,1, 172.29.24.126,172.29.24.126,00:25:96:FF:FE:12:34:56,aliqua ad,aliqua ad,aliqua ad,aliqua ad";
	public AMXGetHolidayMasterV2 AMXGetHolidayMasterV2_ReqBody;
	
	/*
	//MPM Place Order
	// /order/{accountId}
	public static String MPM_PlaceOrder_EndPoint = "/order/{param1}";
	public static String MPM_PlaceOrder_MethodType = "POST";	
	public static String MPM_PlaceOrder_ReqHeaders_Names = "accept";
	public static String MPM_PlaceOrder_ReqHeaders_Values= "application/json";
	public static MPMOrder MPM_PlaceOrder_RequestDetails;
	public static String MPMPlaceOrderAccountId;
	public static int MPMOrderId;
	public static int MPMPlaceOrderId;
	public static int dataRow;
	public static List<HashMap<String,Object>> MPMOrderEntries;
	
	//MPM Get Orders
	// /order/{accountId}
	public static String MPM_GetOrders_EndPoint = "/order/{param1}";
	public static String MPM_GetOrders_MethodType = "GET";	
	public static String MPM_GetOrders_ReqHeaders_Names = "accept";
	public static String MPM_GetOrders_ReqHeaders_Values= "application/json";
	*/
	//MPM Modify Order
	// /order/{accountId}/{orderId}
	public static String MPM_ModifyOrder_EndPoint = "/order/{param1}/{param2}";
	public static String MPM_ModifyOrder_MethodType = "PATCH";	
	public static String MPM_ModifyOrder_ReqHeaders_Names = "accept";
	public static String MPM_ModifyOrder_ReqHeaders_Values= "application/json";
	public static MPMOrder MPM_ModifyOrder_RequestDetails;
	public static String MPMModifyOrderAccountId;
	public static int MPMModifyOrderId;
	public static String MPM_ModifyOrder_RequestBody = "{\r\n" + 
			"    \"branchId\": \"\",\r\n" + 
			"    \"cancelledSize\": 0,\r\n" + 
			"    \"classification\": \"A\",\r\n" + 
			"    \"clientGroupId\": \"clientgroup\",\r\n" + 
			"    \"clientId\": \"\",\r\n" + 
			"    \"customerFirm\": \"X\",\r\n" + 
			"    \"dealerId\": \"\",\r\n" + 
			"    \"discloseQuantity\": 0,\r\n" + 
			"    \"exchangeBroker\": \"\",\r\n" + 
			"    \"exchangeSegment\": \"nse_cm\",\r\n" + 
			"    \"marketProtectionPrice\": 620,\r\n" + 
			"    \"orderDuration\": \"DAY\",\r\n" + 
			"    \"orderId\": 20110113311116,\r\n" + 
			"    \"orderState\": \"\",\r\n" + 
			"    \"orderStatus\": \"\",\r\n" + 
			"    \"orderType\": \"L\",\r\n" + 
			"    \"orderValidityDate\": \"2022-08-25\",\r\n" + 
			"    \"price\": 605,\r\n" + 
			"    \"product\": \"MIS\",\r\n" + 
			"    \"quantity\": 1,\r\n" + 
			"    \"strategyId\": \"0\",\r\n" + 
			"    \"tickSize\": 5,\r\n" + 
			"    \"token\": \"3045\",\r\n" + 
			"    \"tradeSymbol\": \"SBIN-EQ\",\r\n" + 
			"    \"transactionType\": \"B\",\r\n" + 
			"    \"trgSeqNo\": \"A\",\r\n" + 
			"    \"triggerPrice\": 1,\r\n" + 
			"    \"userId\": \"atomar\",\r\n" + 
			"    \"exchangeOrderId\": 0,\r\n" + 
			"    \"updatedOrderId\": 0\r\n" + 
			"}";
	
	//MPM Cancel Order
	// /order/{accountId}
	public static String MPM_CancelOrder_EndPoint = "/order/{param1}";
	public static String MPM_CancelOrder_MethodType = "DELETE";	
	public static String MPM_CancelOrder_ReqHeaders_Names = "accept";
	public static String MPM_CancelOrder_ReqHeaders_Values= "application/json";
	//public static MPMCancelOrder MPM_CancelOrder_RequestDetails;
	public static String MPMCancelOrderAccountId;
	public static int MPMCancelOrderId;
	public static String MPM_CancelOrder_RequestBody = "{\r\n" + 
			"  \"cancellationReason\": \"reason\",\r\n" + 
			"  \"exchangeSegment\": \"nse_cm\",\r\n" + 
			"  \"orderId\": 20110113311117\r\n" + 
			"}";
	
}
