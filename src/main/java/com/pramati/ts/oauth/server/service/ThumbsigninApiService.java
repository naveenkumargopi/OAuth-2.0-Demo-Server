package com.pramati.ts.oauth.server.service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.pramati.thumbsignin.servlet.sdk.Action;
import com.pramati.thumbsignin.servlet.sdk.ThumbsignInClient;
import com.pramati.thumbsignin.servlet.sdk.ThumbsignInRequest;
import com.pramati.thumbsignin.servlet.sdk.ThumbsignInResponse;
import com.pramati.thumbsignin.servlet.sdk.ThumbsigninException;
import com.pramati.thumbsignin.servlet.sdk.TransactionStatus;

@Service
public class ThumbsigninApiService {
		
	//@Value("${client.authenticationSuccess.redirect.url}")
    private String authSuccessClientRedirectUrl = "/oauth-ts/loginSuccess";
	
	//@Value("${client.registrationSuccess.redirect.url}")
    private String registrationSuccessClientRedirectUrl = "/oauth-ts/regSuccess";
	
	//@Value("${client.accessDenied.redirect.url}")
    //private String accessDeniedClientRedirectUrl;
	
	//@Autowired
	//private AzureApiService azureApiService;
	
	private static String statusRequestType;
	
	private static final String STATUS = "Status";
	
	private static final String AUTHENTICATION_STATUS = "authStatus";
	
	private static final String REDIRECT_URL = "redirectUrl";
	
	//private static final String USER_ID_QUERY_PARAM = "{userId}";
	
	private static final String USER_ID = "userId";
	
	private static final String CANCELLED = "cancelled";
	
	private static final String USER_NOT_FOUND_IN_AZURE_AD = "userRemovedFromAzureAD";
	
	private final ThumbsignInClient thumbsignInClient;

    public ThumbsigninApiService() {
        this(new ThumbsignInClient());
    }

    public ThumbsigninApiService(final ThumbsignInClient thumbsignInClient) {
        this.thumbsignInClient = thumbsignInClient;
    }
	
    private void initializeThumbSignInClient(String tsApiKey, String tsApiSecret) {
    	thumbsignInClient.setApiKey(tsApiKey);
    	thumbsignInClient.setApiSecret(tsApiSecret);
    }
    
	public String processThumbsigninRequest(HttpServletRequest servletRequest, String tsApiKey, String tsApiSecret) throws IOException {
		initializeThumbSignInClient(tsApiKey, tsApiSecret);
		ThumbsignInRequest thumbsignInRequest = createThumbsignInRequest(servletRequest);
		ThumbsignInResponse thumbsignInResponse = thumbsignInClient.get(thumbsignInRequest);
		processResponse(thumbsignInRequest, thumbsignInResponse, servletRequest);
		return thumbsignInResponse.getDataAsString();
	}
	
	private ThumbsignInRequest createThumbsignInRequest(HttpServletRequest servletRequest) {
        String pathInfo = servletRequest.getPathInfo();
        if (pathInfo == null) {
        	pathInfo = servletRequest.getServletPath();
        }
        pathInfo = pathInfo.substring(1);
        String[] pathParts = pathInfo.split("/");
        String actionStr = pathParts[2];
        if (pathParts[2].contains(STATUS)) {
        	actionStr = STATUS.toLowerCase();
        	statusRequestType = pathParts[2];
        }
        Action action = Action.fromValue(actionStr);
        if ((action == null) || (Action.GET_USER.equals(action))) {
            throw new ThumbsigninException("Not Found");
        }
        ThumbsignInRequest thumbsignInRequest = new ThumbsignInRequest(action);
        thumbsignInRequest.addHeader("User-Agent", servletRequest.getHeader("User-Agent"));
        
        if (Action.AUTHENTICATE.equals(thumbsignInRequest.getAction())) {
        	SecurityContextHolder.getContext().setAuthentication(null);
        	SecurityContextHolder.clearContext();
        } else if (Action.STATUS.equals(thumbsignInRequest.getAction())) {
        	String cancelled = servletRequest.getParameter(CANCELLED);
            if ((cancelled != null) && ("true".equals(cancelled))) {
                thumbsignInRequest.addQueryParam(CANCELLED, cancelled);
            }
            thumbsignInRequest.setTransactionId(pathParts[3]);
        } else if (Action.REGISTER.equals(thumbsignInRequest.getAction())) {
        	thumbsignInRequest.addQueryParam(USER_ID, pathParts[3]);
        }
        return thumbsignInRequest;
    }
	
	private void processResponse(ThumbsignInRequest thumbsignInRequest, ThumbsignInResponse thumbsignInResponse, HttpServletRequest servletRequest) throws IOException {
        if (Action.STATUS.equals(thumbsignInRequest.getAction())) {
            handleStatusResponse(thumbsignInRequest, thumbsignInResponse, servletRequest);
        }
    }

    private void handleStatusResponse(ThumbsignInRequest thumbsignInRequest, ThumbsignInResponse thumbsignInResponse, HttpServletRequest servletRequest) throws IOException {
        Object status = thumbsignInResponse.getValue("status");
        if (TransactionStatus.COMPLETED_SUCCESSFUL.toString().equals(status)) {
        	
        	ThumbsignInResponse resp = thumbsignInClient.getAuthenticatedUser(thumbsignInRequest.getTransactionId());
    		if ((resp.getStatus() == 200) && (resp.getValue("userId") != null)) {                	
    			String thumbsignin_UserId = (String)resp.getValue("userId");
    			
    			//List<Membership> listOfUserMemberships = new ArrayList<>();
    			String userName = "";
    			if (statusRequestType.equals(AUTHENTICATION_STATUS)) {
    				//listOfUserMemberships = azureApiService.getUserMembershipInfoFromGraph(thumbsignin_UserId);
    				//userName = azureApiService.getUserNameByIdFromGraph(thumbsignin_UserId);
    				//if (listOfUserMemberships == null || userName.equals(USER_NOT_FOUND_IN_AZURE_AD)) {
    					//thumbsignInResponse.getData().put(USER_NOT_FOUND_IN_AZURE_AD, true);
    					//thumbsignInResponse.getData().put(REDIRECT_URL, accessDeniedClientRedirectUrl);
    				//} else {
    					thumbsignInResponse.getData().put("userId", thumbsignin_UserId);
        				//thumbsignInResponse.getData().put("userRolesFromAzure", listOfUserMemberships);
        				//thumbsignInResponse.getData().put("userNameFromAzure", userName);
        				thumbsignInResponse.getData().put(REDIRECT_URL, authSuccessClientRedirectUrl);
        				//thumbsignInResponse.getData().put(REDIRECT_URL, authSuccessClientRedirectUrl.replace(USER_ID_QUERY_PARAM, thumbsignin_UserId));
    				//}    				
    			} else {
    				thumbsignInResponse.getData().put(REDIRECT_URL, registrationSuccessClientRedirectUrl);
    			}
            }
        	clearStatusRequestType();
        }
    }
    
    private void clearStatusRequestType() {
    	statusRequestType = "";
    }

}
