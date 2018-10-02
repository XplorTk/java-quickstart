package nova.helpers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class Utils {
	
	private final int BORROW_LOAN_DECISION_THRESHOLD = 650;

	@Value("${NOVA_SECRET_KEY}")
	private String novaSecretKey;
	
	@Value("${NOVA_ENV}")
	private String novaEnv;
	
	@Value("${NOVA_CLIENT_ID}")
	private String novaClientId;
	
	@Value("${NOVA_ACCESS_TOKEN_URL}")
	private String novaAccessTokenUrl;
	
	@Value("${NOVA_PASSPORT_URL}")
	private String novaPassportUrl;
	
	/**
	 * For demo purposes, we'll store the results of a Nova Credit Passport in-memory via a global variable
	 * Note that production usage should store received data in a database, associated to its respective applicant
	 */
	public static HashMap<String, String> receivedReportData;
	
	/**
	 * Here's where the magic happens!
	 * Parse the credit passport we received for a given applicant, such as storing applicant metadata and analyzing the tradeline data for underwriting purposes
	 */
	@SuppressWarnings("unchecked")
	private void parseNovaPassport(String userArgs, String publicToken, HashMap<String, Object> creditPassport) {
		// See https://docs.neednova.com/ for a full explanation of the Nova Credit Passport
		List<HashMap<String, Object>> scores = (ArrayList<HashMap<String, Object>>) creditPassport.get("scores");
		HashMap<String, String> personal = (HashMap<String, String>) creditPassport.get("personal");

		/*
		 * Now that we have this data, you can easily add Nova to your existing underwriting engine.
		 * In this example, our underwriting decision is: accept applicants whose NOVA_SCORE_BETA is greater than BORROW_LOAN_DECISION_THRESHOLD
		 */

		int novaScoreValue = 0;
		for (HashMap<String, Object> score : scores) {
			if (score.get("score_type").equals("NOVA_SCORE_BETA")) {
				novaScoreValue = (int) score.get("value");
				break;
			}
		}

		/*
		 * Make our decision:
		 */
		String borrowLoanDecision = novaScoreValue > BORROW_LOAN_DECISION_THRESHOLD ? "APPROVE" : "DENY";

		receivedReportData = new HashMap<String, String>();
		receivedReportData.put( "userArgs", userArgs );
		receivedReportData.put( "publicToken" , publicToken );
		receivedReportData.put( "applicantName" , personal.get("full_name") );
		receivedReportData.put( "applicantEmail" , personal.get("email") );
		receivedReportData.put( "novaScore" , String.format("%d", novaScoreValue) );
		receivedReportData.put( "borrowLoanDecision" , borrowLoanDecision );
	}
	
	/**
	 * Logic for handling the webhook sent by Nova Credit to the callback url once an applicant's report status has changed
	 */
	public void handleNovaWebhook(String publicToken, String userArgs) {
		/*
		 * Get an access token from Nova
		 */
		String accessToken = getAccessToken();

		/*
		 * Now make a request to Nova to fetch the Credit Passport for the public token provided in the webhook (i.e., unique identifier for the credit file request in Nova's system)
		 */
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Bearer " + Base64.encodeBase64String(accessToken.getBytes()));
		headers.add("X-ENVIRONMENT", novaEnv);
		headers.add("X-PUBLIC-TOKEN", publicToken);

		ParameterizedTypeReference<HashMap<String, Object>> responseType = 
						new ParameterizedTypeReference<HashMap<String, Object>>() {};
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		ResponseEntity<HashMap<String, Object>> passportResult = restTemplate.exchange(novaPassportUrl, HttpMethod.GET, entity, responseType);
		parseNovaPassport(userArgs, publicToken, passportResult.getBody());
	}
	
	/**
	 * Retrieves an access token from Nova
	 */
	private String getAccessToken() {
		String novaBasicAuthCreds = Base64.encodeBase64String(String.format("%s:%s", novaClientId, novaSecretKey).getBytes());
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + novaBasicAuthCreds);
		headers.add("X-ENVIRONMENT", novaEnv);
		
		ParameterizedTypeReference<HashMap<String, String>> responseType = 
						new ParameterizedTypeReference<HashMap<String, String>>() {};
		HttpEntity<String> entity = new HttpEntity<>("parameters", headers);
		ResponseEntity<HashMap<String, String>> tokenResult = restTemplate.exchange(novaAccessTokenUrl, HttpMethod.GET, entity, responseType);
		
		return tokenResult.getBody().get("accessToken");
	}
}
