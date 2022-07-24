package com.eka.connect.creditrisk.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.json.JSONArray;
import org.json.JSONObject;
import org.owasp.esapi.ESAPI;
import org.owasp.esapi.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import com.eka.connect.creditrisk.constants.CreditRiskConstants;
import com.eka.connect.creditrisk.constants.PlatformConstants;
import com.eka.connect.creditrisk.constants.PropertyConstants;
import com.eka.connect.creditrisk.constants.RelativeUrlConstants;
import com.eka.connect.creditrisk.dataobject.CounterpartyExposure;
import com.eka.connect.creditrisk.dataobject.DerivedPaymentTerms;
import com.eka.connect.creditrisk.dataobject.FxRates;
import com.eka.connect.creditrisk.dataobject.PlatformData;
import com.eka.connect.creditrisk.exception.PlatformException;
import com.eka.connect.creditrisk.util.RestTemplateUtil;
import com.fasterxml.jackson.databind.ObjectMapper;

@Service
public class PlatformDataService {

	@Value("${eka_connect_host}")
	private String ekaConnectHost;
	
	@Autowired
	private RestTemplate restTemplate;

	@Autowired
	private RestTemplateUtil restTemplateUtil;

	private static final Logger LOGGER = ESAPI
			.getLogger(PlatformDataService.class);

