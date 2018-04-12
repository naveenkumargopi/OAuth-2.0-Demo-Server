package com.pramati.ts.oauth.server.controller;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

import javax.servlet.http.HttpServletResponse;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.pramati.ts.oauth.server.security.HmacSignatureBuilder;


//The "/me" path is protected with the access token by declaring that  
//our OAuth app is a Resource Server (in addition to being an Authorization Server)
@Configuration
@EnableResourceServer
@RestController
public class ResourceServerController extends ResourceServerConfigurerAdapter {
	
	private final static String appId = "azureConnectorApp";
    
    private final static String appSecret = "azureThumb$ignInP@ss";
    
    private static CloseableHttpClient httpclient = HttpClients.createDefault();
	
	@Override
	public void configure(HttpSecurity http) throws Exception {
	  http
	    .antMatcher("/me")
	    .authorizeRequests().anyRequest().authenticated();
	}
	
	@RequestMapping({"/me"})
	public String userInfo(OAuth2Authentication authentication) throws ClientProtocolException, URISyntaxException, IOException {		
		String thumbSignInUserId = authentication.getUserAuthentication().getName();
		String userRolesApiResponse = invokeTSAADGatewayAPI("getUserRolesFromAzureAD", thumbSignInUserId);
		String userNameApiResponse = invokeTSAADGatewayAPI("getUserNameFromAzureAD", thumbSignInUserId);
		JSONArray userMemberships = new JSONArray(userRolesApiResponse);
		processUserMembershipData(userMemberships);		
		
		JSONObject userData = new JSONObject();
		userData.put("userMembership", userMemberships);
		userData.put("userid", thumbSignInUserId);
		userData.put("username", userNameApiResponse);		
		return userData.toString();
	}
	
	@RequestMapping({"/user"})
	public OAuth2Authentication user(OAuth2Authentication authentication) {
		return authentication;
	}
	
	private String invokeTSAADGatewayAPI(String apiName, String thumbSignInUserId) throws URISyntaxException, ClientProtocolException, IOException {

		//String urlStr = String.format("https://azuread-api-stage.thumbsignin.com/ts-aad/secure/%s/%s",apiName, thumbSignInUserId);
		//String urlStr = String.format("http://localhost:8012/ts-aad/secure/%s/%s",apiName, thumbSignInUserId);
		String urlStr = String.format("http://172.17.10.110:8082/ts-aad/secure/%s/%s",apiName, thumbSignInUserId);
    	
		URI uri = new URIBuilder(urlStr).build();		
		HttpGet httpget = new HttpGet(uri);
		
		String signatureTimestamp = HmacSignatureBuilder.getTimeStamp();
		httpget.addHeader("Content-Type", "application/json");
		httpget.addHeader("Accept", "application/json");
		httpget.addHeader(HmacSignatureBuilder.X_TS_DATE_HEADER, signatureTimestamp);
		httpget.addHeader("Authorization", HmacSignatureBuilder.createHmacSignature(uri.getPath(),signatureTimestamp, appId, appSecret));
				
		ResponseHandler<String> responseHandler = new ResponseHandler<String>() {
            @Override
            public String handleResponse(HttpResponse response) throws IOException {
                HttpEntity entity = response.getEntity();
                String jsonResponse = entity != null ? EntityUtils.toString(entity) : null;
                return jsonResponse;
            }
        };
		
        return httpclient.execute(httpget, responseHandler);
	}
	
	private void processUserMembershipData(JSONArray userMemberships) {
		for (int i = 0; i < userMemberships.length(); i++) {
            JSONObject userMembershipJSONObject = userMemberships.optJSONObject(i);
            userMembershipJSONObject.remove("description");
            userMembershipJSONObject.remove("objectId");
            String roleName = userMembershipJSONObject.getString("displayName");
            String roleType = userMembershipJSONObject.getString("objectType");
            userMembershipJSONObject.remove("displayName");
            userMembershipJSONObject.remove("objectType");
            userMembershipJSONObject.put("name", roleName);
            userMembershipJSONObject.put("type", roleType);
        }
	}
}
