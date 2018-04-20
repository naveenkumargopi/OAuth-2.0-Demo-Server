package com.pramati.ts.oauth.server.thumbsignin.sdk;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class ThumbsigninApiService {
		
	//@Value("${client.authenticationSuccess.redirect.url}")
    private String authSuccessClientRedirectUrl = "/ts/secure/loginSuccess";
	
	//@Value("${client.registrationSuccess.redirect.url}")
    private String registrationSuccessClientRedirectUrl = "/ts/secure/registrationSuccess";
	
	private static String statusRequestType;
	
	private static final String STATUS = "Status";
	
	private static final String AUTHENTICATION_STATUS = "authStatus";
	
	private static final String REDIRECT_URL = "redirectUrl";
	
	private static final String USER_ID = "userId";
	
	private static final String CANCELLED = "cancelled";
	
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
        	String userId = "OAuth" + pathParts[3]; //Note: OAuth is appended just to identify that these are test users. This can be removed.
        	thumbsignInRequest.addQueryParam(USER_ID, userId);
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
    			
    			if (statusRequestType.equals(AUTHENTICATION_STATUS)) {
    				thumbsignInResponse.getData().put("userId", thumbsignin_UserId);
        			thumbsignInResponse.getData().put(REDIRECT_URL, authSuccessClientRedirectUrl);
        			setUserAuthenticationInContext(thumbsignin_UserId);
    			} else {
    				thumbsignInResponse.getData().put(REDIRECT_URL, registrationSuccessClientRedirectUrl);
    				setUserAuthenticationInContext(thumbsignin_UserId);
    			}
            }
        	clearStatusRequestType();
        }
    }
    
    private void clearStatusRequestType() {
    	statusRequestType = "";
    }
    
    /*
     * After successful ThumbSignIn User Registration/Authentication process, we create a user session within the OAuth Server
     */
    public void setUserAuthenticationInContext(String userId) {
    	UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
    			userId, "N/A", null);
		authenticationToken.setDetails("ThumbSignIn Registered User");
		if(authenticationToken.isAuthenticated()) {
			SecurityContextHolder.getContext().setAuthentication(authenticationToken);
		}
		
    }

}