	public List<CounterpartyExposure> getCounterPartyExposures(
			JSONObject filterObject, HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws PlatformException {
		List<CounterpartyExposure> pojos = null;

		try {
			String finalString = getPlatformDataWithConnectKeys(
					PropertyConstants.COLLECTION_NAME_EXPOSURE_DATA,
					filterObject,
					PropertyConstants.CONNECT_PLATFORM_COLUMN_MAPPING_EXPOSURE_DATA,
					httpHeaders, appProperties);

			ObjectMapper mapper = new ObjectMapper();
			pojos = Arrays.asList(mapper.readValue(finalString,
					CounterpartyExposure[].class));

		} catch (IOException je) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error occured while converting Sting to list of Exposures...")
							,je);
			 
			throw new PlatformException(
					"Error while reading collection data of '"
							+ appProperties.get(PropertyConstants.COLLECTION_NAME_EXPOSURE_DATA)
							+ "'", je);
		}
		return pojos;

	}

	@SuppressWarnings("unchecked")
	private String getPlatformDataWithConnectKeys(
			String collectionPropertyName, JSONObject filterObject,
			String connectPlatformColumnMappingProperty,
			HttpHeaders httpHeaders, Map<String, Object> appProperties) {

		ArrayList<String> platformCollectionNames = (ArrayList<String>) appProperties
				.get(collectionPropertyName);
		Map<String, Map<String, String>> map = (LinkedHashMap<String, Map<String, String>>) appProperties
				.get(connectPlatformColumnMappingProperty);
		JSONArray finalJsonArray = new JSONArray();
		for (String collectionName : platformCollectionNames) {

			if (map != null && map.containsKey(collectionName)) {

				Map<String, String> collectionConnectMapping = map
						.get(collectionName);
				convertConnectKeysToPlatformKeys(filterObject,
						collectionConnectMapping);
				String collectionData = this.getPlatformCollectionDataAsString(
						collectionName, filterObject, httpHeaders,
						appProperties);
				JSONArray collectionDataJsonArray = new JSONArray(
						collectionData);
				String finalString = changeKeysAsPerConfiguration(
						collectionDataJsonArray.toString(),
						collectionConnectMapping);
				JSONArray array = new JSONArray(finalString);
				for (int i = 0; i < array.length(); i++) {
					finalJsonArray.put(array.get(i));
				}
			}

		}

		return finalJsonArray.toString();
	}

	private void convertConnectKeysToPlatformKeys(JSONObject filterObject,
			Map<String,String> collectionConnectMapping) {
		if (null == filterObject
				|| null == collectionConnectMapping) {
			return;
		}
 
		JSONArray jsonArray = filterObject
				.optJSONArray(PlatformConstants.FILTER);
		if (jsonArray != null) {
			for (int i = 0; i < jsonArray.length(); i++) {
				JSONObject obj = jsonArray.getJSONObject(i);
				if (collectionConnectMapping.containsKey(obj.getString(PlatformConstants.FIELD_NAME))) {
					obj.put(PlatformConstants.FIELD_NAME, collectionConnectMapping.get(obj
							.getString(PlatformConstants.FIELD_NAME)));
				}
			}
		}
	}

	private String changeKeysAsPerConfiguration(String jsonString,
			Map<String, String> collectionConnectMapping) {
		if (null == collectionConnectMapping) {
			return jsonString;
		}
	 
		Iterator<Entry<String, String>> iterator = collectionConnectMapping.entrySet().iterator();
		while (iterator.hasNext()) {
			Entry<String, String> next = iterator.next();
			String connectProperty = next.getKey();
			String platformProperty = next.getValue();
			jsonString = String.join(connectProperty,
					jsonString.split(platformProperty));
		}

		return jsonString;
	}
 

	public String getPlatformCollectionDataAsString(
			String collectionName, JSONObject filterObject,
			HttpHeaders httpHeaders, Map<String, Object> appProperties) {

		try {

			HttpHeaders headers = httpHeaders;
			httpHeaders.set("ttl", "0");
			
			JSONObject bodyObject = new JSONObject();
			bodyObject.put("criteria", filterObject);
			bodyObject.put("collectionName", collectionName);
			
			if(filterObject!=null && filterObject.has("start")){
				bodyObject.put("start", filterObject.remove("start"));
			}
			
			if(filterObject!=null && filterObject.has("limit")){
				bodyObject.put("limit", filterObject.remove("limit"));
			}
			
			HttpEntity<String> httpEntity = new HttpEntity<>(
					bodyObject != null ? bodyObject.toString() : "",
					headers);

			/* String url = getPlatformUrlFromProperties(collectionName,
					appProperties);*/
			String url  = ekaConnectHost + RelativeUrlConstants.CONNECT_PLATFORM_COLLECTION_URL;

			LOGGER.info(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Started calling Connect-Platform url :: " + url));
			LOGGER.info(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Connect-Platform url body object :: "
							+ bodyObject));
 
			ResponseEntity<String> responseEntity = restTemplate.exchange(url,
					HttpMethod.POST, httpEntity, String.class);

			LOGGER.info(Logger.EVENT_SUCCESS,
					ESAPI.encoder().encodeForHTML("Completed calling Connect-Platform url :: "
							+ url)); 

			return responseEntity.getBody();

		} catch (HttpClientErrorException he) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("Platform HttpClientErrorException Exception details --> "
							+ he.getRawStatusCode() + "" + he.getResponseBodyAsString()
							+ he.getResponseHeaders())
							,he);
	 
			throw new PlatformException(
					"Error occured whild processing collection '"
							+ collectionName + "' with response body :"+ he.getResponseBodyAsString(),
					he);

		} catch (Exception e) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML("Exception occured while quering platform data..")
							,e);
			throw new PlatformException(
					"Error occured whild processing collection '"
							+ collectionName + "'",
					e);
		}
	}

	public List<DerivedPaymentTerms> getDerivedPaymentTerms(
			JSONObject filterObject, HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws PlatformException {
		List<DerivedPaymentTerms> pojos = null;
		try {
			String finalString = getPlatformDataWithConnectKeys(
					PropertyConstants.COLLECTION_NAME_DERIVED_PAYMENT_TERMS,
					filterObject,
					PropertyConstants.CONNECT_PLATFORM_COLUMN_MAPPING_DERIVED_PAYMENT_TERMS,
					httpHeaders, appProperties);
			ObjectMapper mapper = new ObjectMapper();
			pojos = Arrays.asList(mapper.readValue(finalString,
					DerivedPaymentTerms[].class));

		} catch (IOException je) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error while reading collection data of '"
									+ appProperties.get(PropertyConstants.COLLECTION_NAME_DERIVED_PAYMENT_TERMS)
									+ "'")
							,je);
			 
			throw new PlatformException(
					"Error while reading collection data of '"
							+ appProperties.get(PropertyConstants.COLLECTION_NAME_DERIVED_PAYMENT_TERMS)
							+ "'", je);
		}
		return pojos;

	}

	public List<FxRates> getFxRates(HttpHeaders httpHeaders,
			Map<String, Object> appProperties) throws Exception {
		List<FxRates> pojos = null;

		try {
			String finalString = getPlatformDataWithConnectKeys(
					PropertyConstants.COLLECTION_NAME_FX_RATES,
					null,
					PropertyConstants.CONNECT_PLATFORM_COLUMN_MAPPING_FXRATES_TERMS,
					httpHeaders, appProperties);

			ObjectMapper mapper = new ObjectMapper();
			pojos = Arrays.asList(mapper
					.readValue(finalString, FxRates[].class));

		} catch (IOException je) {
			LOGGER.error(
					Logger.EVENT_FAILURE,
					ESAPI.encoder().encodeForHTML(
							"Error occured while converting Sting to list of Exposures...")
							,je);
			throw new PlatformException(
					"Error while reading collection data of '"
							+ appProperties.get(PropertyConstants.COLLECTION_NAME_FX_RATES)
							+ "'", je);
		}
		return pojos;

	}

	private String getPlatformUrlFromProperties1(
			String collectionName, Map<String, Object> appProperties) {
		String url = (String) appProperties.get(PropertyConstants.PLATRORM_URL)
				+ RelativeUrlConstants.PLATFORM_COLLECTION_URL.replace(
						"${collectionName}",
						collectionName);
		return url;
	}
	
}
